package parser;

import java.util.List;

public record InsertStatement(String tableName, List<Object> values) implements Statement {
}
