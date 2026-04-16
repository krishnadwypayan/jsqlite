package btree;

import java.nio.ByteBuffer;

/**
 * [Common Header]     6 bytes
 *   [Internal Header]   4 bytes (num_keys)
 *   [Right child ptr]   4 bytes (page number of rightmost child)
 *   [Cell 0]
 *     child_ptr         4 bytes (page number of left child)
 *     key               4 bytes
 *   [Cell 1]
 *     ...
 */
public class InternalNode extends Node {

    private static final int RIGHT_CHILD_PTR_OFFSET = COMMON_HEADER_END_OFFSET + INT_BYTES;
    private static final int CELL_START_OFFSET = RIGHT_CHILD_PTR_OFFSET + INT_BYTES;
    private static final int CELL_SIZE = 2 * INT_BYTES;

    public static InternalNode create(byte[] page) {
        InternalNode node = new InternalNode(page);
        node.setNodeType(NodeType.INTERNAL);
        node.setIsRoot(true);
        node.setParentPointer(-1);
        node.setNumKeys(0);
        node.setRightChildPtr(-1);
        return node;
    }

    public static InternalNode from(byte[] page) {
        return new InternalNode(page);
    }

    private InternalNode(byte[] page) {
        super(page);
    }

    public int getNumKeys() {
        return ByteBuffer.wrap(page, COMMON_HEADER_END_OFFSET, INT_BYTES).getInt();
    }

    public void setNumKeys(int count) {
        ByteBuffer.wrap(page, COMMON_HEADER_END_OFFSET, INT_BYTES).putInt(count);
    }

    public int getRightChildPtr() {
        return ByteBuffer.wrap(page, RIGHT_CHILD_PTR_OFFSET, INT_BYTES).getInt();
    }

    public void setRightChildPtr(int rightChildPtr) {
        ByteBuffer.wrap(page, RIGHT_CHILD_PTR_OFFSET, INT_BYTES).putInt(rightChildPtr);
    }

    public int getChildPtr(int cellIndex) {
        return ByteBuffer.wrap(page, CELL_START_OFFSET + (cellIndex * CELL_SIZE), INT_BYTES).getInt();
    }

    public void setChildPtr(int cellIndex, int childPtr) {
        ByteBuffer.wrap(page, CELL_START_OFFSET + (cellIndex * CELL_SIZE), INT_BYTES).putInt(childPtr);
    }

    public int getKey(int cellIndex) {
        return ByteBuffer.wrap(page, CELL_START_OFFSET + (cellIndex * CELL_SIZE) + INT_BYTES, INT_BYTES).getInt();
    }

    public void setKey(int cellIndex, int key) {
        ByteBuffer.wrap(page, CELL_START_OFFSET + (cellIndex * CELL_SIZE) + INT_BYTES, INT_BYTES).putInt(key);
    }

    public int findChild(int key) {
        int low = 0, high = getNumKeys();
        while (low < high) {
            int mid = low + (high - low)/2;
            if (getKey(mid) < key) {
                low = mid+1;
            } else {
                high = mid;
            }
        }

        if (low < getNumKeys() && key <= getKey(low)) {
            return getChildPtr(low);
        }
        return getRightChildPtr();
    }

}
