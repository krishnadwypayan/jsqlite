package btree;

import java.nio.ByteBuffer;

/**
 * [Common Header]     6 bytes
 *  node_type          1 byte
 *  is_root            1 byte
 *  parent_pointer     4 bytes
 */
public abstract class Node {

    public static final int INT_BYTES = 4;
    private static final int PARENT_POINTER_OFFSET = 2;
    protected static final int COMMON_HEADER_END_OFFSET = 6;

    protected final byte[] page;

    protected Node(byte[] page) {
        this.page = page;
    }

    public NodeType getNodeType() {
        return NodeType.values()[page[0]];
    }

    public void setNodeType(NodeType nodeType) {
        page[0] = (byte) nodeType.ordinal();
    }

    public boolean isRoot() {
        return page[1] == 1;
    }

    public void setIsRoot(boolean isRoot) {
        page[1] = (byte) (isRoot ? 1 : 0);
    }

    public int getParentPointer() {
        return ByteBuffer.wrap(page, PARENT_POINTER_OFFSET, INT_BYTES).getInt();
    }

    public void setParentPointer(int parentPointer) {
        ByteBuffer.wrap(page, PARENT_POINTER_OFFSET, INT_BYTES).putInt(parentPointer);
    }

}
