package parser;

import lexer.Lexer;
import lexer.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private final Lexer lexer = new Lexer();

    private Statement parse(String input) {
        List<Token> tokens = lexer.tokenize(input);
        return new Parser(tokens).parse();
    }

    // --- CREATE TABLE ---

    @Test
    void parseCreateTable() {
        Statement stmt = parse("create table users (id number, name char(32))");
        assertInstanceOf(CreateTableStatement.class, stmt);
        CreateTableStatement create = (CreateTableStatement) stmt;
        assertEquals("users", create.tableName());
        assertEquals(2, create.columnDefinitions().size());
        assertEquals(new ColumnDefinition("id", "number", 0), create.columnDefinitions().get(0));
        assertEquals(new ColumnDefinition("name", "char", 32), create.columnDefinitions().get(1));
    }

    @Test
    void parseCreateTableWithVarchar() {
        Statement stmt = parse("create table posts (title varchar(255), body varchar(1000))");
        CreateTableStatement create = (CreateTableStatement) stmt;
        assertEquals("posts", create.tableName());
        assertEquals(new ColumnDefinition("title", "varchar", 255), create.columnDefinitions().get(0));
        assertEquals(new ColumnDefinition("body", "varchar", 1000), create.columnDefinitions().get(1));
    }

    @Test
    void parseCreateTableWithSemicolon() {
        Statement stmt = parse("create table users (id number);");
        CreateTableStatement create = (CreateTableStatement) stmt;
        assertEquals("users", create.tableName());
        assertEquals(1, create.columnDefinitions().size());
    }

    @Test
    void parseCreateTableThreeColumns() {
        Statement stmt = parse("create table users (id number, name char(32), age number)");
        CreateTableStatement create = (CreateTableStatement) stmt;
        assertEquals(3, create.columnDefinitions().size());
        assertEquals(new ColumnDefinition("age", "number", 0), create.columnDefinitions().get(2));
    }

    // --- INSERT ---

    @Test
    void parseInsert() {
        Statement stmt = parse("insert into users values (1, 'alice', 25)");
        assertInstanceOf(InsertStatement.class, stmt);
        InsertStatement insert = (InsertStatement) stmt;
        assertEquals("users", insert.tableName());
        assertEquals(List.of(1, "alice", 25), insert.values());
    }

    @Test
    void parseInsertWithSemicolon() {
        Statement stmt = parse("insert into users values (1, 'alice');");
        InsertStatement insert = (InsertStatement) stmt;
        assertEquals("users", insert.tableName());
        assertEquals(List.of(1, "alice"), insert.values());
    }

    @Test
    void parseInsertStringWithSpaces() {
        Statement stmt = parse("insert into users values (1, 'alice wonderland')");
        InsertStatement insert = (InsertStatement) stmt;
        assertEquals(List.of(1, "alice wonderland"), insert.values());
    }

    // --- SELECT ---

    @Test
    void parseSelectStar() {
        Statement stmt = parse("select * from users");
        assertInstanceOf(SelectStatement.class, stmt);
        SelectStatement select = (SelectStatement) stmt;
        assertEquals("users", select.tableName());
        assertEquals(List.of("*"), select.columns());
    }

    @Test
    void parseSelectColumns() {
        Statement stmt = parse("select id, name from users");
        SelectStatement select = (SelectStatement) stmt;
        assertEquals("users", select.tableName());
        assertEquals(List.of("id", "name"), select.columns());
    }

    @Test
    void parseSelectSingleColumn() {
        Statement stmt = parse("select name from users");
        SelectStatement select = (SelectStatement) stmt;
        assertEquals(List.of("name"), select.columns());
    }

    @Test
    void parseSelectStarWithSemicolon() {
        Statement stmt = parse("select * from users;");
        SelectStatement select = (SelectStatement) stmt;
        assertEquals("users", select.tableName());
        assertEquals(List.of("*"), select.columns());
    }

    // --- CASE INSENSITIVITY ---

    @Test
    void parseUpperCaseKeywords() {
        Statement stmt = parse("CREATE TABLE users (id number)");
        assertInstanceOf(CreateTableStatement.class, stmt);
    }

    @Test
    void parseMixedCaseKeywords() {
        Statement stmt = parse("Insert Into users Values (1, 'alice')");
        assertInstanceOf(InsertStatement.class, stmt);
    }

    // --- ERROR CASES ---

    @Test
    void emptyInputThrows() {
        assertThrows(Exception.class, () -> parse(""));
    }

    @Test
    void missingTableNameThrows() {
        assertThrows(ParserException.class, () -> parse("create table"));
    }

    @Test
    void missingParenthesisThrows() {
        assertThrows(ParserException.class, () -> parse("create table users id number"));
    }

    @Test
    void insertMissingValuesKeywordThrows() {
        assertThrows(ParserException.class, () -> parse("insert into users (1, 'alice')"));
    }

    @Test
    void unrecognizedKeywordThrows() {
        assertThrows(Exception.class, () -> parse("drop table users"));
    }
}
