package command;

import command.handler.CreateTableCommandHandler;
import command.handler.ExitCommandHandler;
import command.handler.InsertCommandHandler;
import command.handler.SelectCommandHandler;
import lexer.Lexer;
import lexer.Token;
import store.Database;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {

    private final Database database;
    private final Lexer lexer;
    private final Map<MetaCommand, MetaCommandHandler> metaCommands = new EnumMap<>(MetaCommand.class);
    private final Map<SqlCommand, SqlCommandHandler> sqlCommands = new EnumMap<>(SqlCommand.class);

    public CommandRegistry() {
        lexer = new Lexer();
        database = new Database();
        metaCommands.put(MetaCommand.EXIT, new ExitCommandHandler());
        sqlCommands.put(SqlCommand.SELECT, new SelectCommandHandler(database));
        sqlCommands.put(SqlCommand.INSERT, new InsertCommandHandler(database));
        sqlCommands.put(SqlCommand.CREATE, new CreateTableCommandHandler(database));
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

        return SqlCommand.fromInput(tokens)
                .map(sqlCommands::get)
                .map(h -> h.execute(tokens))
                .orElse(CommandResult.UNRECOGNIZED_STATEMENT);
    }
}
