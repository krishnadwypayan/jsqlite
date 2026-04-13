package store;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static store.DatabaseConstants.MAX_PAGES;
import static store.DatabaseConstants.PAGE_SIZE;
import static store.DatabaseConstants.TABLE_ROW_START_OFFSET;

public class Table {

    private final int ROWS_IN_START_PAGE;
    private final int ROWS_PER_PAGE;
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
        ROWS_IN_START_PAGE = (PAGE_SIZE - TABLE_ROW_START_OFFSET) / rowSerializer.getRowSize();
        ROWS_PER_PAGE = PAGE_SIZE / rowSerializer.getRowSize();
        this.numRows = ByteBuffer.wrap(pager.getPage(startPage)).getInt();
    }

    public boolean insertRow(List<ColumnValue> row) {
        if (numRows >= ROWS_IN_START_PAGE + ((MAX_PAGES - startPage - 1) * ROWS_PER_PAGE)) {
            return false;
        }

        serializeRowData(row);
        incrementNumRows();
        return true;
    }

    private void serializeRowData(List<ColumnValue> row) {
        PageNumberAndOffset result = getPageNumberAndOffset(numRows);
        byte[] page = pager.getPage(result.currentPageNumber());
        rowSerializer.serialize(row, page, result.rowOffset());
        pager.markDirty(result.currentPageNumber());
    }

    public List<ColumnValue> getRow(int rowNumber) {
        if (rowNumber > numRows) {
            return Collections.emptyList();
        }

        PageNumberAndOffset result = getPageNumberAndOffset(rowNumber);
        byte[] page = pager.getPage(result.currentPageNumber());
        return rowSerializer.deserialize(page, result.rowOffset());
    }

    private void incrementNumRows() {
        numRows++;
        byte[] page = pager.getPage(this.startPage);
        rowSerializer.serializeRowCount(page, numRows);
        pager.markDirty(this.startPage);
    }

    private PageNumberAndOffset getPageNumberAndOffset(int rowNumber) {
        int currentPageNumber;
        int rowOffset;
        if (rowNumber < ROWS_IN_START_PAGE) {
            currentPageNumber = startPage;
            rowOffset = TABLE_ROW_START_OFFSET + (rowNumber * rowSerializer.getRowSize());
        } else {
            int adjusted = rowNumber - ROWS_IN_START_PAGE;
            currentPageNumber = startPage + 1 + (adjusted / ROWS_PER_PAGE);
            rowOffset = (adjusted % ROWS_PER_PAGE) * rowSerializer.getRowSize();
        }
        return new PageNumberAndOffset(currentPageNumber, rowOffset);
    }

    private record PageNumberAndOffset(int currentPageNumber, int rowOffset) {
    }

}
