package btree;

import java.nio.ByteBuffer;

/**
 * Leaf node layout (per page):
 *   [Common Header]     6 bytes
 *     node_type          1 byte
 *     is_root            1 byte
 *     parent_pointer     4 bytes
 *   [Leaf Header]       4 bytes
 *     num_cells          4 bytes
 *   [Cell 0]
 *     key                4 bytes (primary key value)
 *     value              row_size bytes (serialized row)
 *   [Cell 1]
 *     ...
 */
public class LeafNode extends Node {

    private static final int LEAF_HEADER_START_OFFSET = 6;
    private static final int CELLS_START_OFFSET = 10;

    private final int rowSize;

    public static LeafNode create(byte[] page, int rowSize) {
        LeafNode leafNode = new LeafNode(page, rowSize);
        leafNode.setNodeType(NodeType.LEAF);
        leafNode.setIsRoot(true);
        leafNode.setParentPointer(-1);
        return leafNode;
    }

    public static LeafNode from(byte[] page, int rowSize) {
        return new LeafNode(page, rowSize);
    }

    private LeafNode(byte[] page, int rowSize) {
        super(page);
        this.rowSize = rowSize;
    }

    public int getNumCells() {
        return ByteBuffer.wrap(page, LEAF_HEADER_START_OFFSET, INT_BYTES).getInt();
    }

    public void setNumCells(int count) {
        ByteBuffer.wrap(page, LEAF_HEADER_START_OFFSET, INT_BYTES).putInt(count);
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
        ByteBuffer.wrap(page, getCellOffset(cellIndex), INT_BYTES + rowSize).putInt(key).put(value);
    }

    private int getCellOffset(int cellIndex) {
        return CELLS_START_OFFSET + (cellIndex * (INT_BYTES + rowSize));
    }

}
