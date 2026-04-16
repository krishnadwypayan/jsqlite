package btree;

import lombok.Getter;

import java.nio.ByteBuffer;

import static store.DatabaseConstants.PAGE_SIZE;

/**
 * Leaf node layout (per page):
 *   [Common Header]     6 bytes
 *     node_type          1 byte
 *     is_root            1 byte
 *     parent_pointer     4 bytes
 *   [Leaf Header]       8 bytes
 *     num_cells          4 bytes
 *     next_leaf          4 bytes
 *   [Cell 0]
 *     key                4 bytes (primary key value)
 *     value              row_size bytes (serialized row)
 *   [Cell 1]
 *     ...
 */
public class LeafNode extends Node {

    public static final int CELLS_START_OFFSET = 14;

    private final int rowSize;

    @Getter
    private final int maxCells;

    public static LeafNode create(byte[] page, int rowSize) {
        LeafNode leafNode = new LeafNode(page, rowSize);
        leafNode.setNodeType(NodeType.LEAF);
        leafNode.setIsRoot(true);
        leafNode.setParentPointer(0);
        leafNode.setNextLeaf(0);
        return leafNode;
    }

    public static LeafNode from(byte[] page, int rowSize) {
        return new LeafNode(page, rowSize);
    }

    private LeafNode(byte[] page, int rowSize) {
        super(page);
        this.rowSize = rowSize;
        this.maxCells = (PAGE_SIZE - CELLS_START_OFFSET)/(4 + rowSize);
    }

    public int getNumCells() {
        return ByteBuffer.wrap(page, COMMON_HEADER_END_OFFSET, INT_BYTES).getInt();
    }

    public void setNumCells(int count) {
        ByteBuffer.wrap(page, COMMON_HEADER_END_OFFSET, INT_BYTES).putInt(count);
    }

    public int getNextLeaf() {
        return ByteBuffer.wrap(page, COMMON_HEADER_END_OFFSET + INT_BYTES, INT_BYTES).getInt();
    }

    public void setNextLeaf(int count) {
        ByteBuffer.wrap(page, COMMON_HEADER_END_OFFSET + INT_BYTES, INT_BYTES).putInt(count);
    }

    public int getKey(int cellIndex) {
        return ByteBuffer.wrap(page, getCellOffset(cellIndex), INT_BYTES).getInt();
    }

    public byte[] getValue(int cellIndex) {
        byte[] value = new byte[rowSize];
        ByteBuffer.wrap(page, getCellOffset(cellIndex) + INT_BYTES, rowSize).get(value);
        return value;
    }

    public void insertCell(int cellIndex, int key, byte[] value) {
        int numCells = getNumCells();
        int cellSize = INT_BYTES + rowSize;

        // shift cells right by one position
        int srcOffset = getCellOffset(cellIndex);
        int destOffset = getCellOffset(cellIndex + 1);
        int bytesToMove = (numCells - cellIndex) * cellSize;
        System.arraycopy(page, srcOffset, page, destOffset, bytesToMove);

        // write new cell
        ByteBuffer.wrap(page, srcOffset, cellSize).putInt(key).put(value);
    }

    public int getCellOffset(int cellIndex) {
        return CELLS_START_OFFSET + (cellIndex * (INT_BYTES + rowSize));
    }

    public int findCell(int key) {
        int low = 0, high = getNumCells();
        while (low < high) {
            int mid = low + (high - low)/2;
            if (getKey(mid) < key) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

}
