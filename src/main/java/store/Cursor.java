package store;

import btree.LeafNode;
import lombok.Getter;

public class Cursor {

    private final Pager pager;
    private final int startPage;
    private final LeafNode leafNode;
    private int cellIndex;

    @Getter
    private boolean endOfTable;

    public Cursor(Pager pager, int startPage, int rowSize) {
        this.leafNode = LeafNode.from(pager.getPage(startPage), rowSize);
        this.pager = pager;
        this.startPage = startPage;
        endOfTable = (leafNode.getNumCells() == 0);
    }

    public void insert(int key, byte[] rowBytes) {
        LeafNode leafNode = seekToEnd();
        leafNode.insertCell(cellIndex, key, rowBytes);
        leafNode.setNumCells(cellIndex+1);
        pager.markDirty(startPage);
    }

    public void advance() {
        cellIndex++;
        if (cellIndex == leafNode.getNumCells()) {
            endOfTable = true;
        }
    }

    private LeafNode seekToEnd() {
        cellIndex = leafNode.getNumCells();
        endOfTable = true;
        return leafNode;
    }

    public int getKey() {
        return leafNode.getKey(cellIndex);
    }

    public byte[] getValue() {
        return leafNode.getValue(cellIndex);
    }

}
