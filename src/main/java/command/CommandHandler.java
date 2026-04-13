package command;

import java.io.PrintWriter;

public sealed interface CommandHandler
        permits MetaCommandHandler, SqlCommandHandler {

    PrintWriter out = new PrintWriter(System.out, true);

    default void printLine(String line) {
        out.println(line);
    }

}
