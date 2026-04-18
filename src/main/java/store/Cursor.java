package store;

import btree.InternalNode;
import btree.LeafNode;
import btree.NodeType;
import lombok.Getter;

import static store.DatabaseConstants.PAGE_SIZE;

public class Cursor {

    private final Pager pager;
    private final int startPage;
    private int currentPageNumber;
    private final int rowSize;
    private LeafNode node;
    private int cellIndex;

    @Getter
    private boolean endOfTable;

    public Cursor(Pager pager, int startPage, int rowSize) {
        byte[] page = pager.getPage(startPage);
        if (NodeType.values()[page[0]] == NodeType.LEAF) {
            node = LeafNode.from(page, rowSize);
            currentPageNumber = startPage;
            endOfTable = (node.getNumCells() == 0);
        } else {
            int currentPageNumber = startPage;
            while (NodeType.values()[pager.getPage(currentPageNumber)[0]] != NodeType.LEAF) {
                currentPageNumber = InternalNode.from(pager.getPage(currentPageNumber)).getChildPtr(0);
            }
            node = LeafNode.from(pager.getPage(currentPageNumber), rowSize);
            this.currentPageNumber = currentPageNumber;
        }

        this.pager = pager;
        this.startPage = startPage;
        this.rowSize = rowSize;
    }

    public void insert(int key, byte[] rowBytes) {
        navigateToLeaf(key);
        node = LeafNode.from(pager.getPage(currentPageNumber), rowSize);
        int cellIndex = node.findCell(key);

        // check duplicate
        if (cellIndex < node.getNumCells() && node.getKey(cellIndex) == key) {
            throw new StorageException("Duplicate key: " + key);
        }

        if (node.getMaxCells() == node.getNumCells()) {
            splitAndInsert(key, rowBytes);
        } else {
            node.insertCell(cellIndex, key, rowBytes);
            node.setNumCells(node.getNumCells() + 1);
            pager.markDirty(currentPageNumber);
        }
    }

    private void splitAndInsert(int key, byte[] rowBytes) {

        /*
        Common steps (both cases):
            1. Allocate right page, copy upper half cells there
            2. Set right node headers (not root, sibling pointer)
            3. Trim current node to keep lower half only
            4. Insert the new cell into the correct node (left or right)
            5. Calculate separator key
         */

        // calculate splitIndex keeping +1 for the new cell that needs to get inserted
        int splitIndex = (node.getMaxCells() + 1)/2;

        // copy leafNode's upper half data to new rightNode
        int rightNodePageNumber = pager.allocatePage();
        byte[] rightNodePage = pager.getPage(rightNodePageNumber);
        int splitIndexCellOffset = node.getCellOffset(splitIndex);
        int cellSize = LeafNode.INT_BYTES + rowSize;
        System.arraycopy(pager.getPage(currentPageNumber), splitIndexCellOffset, rightNodePage, LeafNode.CELLS_START_OFFSET,
                (node.getNumCells() - splitIndex) * cellSize);

        // create rightNode and set its headers
        LeafNode rightNode = LeafNode.from(rightNodePage, rowSize);
        rightNode.setNodeType(NodeType.LEAF);
        rightNode.setNumCells(node.getMaxCells() - splitIndex);
        rightNode.setIsRoot(false);
        rightNode.setNextLeaf(node.getNextLeaf());

        // trim current node
        node.setNumCells(splitIndex);

        // insert new key in its actual node
        if (key < node.getKey(splitIndex-1)) {
            int cellIndex = node.findCell(key);
            node.insertCell(cellIndex, key, rowBytes);
            node.setNumCells(splitIndex + 1);
        } else {
            int cellIndex = rightNode.findCell(key);
            rightNode.insertCell(cellIndex, key, rowBytes);
            rightNode.setNumCells(node.getMaxCells() - splitIndex + 1);
        }

        int separatorKey = node.getKey(node.getNumCells() - 1);
        
        // -------------------------------- Common logic ends -------------------------------------------- //

        if (node.isRoot()) {
            // allocate a new page for the left child as well and this current page will be reused for the internal node

            // copy leafNode data to new leftNode
            int leftNodePageNumber = pager.allocatePage();
            byte[] leftNodePage = pager.getPage(leftNodePageNumber);
            System.arraycopy(pager.getPage(currentPageNumber), 0, leftNodePage, 0, PAGE_SIZE);

            // create leftNode and set its headers
            LeafNode leftNode = LeafNode.from(leftNodePage, rowSize);
            leftNode.setNodeType(NodeType.LEAF);
            leftNode.setIsRoot(false);
            leftNode.setParentPointer(startPage);
            leftNode.setNextLeaf(rightNodePageNumber);

            // create a new internal node
            InternalNode root = InternalNode.create(pager.getPage(startPage));
            root.setChildPtr(0, leftNodePageNumber);
            root.setRightChildPtr(rightNodePageNumber);
            root.setNumKeys(1);
            root.setKey(0, separatorKey);
            rightNode.setParentPointer(startPage);

            // mark all pages dirty
            pager.markDirty(startPage);
            pager.markDirty(leftNodePageNumber);
            pager.markDirty(rightNodePageNumber);
        } else {
            // get the root for the current page
            InternalNode parent = InternalNode.from(pager.getPage(node.getParentPointer()));
            parent.insertKeyAndChild(separatorKey, rightNodePageNumber);
            node.setNextLeaf(rightNodePageNumber);
            rightNode.setParentPointer(node.getParentPointer());

            // mark all pages dirty
            pager.markDirty(node.getParentPointer());
            pager.markDirty(currentPageNumber);
            pager.markDirty(rightNodePageNumber);
        }

    }

    public void advance() {
        cellIndex++;
        if (cellIndex == node.getNumCells()) {
            if (node.getNextLeaf() == 0) {
                endOfTable = true;
            } else {
                cellIndex = 0;
                currentPageNumber = node.getNextLeaf();
                node = LeafNode.from(pager.getPage(currentPageNumber), rowSize);
            }
        }
    }

    private void navigateToLeaf(int key) {
        int currentPageNumber = startPage;
        while (NodeType.values()[pager.getPage(currentPageNumber)[0]] == NodeType.INTERNAL) {
            InternalNode internalNode = InternalNode.from(pager.getPage(currentPageNumber));
            currentPageNumber = internalNode.findChildPtr(key);
        }
        this.currentPageNumber = currentPageNumber;
    }

    public int getKey() {
        return node.getKey(cellIndex);
    }

    public byte[] getValue() {
        return node.getValue(cellIndex);
    }

}
