package command;

public non-sealed interface MetaCommandHandler extends CommandHandler {

    CommandResult execute(String statement);

}
