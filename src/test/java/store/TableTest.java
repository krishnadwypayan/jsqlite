package store;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    private final Column id = new Column("id", ColumnType.NUMBER, 4, true);
    private final Column name = new Column("name", ColumnType.CHAR, 32, false);
    private final List<Column> columns = List.of(id, name);
    private File tempFile;
    private Pager pager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("jsqlite-test", ".db");
        pager = new Pager(tempFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        pager.close();
        tempFile.delete();
    }

    private List<ColumnValue> makeRow(int idVal, String nameVal) {
        return List.of(new ColumnValue(id, idVal), new ColumnValue(name, nameVal));
    }

    @Test
    void insertAndReadBackAllRows() {
        Table table = new Table(pager, pager.allocatePage(), columns, true);
        assertTrue(table.insertRow(makeRow(1, "alice")));
        assertTrue(table.insertRow(makeRow(2, "bob")));

        List<List<ColumnValue>> rows = table.getAllRows();
        assertEquals(2, rows.size());
        assertEquals(1, rows.get(0).get(0).value());
        assertEquals("alice", rows.get(0).get(1).value());
        assertEquals(2, rows.get(1).get(0).value());
        assertEquals("bob", rows.get(1).get(1).value());
    }

    @Test
    void getRowByKey() {
        Table table = new Table(pager, pager.allocatePage(), columns, true);
        table.insertRow(makeRow(1, "alice"));
        table.insertRow(makeRow(2, "bob"));

        List<ColumnValue> row = table.getRowByKey(2);
        assertNotNull(row);
        assertEquals(2, row.get(0).value());
        assertEquals("bob", row.get(1).value());
    }

    @Test
    void getRowByKeyNotFound() {
        Table table = new Table(pager, pager.allocatePage(), columns, true);
        table.insertRow(makeRow(1, "alice"));

        assertNull(table.getRowByKey(99));
    }

    @Test
    void insertMultipleRows() {
        Table table = new Table(pager, pager.allocatePage(), columns, true);
        int rowsToInsert = 50;
        for (int i = 0; i < rowsToInsert; i++) {
            assertTrue(table.insertRow(makeRow(i, "user" + i)));
        }

        List<List<ColumnValue>> rows = table.getAllRows();
        assertEquals(rowsToInsert, rows.size());
        assertEquals(0, rows.get(0).get(0).value());
        assertEquals(49, rows.get(49).get(0).value());
        assertEquals("user49", rows.get(49).get(1).value());
    }

    @Test
    void emptyTableReturnsNoRows() {
        Table table = new Table(pager, pager.allocatePage(), columns, true);
        List<List<ColumnValue>> rows = table.getAllRows();
        assertTrue(rows.isEmpty());
    }

    @Test
    void insertedRowsAreSortedByKey() {
        Table table = new Table(pager, pager.allocatePage(), columns, true);
        table.insertRow(makeRow(3, "charlie"));
        table.insertRow(makeRow(1, "alice"));
        table.insertRow(makeRow(2, "bob"));

        List<List<ColumnValue>> rows = table.getAllRows();
        assertEquals(3, rows.size());
        assertEquals(1, rows.get(0).get(0).value());
        assertEquals("alice", rows.get(0).get(1).value());
        assertEquals(2, rows.get(1).get(0).value());
        assertEquals("bob", rows.get(1).get(1).value());
        assertEquals(3, rows.get(2).get(0).value());
        assertEquals("charlie", rows.get(2).get(1).value());
    }

    @Test
    void duplicateKeyThrows() {
        Table table = new Table(pager, pager.allocatePage(), columns, true);
        table.insertRow(makeRow(1, "alice"));
        assertThrows(StorageException.class, () -> table.insertRow(makeRow(1, "bob")));
    }

    @Test
    void getRowByKeyAfterOutOfOrderInserts() {
        Table table = new Table(pager, pager.allocatePage(), columns, true);
        table.insertRow(makeRow(5, "eve"));
        table.insertRow(makeRow(2, "bob"));
        table.insertRow(makeRow(8, "heidi"));
        table.insertRow(makeRow(1, "alice"));

        List<ColumnValue> row = table.getRowByKey(2);
        assertNotNull(row);
        assertEquals(2, row.get(0).value());
        assertEquals("bob", row.get(1).value());

        row = table.getRowByKey(8);
        assertNotNull(row);
        assertEquals("heidi", row.get(1).value());

        assertNull(table.getRowByKey(99));
    }

    @Test
    void splitLeafNodeWhenFull() {
        int startPage = pager.allocatePage();

        // row size = 4 (id) + 32 (name) = 36, cell size = 40
        // maxCells = (4096 - 10) / 40 = 102
        Table table = new Table(pager, startPage, columns, true);
        int maxCells = 102;

        // fill the leaf node to capacity
        for (int i = 0; i < maxCells; i++) {
            assertTrue(table.insertRow(makeRow(i, "user" + i)));
        }

        // this insert should trigger a split
        assertTrue(table.insertRow(makeRow(maxCells, "user" + maxCells)));

        // all rows should still be readable
        List<List<ColumnValue>> allRows = table.getAllRows();
        assertEquals(maxCells + 1, allRows.size());

        // verify first, middle, and last
        assertEquals(0, allRows.get(0).get(0).value());
        assertEquals(50, allRows.get(50).get(0).value());
        assertEquals(maxCells, allRows.get(maxCells).get(0).value());
    }

    @Test
    void splitLeafNodeWithOutOfOrderKeys() {
        int startPage = pager.allocatePage();
        Table table = new Table(pager, startPage, columns, true);
        int maxCells = 102;

        // insert in reverse order to stress sorted insertion + split
        for (int i = maxCells; i >= 0; i--) {
            assertTrue(table.insertRow(makeRow(i, "user" + i)));
        }

        // verify all rows readable and sorted
        List<List<ColumnValue>> allRows = table.getAllRows();
        assertEquals(maxCells + 1, allRows.size());
        assertEquals(0, allRows.get(0).get(0).value());
        assertEquals(maxCells, allRows.get(maxCells).get(0).value());
    }
}
