package store;

import lombok.Getter;

import static store.DatabaseConstants.MAX_PAGES;
import static store.DatabaseConstants.PAGE_SIZE;
import static store.DatabaseConstants.TABLE_ROW_START_OFFSET;

public class Cursor {

    private final Pager pager;
    private final int startPage;
    private final int rowSize;
    private final int numRows;

    private final int ROWS_IN_START_PAGE;
    private final int ROWS_PER_PAGE;

    @Getter
    private int rowNumber; // current row position

    @Getter
    private boolean endOfTable;

    public Cursor(Pager pager, int startPage, int rowSize, int numRows, int rowNumber) {
        ROWS_IN_START_PAGE = (PAGE_SIZE - TABLE_ROW_START_OFFSET) / rowSize;
        ROWS_PER_PAGE = PAGE_SIZE / rowSize;
        this.pager = pager;
        this.startPage = startPage;
        this.rowSize = rowSize;
        this.numRows = numRows;
        this.rowNumber = rowNumber;
        endOfTable = (numRows == 0);
    }

    public void tableEnd() {
        rowNumber = numRows;
        endOfTable = true;
    }

    public CursorValue value() {
        PageNumberAndOffset pageNumberAndOffset = getPageNumberAndOffset(rowNumber);
        return new CursorValue(pager.getPage(pageNumberAndOffset.pageNumber()), pageNumberAndOffset.pageNumber(), pageNumberAndOffset.rowOffset());
    }

    public void advance() {
        if (endOfTable) {
            return;
        }

        rowNumber++;
        if (rowNumber == numRows) {
            endOfTable = true;
        }
    }

    public int maxRows() {
        return ROWS_IN_START_PAGE + ((MAX_PAGES - startPage - 1) * ROWS_PER_PAGE);
    }

    private PageNumberAndOffset getPageNumberAndOffset(int rowNumber) {
        int currentPageNumber;
        int rowOffset;
        if (rowNumber < ROWS_IN_START_PAGE) {
            currentPageNumber = startPage;
            rowOffset = TABLE_ROW_START_OFFSET + (rowNumber * rowSize);
        } else {
            int adjusted = rowNumber - ROWS_IN_START_PAGE;
            currentPageNumber = startPage + 1 + (adjusted / ROWS_PER_PAGE);
            rowOffset = (adjusted % ROWS_PER_PAGE) * rowSize;
        }
        return new PageNumberAndOffset(currentPageNumber, rowOffset);
    }

    private record PageNumberAndOffset(int pageNumber, int rowOffset) {
    }

}
