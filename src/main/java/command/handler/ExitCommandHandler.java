package command.handler;

import command.CommandResult;
import command.MetaCommandHandler;

public class ExitCommandHandler implements MetaCommandHandler {

    @Override
    public CommandResult execute(String input) {
        return CommandResult.EXIT;
    }

}
