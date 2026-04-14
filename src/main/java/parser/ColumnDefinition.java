package parser;

public record ColumnDefinition(String name, String type, int size, boolean primaryKey) {
}
