package btree;

import store.Pager;
import store.StorageException;

import static store.DatabaseConstants.PAGE_SIZE;

public class BTree {

    private final Pager pager;
    private final int rootPageNumber;
    private final int rowSize;

    public BTree(Pager pager, int rootPageNumber, int rowSize) {
        this.pager = pager;
        this.rootPageNumber = rootPageNumber;
        this.rowSize = rowSize;
    }

    public void insert(int key, byte[] rowBytes) {
        int leafPageNumber = navigateToLeaf(key);
        LeafNode leafNode = LeafNode.from(pager.getPage(leafPageNumber), rowSize);
        int cellIndex = leafNode.findCell(key);

        // check duplicate
        if (cellIndex < leafNode.getNumCells() && leafNode.getKey(cellIndex) == key) {
            throw new StorageException("Duplicate key: " + key);
        }

        if (leafNode.getMaxCells() == leafNode.getNumCells()) {
            splitLeaf(leafPageNumber, key, rowBytes);
        } else {
            leafNode.insertCell(cellIndex, key, rowBytes);
            leafNode.setNumCells(leafNode.getNumCells() + 1);
            pager.markDirty(leafPageNumber);
        }
    }

    private void splitLeaf(int leafPageNumber, int key, byte[] rowBytes) {

        LeafNode node = LeafNode.from(pager.getPage(leafPageNumber), rowSize);

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
        System.arraycopy(pager.getPage(leafPageNumber), splitIndexCellOffset, rightNodePage, LeafNode.CELLS_START_OFFSET,
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
            System.arraycopy(pager.getPage(leafPageNumber), 0, leftNodePage, 0, PAGE_SIZE);

            // create leftNode and set its headers
            LeafNode leftNode = LeafNode.from(leftNodePage, rowSize);
            leftNode.setNodeType(NodeType.LEAF);
            leftNode.setIsRoot(false);
            leftNode.setParentPointer(rootPageNumber);
            leftNode.setNextLeaf(rightNodePageNumber);

            // create a new internal node
            InternalNode root = InternalNode.create(pager.getPage(rootPageNumber));
            root.setChildPtr(0, leftNodePageNumber);
            root.setRightChildPtr(rightNodePageNumber);
            root.setNumKeys(1);
            root.setKey(0, separatorKey);
            rightNode.setParentPointer(rootPageNumber);

            // mark all pages dirty
            pager.markDirty(rootPageNumber);
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
            pager.markDirty(leafPageNumber);
            pager.markDirty(rightNodePageNumber);
        }

    }

    private int navigateToLeaf(int key) {
        int currentPageNumber = rootPageNumber;
        while (NodeType.values()[pager.getPage(currentPageNumber)[0]] == NodeType.INTERNAL) {
            InternalNode internalNode = InternalNode.from(pager.getPage(currentPageNumber));
            currentPageNumber = internalNode.findChildPtr(key);
        }
        return currentPageNumber;
    }

}
