import command.CommandRegistry;
import command.CommandResult;

final PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
final CommandRegistry commandRegistry = new CommandRegistry();

void main() throws IOException {

    while (true) {
        printPrompt();
        String input = readInput();
        if (input == null) break;

        CommandResult commandResult;
        try {
            commandResult = commandRegistry.dispatch(input);
        } catch (Exception e) {
            out.printf("Error: %s\n", e.getMessage());
            out.flush();
            continue;
        }
        switch (commandResult) {
            case EXIT -> {
                bufferedReader.close();
                System.exit(0);
            }
            case PREPARE_SUCCESS, PREPARE_SYNTAX_ERROR -> {}
            case UNRECOGNIZED_COMMAND -> out.printf("Unrecognized command '%s'.\n", input);
            case UNRECOGNIZED_STATEMENT -> out.printf("Unrecognized keyword at start of '%s'.\n", input);
        }
        out.flush();
    }
}

void printPrompt() {
    out.printf("jsqlite> ");
    out.flush();
}

String readInput() throws IOException {
    return bufferedReader.readLine();
}

