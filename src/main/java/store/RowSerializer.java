package store;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class RowSerializer {

    private int rowSize;
    private final List<Column> columns;
    private final Map<String, Integer> offsets;

    public RowSerializer(List<Column> columns) {
        this.columns = columns;
        offsets = initOffsets(columns);
    }

    private Map<String, Integer> initOffsets(List<Column> columns) {
        Map<String, Integer> offsets = new HashMap<>();
        int startingOffset = 0;
        for (Column column : CollectionUtils.emptyIfNull(columns)) {
            offsets.put(column.name(), startingOffset);
            startingOffset += column.size();
        }
        this.rowSize = startingOffset;
        return offsets;
    }

    public void serialize(List<ColumnValue> columnValues, byte[] page, int offset) {
        ByteBuffer rowBuf = ByteBuffer.wrap(page, offset, rowSize);
        CollectionUtils.emptyIfNull(columnValues).forEach(columnValue -> {
            Column column = columnValue.column();
            Integer colOffset = offsets.get(column.name());
            if (colOffset == null) {
                throw new IllegalArgumentException("invalid column name: " + column.name());
            }

            rowBuf.position(offset + colOffset);
            switch (column.type()) {
                case NUMBER -> rowBuf.putInt((Integer) columnValue.value());
                case CHAR,VARCHAR -> rowBuf.put(padToFixedSize((String) columnValue.value(), column.size()));
            }
        });
    }

    public List<ColumnValue> deserialize(byte[] page, int offset) {
        List<ColumnValue> columnValues = new ArrayList<>();
        ByteBuffer rowBuffer = ByteBuffer.wrap(page, offset, rowSize);
        CollectionUtils.emptyIfNull(columns).forEach(column -> {
            Integer columnOffset = offsets.get(column.name());
            if (columnOffset == null) {
                throw new IllegalArgumentException("invalid column name: " + column.name());
            }

            rowBuffer.position(offset + columnOffset);
            switch (column.type()) {
                case NUMBER -> columnValues.add(new ColumnValue(column, rowBuffer.getInt()));
                case CHAR, VARCHAR -> {
                    byte[] bytes = new byte[column.size()];
                    rowBuffer.get(bytes);
                    columnValues.add(new ColumnValue(column, getStrippedString(bytes, column.size())));
                }
            }
        });
        return columnValues;
    }

    public void serializeRowCount(byte[] page, int count) {
        ByteBuffer buffer = ByteBuffer.wrap(page);
        buffer.putInt(count);
    }

    private byte[] padToFixedSize(String value, int size) {
        byte[] src = value.getBytes(StandardCharsets.UTF_8);
        byte[] dest = new byte[size];
        System.arraycopy(src, 0, dest, 0, Math.min(src.length, size));
        return dest;
    }

    private String getStrippedString(byte[] bytes, int size) {
        int i = 0;
        while (i < size && bytes[i] != 0x00) {
            i++;
        }
        return new String(bytes, 0, i);
    }
}
