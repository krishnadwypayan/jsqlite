package command;

import parser.Token;

import java.util.List;

public non-sealed interface SqlCommandHandler extends CommandHandler {

    CommandResult execute(List<Token> statement);

}
