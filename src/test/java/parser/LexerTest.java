package parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    private final Lexer lexer = new Lexer();

    @Test
    void tokenizeCreateTableStatement() {
        List<Token> tokens = lexer.tokenize("create table users (id number, name char(32))");
        assertEquals(List.of(
                new Token(TokenType.KEYWORD, "create"),
                new Token(TokenType.KEYWORD, "table"),
                new Token(TokenType.IDENTIFIER, "users"),
                new Token(TokenType.LEFT_PARENTHESIS, "("),
                new Token(TokenType.IDENTIFIER, "id"),
                new Token(TokenType.KEYWORD, "number"),
                new Token(TokenType.COMMA, ","),
                new Token(TokenType.IDENTIFIER, "name"),
                new Token(TokenType.KEYWORD, "char"),
                new Token(TokenType.LEFT_PARENTHESIS, "("),
                new Token(TokenType.NUMBER_LITERAL, "32"),
                new Token(TokenType.RIGHT_PARENTHESIS, ")"),
                new Token(TokenType.RIGHT_PARENTHESIS, ")")
        ), tokens);
    }

    @Test
    void tokenizeInsertStatement() {
        List<Token> tokens = lexer.tokenize("insert into users 1 alice");
        assertEquals(List.of(
                new Token(TokenType.KEYWORD, "insert"),
                new Token(TokenType.KEYWORD, "into"),
                new Token(TokenType.IDENTIFIER, "users"),
                new Token(TokenType.NUMBER_LITERAL, "1"),
                new Token(TokenType.IDENTIFIER, "alice")
        ), tokens);
    }

    @Test
    void tokenizeSelectStatement() {
        List<Token> tokens = lexer.tokenize("select");
        assertEquals(List.of(new Token(TokenType.KEYWORD, "select")), tokens);
    }

    @Test
    void caseInsensitiveKeywords() {
        List<Token> upper = lexer.tokenize("CREATE");
        List<Token> lower = lexer.tokenize("create");
        assertEquals(TokenType.KEYWORD, upper.get(0).tokenType());
        assertEquals(TokenType.KEYWORD, lower.get(0).tokenType());
    }

    @Test
    void symbolsAdjacentToWords() {
        List<Token> tokens = lexer.tokenize("name,");
        assertEquals(List.of(
                new Token(TokenType.IDENTIFIER, "name"),
                new Token(TokenType.COMMA, ",")
        ), tokens);
    }

    @Test
    void numberLiteral() {
        List<Token> tokens = lexer.tokenize("32");
        assertEquals(List.of(new Token(TokenType.NUMBER_LITERAL, "32")), tokens);
    }

    @Test
    void identifierWithDigitsAndUnderscores() {
        List<Token> tokens = lexer.tokenize("user_1");
        assertEquals(List.of(new Token(TokenType.IDENTIFIER, "user_1")), tokens);
    }

    @Test
    void emptyInput() {
        assertTrue(lexer.tokenize("").isEmpty());
    }

    @Test
    void whitespaceOnlyInput() {
        assertTrue(lexer.tokenize("   ").isEmpty());
    }

    @Test
    void semicolonAtEndOfStatement() {
        List<Token> tokens = lexer.tokenize("select;");
        assertEquals(List.of(
                new Token(TokenType.KEYWORD, "select"),
                new Token(TokenType.SEMICOLON, ";")
        ), tokens);
    }

    @Test
    void createTableWithSemicolon() {
        List<Token> tokens = lexer.tokenize("create table users(id number, name varchar(30));");
        assertEquals(TokenType.KEYWORD, tokens.get(0).tokenType());
        assertEquals("create", tokens.get(0).value());
        assertEquals(TokenType.KEYWORD, tokens.get(1).tokenType());
        assertEquals("table", tokens.get(1).value());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).tokenType());
        assertEquals(TokenType.SEMICOLON, tokens.get(tokens.size() - 1).tokenType());
    }

    @Test
    void unsupportedCharacterThrows() {
        assertThrows(UnsupportedOperationException.class, () -> lexer.tokenize("@"));
    }
}
