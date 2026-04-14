package store;

public record Column(String name, ColumnType type, int size, boolean primaryKey) {
}
