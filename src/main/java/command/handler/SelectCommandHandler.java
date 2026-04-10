package command.handler;

import command.CommandResult;
import command.SqlCommandHandler;
import lexer.Token;
import store.Database;

import java.util.List;

public class SelectCommandHandler implements SqlCommandHandler {

    private final Database database;

    public SelectCommandHandler(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(List<Token> tokens) {
//        printLine(String.format("Executed '%s'", statement));
        return CommandResult.PREPARE_SUCCESS;
    }

}
