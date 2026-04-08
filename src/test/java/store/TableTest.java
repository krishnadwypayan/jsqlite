package store;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    private final Column id = new Column("id", ColumnType.NUMBER, 4);
    private final Column name = new Column("name", ColumnType.CHAR, 32);
    private final List<Column> columns = List.of(id, name);

    private List<ColumnValue> makeRow(int idVal, String nameVal) {
        return List.of(new ColumnValue(id, idVal), new ColumnValue(name, nameVal));
    }

    @Test
    void insertAndReadBack() {
        Table table = new Table(columns);
        assertTrue(table.insertRow(makeRow(1, "alice")));

        List<ColumnValue> row = table.getRow(0);
        assertEquals(1, row.get(0).value());
        assertEquals("alice", row.get(1).value());
    }

    @Test
    void insertMultipleRowsAcrossPageBoundary() {
        Table table = new Table(columns);
        // row size = 4 + 32 = 36 bytes, page = 4096 bytes => 113 rows per page
        int rowsToInsert = 120; // crosses into second page
        for (int i = 0; i < rowsToInsert; i++) {
            assertTrue(table.insertRow(makeRow(i, "user" + i)));
        }
        assertEquals(rowsToInsert, table.getNumRows());

        // verify first and last rows
        assertEquals(0, table.getRow(0).get(0).value());
        assertEquals(119, table.getRow(119).get(0).value());
        assertEquals("user119", table.getRow(119).get(1).value());
    }

    @Test
    void insertWhenFullReturnsFalse() {
        // Use a large row to reduce max capacity: row size = 4 + 4092 = 4096 => 1 row per page
        Column big = new Column("big", ColumnType.CHAR, 4092);
        Table table = new Table(List.of(id, big));
        // 1 row per page * 1000 pages = 1000 rows max
        for (int i = 0; i < 1000; i++) {
            assertTrue(table.insertRow(List.of(new ColumnValue(id, i), new ColumnValue(big, "x"))));
        }
        assertFalse(table.insertRow(List.of(new ColumnValue(id, 9999), new ColumnValue(big, "x"))));
    }

    @Test
    void getRowWithInvalidIndex() {
        Table table = new Table(columns);
        table.insertRow(makeRow(1, "alice"));
        // index beyond numRows returns empty list
        assertTrue(table.getRow(5).isEmpty());
    }
}
