package store;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Table {

    private final Pager pager;
    private final RowSerializer rowSerializer;
    private final int startPage;

    @Getter
    private final List<Column> columns;

    public Table(Pager pager, int startPage, List<Column> columns) {
        this.pager = pager;
        this.startPage = startPage;
        this.columns = columns;
        this.rowSerializer = new RowSerializer(columns);
    }

    public boolean insertRow(List<ColumnValue> row) {
        Cursor cursor = new Cursor(pager, startPage, rowSerializer.getRowSize());
        byte[] rowBytes = rowSerializer.serialize(row);
        Optional<ColumnValue> key = row.stream().filter(columnValue -> columnValue.column().primaryKey()).findFirst();
        key.ifPresent(columnValue -> cursor.insert((Integer) columnValue.value(), rowBytes));
        return true;
    }

    public List<List<ColumnValue>> getAllRows() {
        List<List<ColumnValue>> columnValues = new ArrayList<>();
        Cursor cursor = new Cursor(pager, startPage, rowSerializer.getRowSize());
        while (!cursor.isEndOfTable()) {
            columnValues.add(rowSerializer.deserialize(cursor.getValue()));
            cursor.advance();
        }
        return columnValues;
    }

    public List<ColumnValue> getRowByKey(int key) {
        Cursor cursor = new Cursor(pager, startPage, rowSerializer.getRowSize());
        while (!cursor.isEndOfTable() && cursor.getKey() != key) {
            cursor.advance();
        }
        return cursor.isEndOfTable() ? null : rowSerializer.deserialize(cursor.getValue());
    }

}
