package store;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class Table {

    private final Pager pager;
    private final RowSerializer rowSerializer;
    private final int startPage;

    @Getter
    private int numRows;

    @Getter
    private final List<Column> columns;

    public Table(Pager pager, int startPage, List<Column> columns) {
        this.pager = pager;
        this.startPage = startPage;
        this.columns = columns;
        this.rowSerializer = new RowSerializer(columns);
        this.numRows = ByteBuffer.wrap(pager.getPage(startPage)).getInt();
    }

    public boolean insertRow(List<ColumnValue> row) {
        Cursor cursor = new Cursor(pager, startPage, rowSerializer.getRowSize(), numRows, numRows);
        if (numRows >= cursor.maxRows()) {
            return false;
        }

        serializeRowData(row, cursor);
        incrementNumRows();
        return true;
    }

    private void serializeRowData(List<ColumnValue> row, Cursor cursor) {
        cursor.tableEnd();
        CursorValue cursorValue = cursor.value();
        rowSerializer.serialize(row, cursorValue.page(), cursorValue.rowOffset());
        pager.markDirty(cursorValue.pageNumber());
    }

    public List<ColumnValue> getRow(int rowNumber) {
        if (rowNumber >= numRows) {
            return Collections.emptyList();
        }

        Cursor cursor = new Cursor(pager, startPage, rowSerializer.getRowSize(), numRows, rowNumber);
        CursorValue cursorValue = cursor.value();
        return rowSerializer.deserialize(cursorValue.page(), cursorValue.rowOffset());
    }

    private void incrementNumRows() {
        numRows++;
        byte[] page = pager.getPage(this.startPage);
        rowSerializer.serializeRowCount(page, numRows);
        pager.markDirty(this.startPage);
    }

}
