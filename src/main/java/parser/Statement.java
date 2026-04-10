package parser;

public sealed interface Statement permits CreateTableStatement, InsertStatement, SelectStatement {
}
