package store;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * [table_count]
 * [table_name_len][table_name][start_page][column_count]
 * [col_name_len][col_name][col_type][col_size]
 * ...
 */
public class SchemaSerializer {

    public void serialize(List<TableMetadata> tableMetadataList, byte[] page) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(page);

        int tableCount = tableMetadataList.size();
        byteBuffer.putInt(tableCount);

        for (TableMetadata tableMetadata : tableMetadataList) {
            byteBuffer.putInt(tableMetadata.tableName().length());
            byteBuffer.put(tableMetadata.tableName().getBytes(StandardCharsets.UTF_8));
            byteBuffer.putInt(tableMetadata.startPage());
            byteBuffer.putInt(tableMetadata.columns().size());
            for (Column column : tableMetadata.columns()) {
                byteBuffer.putInt(column.name().length());
                byteBuffer.put(column.name().getBytes(StandardCharsets.UTF_8));
                byteBuffer.putInt(column.type().ordinal());
                byteBuffer.putInt(column.size());
                byteBuffer.putInt(column.primaryKey() ? 1 : 0);
            }
        }
    }

    public List<TableMetadata> deserialize(byte[] page) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(page);
        int tableCount = byteBuffer.getInt();
        List<TableMetadata> tableMetadataList = new ArrayList<>();
        for (int i = 0; i < tableCount; i++) {
            int tableNameLength = byteBuffer.getInt();
            byte[] tableNameBytes = new byte[tableNameLength];
            byteBuffer.get(tableNameBytes);

            int startPage = byteBuffer.getInt();
            int numColumns = byteBuffer.getInt();

            List<Column> columns = new ArrayList<>();
            for (int j = 0; j < numColumns; j++) {
                int columnNameLength = byteBuffer.getInt();
                byte[] columnNameBytes = new byte[columnNameLength];
                byteBuffer.get(columnNameBytes);
                int columnTypeOrdinal = byteBuffer.getInt();
                int columnSize = byteBuffer.getInt();
                boolean primaryKey = byteBuffer.getInt() == 1;
                columns.add(new Column(new String(columnNameBytes, StandardCharsets.UTF_8), ColumnType.values()[columnTypeOrdinal], columnSize, primaryKey));
            }
            tableMetadataList.add(new TableMetadata(new String(tableNameBytes, StandardCharsets.UTF_8), startPage, columns));
        }
        return tableMetadataList;
    }

}
