package command.handler;

import command.CommandResult;
import command.SqlCommandHandler;
import parser.CreateTableStatement;
import parser.ParserException;
import parser.Statement;
import store.Column;
import store.ColumnType;
import store.Database;

import java.util.List;
import java.util.Optional;

public class CreateTableCommandHandler implements SqlCommandHandler {

    private final Database database;

    public CreateTableCommandHandler(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(Statement statement) {
        CreateTableStatement createTableStatement = (CreateTableStatement) statement;
        List<Column> columns = createTableStatement.columnDefinitions().stream()
                .map(columnDefinition -> {
                    Optional<ColumnType> columnType = ColumnType.fromInput(columnDefinition.type());
                    if (columnType.isEmpty()) {
                        throw new CommandHandlerExecutionException(String.format("Unknown column type: '%s - %s'",
                                columnDefinition.name(), columnDefinition.type()));
                    }
                    return new Column(columnDefinition.name(), columnType.get(),
                            columnDefinition.size() == 0 ? columnType.get().getSize() : columnDefinition.size(),
                            columnDefinition.primaryKey());
                }).toList();

        if (columns.stream().noneMatch(Column::primaryKey)) {
            throw new CommandHandlerExecutionException("No primary key column given");
        }

        database.createTable(createTableStatement.tableName(), columns);
        return CommandResult.PREPARE_SUCCESS;
    }

}
