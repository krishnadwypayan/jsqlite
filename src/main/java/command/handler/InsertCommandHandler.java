package command.handler;

import command.CommandResult;
import command.SqlCommandHandler;
import parser.InsertStatement;
import parser.Statement;
import store.Column;
import store.ColumnValue;
import store.Database;
import store.Table;

import java.util.ArrayList;
import java.util.List;

public class InsertCommandHandler implements SqlCommandHandler {

    private final Database database;

    public InsertCommandHandler(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(Statement statement) {
        InsertStatement insertStatement = (InsertStatement) statement;
        Table table = database.getTable(insertStatement.tableName());
        if (table == null) {
            throw new CommandHandlerExecutionException(String.format("table not found %s", insertStatement.tableName()));
        }

        List<Column> columns = table.getColumns();
        if (columns.size() != insertStatement.values().size()) {
            throw new CommandHandlerExecutionException("values list does not have all the columns of the table");
        }

        int i = 0;
        List<ColumnValue> columnValues = new ArrayList<>();
        for (Object value: insertStatement.values()) {
            columnValues.add(new ColumnValue(columns.get(i++), value));
        }

        table.insertRow(columnValues);

        return CommandResult.PREPARE_SUCCESS;
    }

}
