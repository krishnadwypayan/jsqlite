package command.handler;

import command.CommandResult;
import command.SqlCommandHandler;
import lexer.Token;
import store.Database;

import java.util.List;

public class CreateTableCommandHandler implements SqlCommandHandler {

    private final Database database;

    public CreateTableCommandHandler(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(List<Token> tokens) {
        printLine(tokens.toString());
        return CommandResult.PREPARE_SUCCESS;
    }

}
