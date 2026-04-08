package command;

import java.io.BufferedOutputStream;
import java.io.PrintWriter;

public sealed interface CommandHandler
        permits MetaCommandHandler, SqlCommandHandler {

    PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));

    default void printLine(String line) {
        out.println(line);
        out.flush();
    }

}
