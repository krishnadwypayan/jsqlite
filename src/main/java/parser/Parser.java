package parser;

import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private final int numTokens;
    private int currentIndex;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.numTokens = tokens.size();
    }

    public Statement parse() {
        Token token = advance();
        if (token.tokenType() != TokenType.KEYWORD) {
            throw new ParserException("Keyword expected at the start of the statement");
        }

        return switch (token.value().toLowerCase()) {
            case "create" -> parseCreateTable();
            case "select" -> parseSelectTable();
            case "insert" -> parseInsertTable();
            default -> throw new IllegalStateException("Unexpected value: " + token.value());
        };
    }

    /**
     * sample query:
     *   select * from users
     * Or with specific columns:
     *   select id, name from users
     * @return
     */
    private SelectStatement parseSelectTable() {
        Token token = peek();
        if (token == null) {
            throw new ParserException("Unexpected end of tokens reached");
        }

        List<String> columnNames = new ArrayList<>();
        if (token.tokenType() == TokenType.STAR) {
            columnNames.add(advance().value());
        } else {
            do {
                columnNames.add(advance().value());
            } while (hasComma());
        }
        expect(TokenType.KEYWORD, "from");
        Token tableNameToken = expectIdentifier("table name");
        expectEnd();
        return new SelectStatement(tableNameToken.value(), columnNames);
    }

    /**
     * sample query: create table users (id number, name char(32), age number);
     * @return
     */
    private CreateTableStatement parseCreateTable() {
        expect(TokenType.KEYWORD, "table");
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();

        Token tableNameToken = expectIdentifier("table name");
        expect(TokenType.LEFT_PARENTHESIS, "(");
        do {
            Token columnName = advance();
            Token columnType = advance();
            int size = 0;
            if (columnType.value().equalsIgnoreCase("char") || columnType.value().equalsIgnoreCase("varchar")) {
                expect(TokenType.LEFT_PARENTHESIS, "(");
                Token sizeToken = advance();
                if (sizeToken.tokenType() != TokenType.NUMBER_LITERAL) {
                    throw new ParserException("Expected size for char token");
                }
                size = Integer.parseInt(sizeToken.value());
                expect(TokenType.RIGHT_PARENTHESIS, ")");
            }

            Token primaryKeyToken = peek();
            boolean primaryKey = false;
            if (primaryKeyToken != null && primaryKeyToken.tokenType() == TokenType.KEYWORD &&
                    primaryKeyToken.value().equalsIgnoreCase("primary")) {
                primaryKey = true;
                expect(TokenType.KEYWORD, "primary");
                expect(TokenType.KEYWORD, "key");
            }
            columnDefinitions.add(new ColumnDefinition(columnName.value(), columnType.value(), size, primaryKey));

        } while (hasComma());
        expect(TokenType.RIGHT_PARENTHESIS, ")");
        expectEnd();

        if (columnDefinitions.stream().filter(ColumnDefinition::primaryKey).count() > 1) {
            throw new ParserException("Table can have a single primary key");
        }

        return new CreateTableStatement(tableNameToken.value(), columnDefinitions);
    }

    /**
     * sample query: insert into users values (1, 'alice', 25);
     * @return
     */
    private InsertStatement parseInsertTable() {
        expect(TokenType.KEYWORD, "into");
        List<Object> values = new ArrayList<>();

        Token tableNameToken = expectIdentifier("table name");

        expect(TokenType.KEYWORD, "values");
        expect(TokenType.LEFT_PARENTHESIS, "(");

        do {
            Token valueToken = advance();
            if (valueToken.tokenType() == TokenType.NUMBER_LITERAL) {
                values.add(Integer.parseInt(valueToken.value()));
            } else if (valueToken.tokenType() == TokenType.STRING_LITERAL) {
                values.add(valueToken.value());
            } else {
                throw new ParserException("Expected number or string literal");
            }

        } while (hasComma());
        expect(TokenType.RIGHT_PARENTHESIS, ")");
        expectEnd();
        return new InsertStatement(tableNameToken.value(), values);
    }

    // ------ HELPER METHODS ----------- //

    private boolean hasComma() {
        Token token = peek();
        if (token != null && token.tokenType() == TokenType.COMMA) {
            advance();
            return true;
        }
        return false;
    }

    private void expectEnd() {
        if (currentIndex < numTokens) {
            expect(TokenType.SEMICOLON, ";");
        }
        if (currentIndex != numTokens) {
            throw new ParserException("Unexpected tokens after statement");
        }
    }

    private Token expectIdentifier(String context) {
        Token token = advance();
        if (token.tokenType() != TokenType.IDENTIFIER) {
            throw new ParserException("Expected " + context);
        }
        return token;
    }

    private Token advance() {
        if (currentIndex < numTokens) {
            Token token = peek();
            currentIndex++;
            return token;
        }
        throw new ParserException("No more tokens found");
    }

    private Token peek() {
        return currentIndex < numTokens ? tokens.get(currentIndex) : null;
    }

    private Token expect(TokenType tokenType, String value) {
        Token token = peek();
        if (token == null || token.tokenType() != tokenType || !token.value().equalsIgnoreCase(value)) {
            throw new ParserException(String.format("Expected '%s:%s' token", tokenType.name(), value));
        }
        currentIndex++;
        return token;
    }

}
