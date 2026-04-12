package command.handler;

import command.CommandResult;
import command.SqlCommandHandler;
import parser.SelectStatement;
import parser.Statement;
import store.Column;
import store.ColumnValue;
import store.Database;
import store.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SelectCommandHandler implements SqlCommandHandler {

    private final Database database;

    public SelectCommandHandler(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(Statement statement) {
        SelectStatement selectStatement = (SelectStatement) statement;
        Table table = database.getTable(selectStatement.tableName());
        if (table == null) {
            throw new CommandHandlerExecutionException(String.format("table not found %s", selectStatement.tableName()));
        }

        Map<Object, Column> tableColumns = table.getColumns().stream().collect(Collectors.toUnmodifiableMap(Column::name, Function.identity()));
        List<Column> queryColumns;
        if (selectStatement.columns().getFirst().equals("*")) {
            queryColumns = table.getColumns();
        } else {
            queryColumns = selectStatement.columns().stream().map(tableColumns::get).toList();
        }

        // Collect all rows, filtering to requested columns
        List<String> headers = queryColumns.stream().map(Column::name).toList();
        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < table.getNumRows(); i++) {
            List<ColumnValue> fullRow = table.getRow(i);
            Map<String, ColumnValue> rowMap = fullRow.stream()
                    .collect(Collectors.toMap(cv -> cv.column().name(), Function.identity()));
            List<String> filteredRow = queryColumns.stream()
                    .map(col -> String.valueOf(rowMap.get(col.name()).value()))
                    .toList();
            rows.add(filteredRow);
        }

        // Calculate column widths
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            widths[i] = headers.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }

        // Print header
        printLine(formatSeparator(widths));
        printLine(formatRow(headers, widths));
        printLine(formatSeparator(widths));

        // Print rows
        for (List<String> row : rows) {
            printLine(formatRow(row, widths));
        }
        printLine(formatSeparator(widths));

        return CommandResult.PREPARE_SUCCESS;
    }

    private String formatRow(List<String> values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.size(); i++) {
            sb.append(String.format(" %-" + widths[i] + "s |", values.get(i)));
        }
        return sb.toString();
    }

    private String formatSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append("+");
        }
        return sb.toString();
    }

}
