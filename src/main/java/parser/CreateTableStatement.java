package parser;

import java.util.List;

public record CreateTableStatement(String tableName, List<ColumnDefinition> columnDefinitions) implements Statement {
}
