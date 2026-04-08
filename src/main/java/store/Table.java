package store;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class Table {

    private static final int PAGE_SIZE = 4096;
    private static final int MAX_PAGES = 1_000;
    
    private final int ROWS_PER_PAGE;
    private final byte[][] pages;
    private final RowSerializer rowSerializer;

    @Getter
    private int numRows;

    public Table(List<Column> columns) {
        this.rowSerializer = new RowSerializer(columns);
        ROWS_PER_PAGE = PAGE_SIZE/rowSerializer.getRowSize();
        pages = new byte[MAX_PAGES][];
    }

    public boolean insertRow(List<ColumnValue> row) {
        if (numRows >= MAX_PAGES * ROWS_PER_PAGE) {
            return false;
        }

        int currentPage = numRows/ROWS_PER_PAGE;
        if (pages[currentPage] == null) {
            pages[currentPage] = new byte[PAGE_SIZE];
        }

        int rowOffset = (numRows % ROWS_PER_PAGE) * rowSerializer.getRowSize();
        rowSerializer.serialize(row, pages[currentPage], rowOffset);
        numRows++;
        return true;
    }

    public List<ColumnValue> getRow(int rowNumber) {
        if (rowNumber > numRows) {
            return Collections.emptyList();
        }
        int page = rowNumber/ROWS_PER_PAGE;
        int rowOffset = (rowNumber % ROWS_PER_PAGE) * rowSerializer.getRowSize();
        return rowSerializer.deserialize(pages[page], rowOffset);
    }

}
