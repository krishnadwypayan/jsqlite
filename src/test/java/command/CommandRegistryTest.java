package command;

import command.handler.CommandHandlerExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ParserException;

import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    private CommandRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
    }

    // --- DISPATCH BASICS ---

    @Test
    void nullInputReturnsUnrecognizedCommand() {
        assertEquals(CommandResult.UNRECOGNIZED_COMMAND, registry.dispatch(null));
    }

    @Test
    void blankInputReturnsUnrecognizedCommand() {
        assertEquals(CommandResult.UNRECOGNIZED_COMMAND, registry.dispatch("   "));
    }

    @Test
    void exitCommandReturnsExit() {
        assertEquals(CommandResult.EXIT, registry.dispatch(".exit"));
    }

    @Test
    void unknownMetaCommandReturnsUnrecognized() {
        assertEquals(CommandResult.UNRECOGNIZED_COMMAND, registry.dispatch(".unknown"));
    }

    // --- CREATE TABLE ---

    @Test
    void createTableReturnsSuccess() {
        CommandResult result = registry.dispatch("create table users (id number, name char(32))");
        assertEquals(CommandResult.PREPARE_SUCCESS, result);
    }

    @Test
    void createTableWithSemicolonReturnsSuccess() {
        CommandResult result = registry.dispatch("create table users (id number);");
        assertEquals(CommandResult.PREPARE_SUCCESS, result);
    }

    // --- INSERT ---

    @Test
    void insertReturnsSuccess() {
        registry.dispatch("create table users (id number, name char(32))");
        CommandResult result = registry.dispatch("insert into users values (1, 'alice')");
        assertEquals(CommandResult.PREPARE_SUCCESS, result);
    }

    @Test
    void insertIntoNonExistentTableThrows() {
        assertThrows(CommandHandlerExecutionException.class,
                () -> registry.dispatch("insert into users values (1, 'alice')"));
    }

    @Test
    void insertWrongValueCountThrows() {
        registry.dispatch("create table users (id number, name char(32))");
        assertThrows(CommandHandlerExecutionException.class,
                () -> registry.dispatch("insert into users values (1)"));
    }

    // --- SELECT ---

    @Test
    void selectStarReturnsSuccess() {
        registry.dispatch("create table users (id number, name char(32))");
        registry.dispatch("insert into users values (1, 'alice')");
        CommandResult result = registry.dispatch("select * from users");
        assertEquals(CommandResult.PREPARE_SUCCESS, result);
    }

    @Test
    void selectColumnsReturnsSuccess() {
        registry.dispatch("create table users (id number, name char(32))");
        registry.dispatch("insert into users values (1, 'alice')");
        CommandResult result = registry.dispatch("select id from users");
        assertEquals(CommandResult.PREPARE_SUCCESS, result);
    }

    @Test
    void selectFromNonExistentTableThrows() {
        assertThrows(CommandHandlerExecutionException.class,
                () -> registry.dispatch("select * from users"));
    }

    // --- FULL PIPELINE ---

    @Test
    void fullPipelineCreateInsertSelect() {
        assertEquals(CommandResult.PREPARE_SUCCESS,
                registry.dispatch("create table users (id number, name char(32), age number)"));
        assertEquals(CommandResult.PREPARE_SUCCESS,
                registry.dispatch("insert into users values (1, 'alice', 25)"));
        assertEquals(CommandResult.PREPARE_SUCCESS,
                registry.dispatch("insert into users values (2, 'bob', 30)"));
        assertEquals(CommandResult.PREPARE_SUCCESS,
                registry.dispatch("select * from users"));
        assertEquals(CommandResult.PREPARE_SUCCESS,
                registry.dispatch("select id, name from users"));
    }

    @Test
    void multipleTablesWork() {
        registry.dispatch("create table users (id number, name char(32))");
        registry.dispatch("create table posts (id number, title varchar(100))");
        registry.dispatch("insert into users values (1, 'alice')");
        registry.dispatch("insert into posts values (1, 'hello world')");
        assertEquals(CommandResult.PREPARE_SUCCESS, registry.dispatch("select * from users"));
        assertEquals(CommandResult.PREPARE_SUCCESS, registry.dispatch("select * from posts"));
    }

    // --- PARSE ERRORS ---

    @Test
    void invalidSqlThrows() {
        assertThrows(ParserException.class,
                () -> registry.dispatch("gibberish"));
    }
}
