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
        Table table = new Table(pager, 1, columns);
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
        Table table = new Table(pager, 1, columns);
        table.insertRow(makeRow(1, "alice"));
        table.insertRow(makeRow(2, "bob"));

        List<ColumnValue> row = table.getRowByKey(2);
        assertNotNull(row);
        assertEquals(2, row.get(0).value());
        assertEquals("bob", row.get(1).value());
    }

    @Test
    void getRowByKeyNotFound() {
        Table table = new Table(pager, 1, columns);
        table.insertRow(makeRow(1, "alice"));

        assertNull(table.getRowByKey(99));
    }

    @Test
    void insertMultipleRows() {
        Table table = new Table(pager, 1, columns);
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
        Table table = new Table(pager, 1, columns);
        List<List<ColumnValue>> rows = table.getAllRows();
        assertTrue(rows.isEmpty());
    }
}
