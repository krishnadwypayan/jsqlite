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
        int cellIndex = leafNode.findCell(key);

        // check duplicate
        if (cellIndex < leafNode.getNumCells() && leafNode.getKey(cellIndex) == key) {
            throw new StorageException("Duplicate key: " + key);
        }

        leafNode.insertCell(cellIndex, key, rowBytes);
        leafNode.setNumCells(leafNode.getNumCells() + 1);
        pager.markDirty(startPage);
    }

    public void advance() {
        cellIndex++;
        if (cellIndex == leafNode.getNumCells()) {
            endOfTable = true;
        }
    }

    private void seekToEnd() {
        cellIndex = leafNode.getNumCells();
        endOfTable = true;
    }

    public int getKey() {
        return leafNode.getKey(cellIndex);
    }

    public byte[] getValue() {
        return leafNode.getValue(cellIndex);
    }

}
