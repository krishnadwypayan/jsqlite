package btree;

import java.nio.ByteBuffer;

/**
 * [Common Header]     6 bytes
 *  node_type          1 byte
 *  is_root            1 byte
 *  parent_pointer     4 bytes
 */
public abstract class Node {

    protected static final int INT_BYTES = 4;
    private static final int PARENT_POINTER_OFFSET = 2;
    private static final int NODE_HEADER_BYTES = 6;

    protected final byte[] page;

    protected Node(byte[] page) {
        this.page = page;
    }

    protected NodeType getNodeType() {
        return NodeType.values()[page[0]];
    }

    protected void setNodeType(NodeType nodeType) {
        page[0] = (byte) nodeType.ordinal();
    }

    protected boolean isRoot() {
        return page[1] == 1;
    }

    protected void setIsRoot(boolean isRoot) {
        page[1] = (byte) (isRoot ? 1 : 0);
    }

    protected int getParentPointer() {
        return ByteBuffer.wrap(page, PARENT_POINTER_OFFSET, INT_BYTES).getInt();
    }

    protected void setParentPointer(int parentPointer) {
        ByteBuffer.wrap(page, PARENT_POINTER_OFFSET, INT_BYTES).putInt(parentPointer);
    }

}
