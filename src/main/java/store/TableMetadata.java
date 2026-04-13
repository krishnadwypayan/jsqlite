package store;

import java.util.List;

public record TableMetadata(String tableName, int startPage, List<Column> columns) {
}
