package command.handler;

import command.CommandResult;
import command.SqlCommandHandler;
import lexer.Token;
import store.Database;

import java.util.List;

public class InsertCommandHandler implements SqlCommandHandler {

    private final Database database;

    public InsertCommandHandler(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(List<Token> tokens) {

        printLine(tokens.toString());

        try {

//            database.getTable()

        } catch (NumberFormatException ex) {
//            printLine(String.format("Error parsing id: '%s'", args[1]));
            return CommandResult.PREPARE_SYNTAX_ERROR;
        }


        return CommandResult.PREPARE_SUCCESS;
    }

}
