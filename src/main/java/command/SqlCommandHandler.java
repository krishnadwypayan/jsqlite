package command;

import parser.Statement;

public non-sealed interface SqlCommandHandler extends CommandHandler {

    CommandResult execute(Statement statement);

}
