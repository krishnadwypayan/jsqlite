package command;

import org.junit.jupiter.api.Test;
import lexer.Lexer;
import lexer.Token;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SqlCommandTest {

    private final Lexer lexer = new Lexer();

    @Test
    void fromInputMatchesCreateTable() {
        List<Token> tokens = lexer.tokenize("create table users (id number);");
        Optional<SqlCommand> result = SqlCommand.fromInput(tokens);
        assertTrue(result.isPresent());
        assertEquals(SqlCommand.CREATE, result.get());
    }

    @Test
    void fromInputMatchesInsert() {
        List<Token> tokens = lexer.tokenize("insert into users 1 alice;");
        Optional<SqlCommand> result = SqlCommand.fromInput(tokens);
        assertTrue(result.isPresent());
        assertEquals(SqlCommand.INSERT, result.get());
    }

    @Test
    void fromInputMatchesSelect() {
        List<Token> tokens = lexer.tokenize("select;");
        Optional<SqlCommand> result = SqlCommand.fromInput(tokens);
        assertTrue(result.isPresent());
        assertEquals(SqlCommand.SELECT, result.get());
    }

    @Test
    void fromInputReturnsEmptyForUnknownCommand() {
        List<Token> tokens = lexer.tokenize("drop");
        Optional<SqlCommand> result = SqlCommand.fromInput(tokens);
        assertTrue(result.isEmpty());
    }
}
