package store;

import btree.BTree;
import btree.InternalNode;
import btree.LeafNode;
import btree.NodeType;
import lombok.Getter;

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
        new BTree(pager, startPage, rowSize).insert(key, rowBytes);
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

    public int getKey() {
        return node.getKey(cellIndex);
    }

    public byte[] getValue() {
        return node.getValue(cellIndex);
    }

}
