package parser;

import java.util.List;

public record SelectStatement(String tableName, List<String> columns) implements Statement {
}
