package command;

import command.handler.CreateTableCommandHandler;
import command.handler.ExitCommandHandler;
import command.handler.InsertCommandHandler;
import command.handler.SelectCommandHandler;
import lexer.Lexer;
import lexer.Token;
import parser.CreateTableStatement;
import parser.InsertStatement;
import parser.Parser;
import parser.SelectStatement;
import parser.Statement;
import store.Database;
import store.DatabaseConstants;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {

    private final Database database;
    private final Lexer lexer;
    private final Map<MetaCommand, MetaCommandHandler> metaCommands = new EnumMap<>(MetaCommand.class);

    private final SelectCommandHandler selectCommandHandler;
    private final InsertCommandHandler insertCommandHandler;
    private final CreateTableCommandHandler createTableCommandHandler;

    public CommandRegistry() {
        this(DatabaseConstants.DATABASE_FILE_PATH);
    }

    public CommandRegistry(String databaseFilePath) {
        lexer = new Lexer();
        database = new Database(databaseFilePath);
        metaCommands.put(MetaCommand.EXIT, new ExitCommandHandler());
        selectCommandHandler = new SelectCommandHandler(database);
        insertCommandHandler = new InsertCommandHandler(database);
        createTableCommandHandler = new CreateTableCommandHandler(database);
    }

    public CommandResult dispatch(String input) {
        if (input == null || input.isBlank()) {
            return CommandResult.UNRECOGNIZED_COMMAND;
        }

        if (input.startsWith(".")) {
            return MetaCommand.fromInput(input)
                    .map(metaCommands::get)
                    .map(h -> h.execute(input))
                    .orElse(CommandResult.UNRECOGNIZED_COMMAND);
        }

        List<Token> tokens = lexer.tokenize(input);
        Statement statement = new Parser(tokens).parse();
        return switch (statement) {
            case CreateTableStatement ignored -> createTableCommandHandler.execute(statement);
            case InsertStatement ignored -> insertCommandHandler.execute(statement);
            case SelectStatement ignored -> selectCommandHandler.execute(statement);
        };
    }

    public void close() {
        database.close();
    }
}
