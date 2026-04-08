package store;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RowSerializerTest {

    @Test
    void serializeAndDeserializeRoundTrip() {
        Column id = new Column("id", ColumnType.NUMBER, 4);
        Column name = new Column("name", ColumnType.CHAR, 32);
        RowSerializer serializer = new RowSerializer(List.of(id, name));

        byte[] page = new byte[4096];
        List<ColumnValue> row = List.of(
                new ColumnValue(id, 42),
                new ColumnValue(name, "alice")
        );
        serializer.serialize(row, page, 0);

        List<ColumnValue> result = serializer.deserialize(page, 0);
        assertEquals(2, result.size());
        assertEquals(42, result.get(0).value());
        assertEquals("alice", result.get(1).value());
    }

    @Test
    void shortStringIsPaddedToFixedSize() {
        Column name = new Column("name", ColumnType.CHAR, 32);
        RowSerializer serializer = new RowSerializer(List.of(name));

        byte[] page = new byte[4096];
        serializer.serialize(List.of(new ColumnValue(name, "hi")), page, 0);

        List<ColumnValue> result = serializer.deserialize(page, 0);
        assertEquals("hi", result.get(0).value());
    }

    @Test
    void longStringIsTruncated() {
        Column name = new Column("name", ColumnType.CHAR, 4);
        RowSerializer serializer = new RowSerializer(List.of(name));

        byte[] page = new byte[4096];
        serializer.serialize(List.of(new ColumnValue(name, "abcdefgh")), page, 0);

        List<ColumnValue> result = serializer.deserialize(page, 0);
        assertEquals("abcd", result.get(0).value());
    }

    @Test
    void offsetsAreCorrect() {
        Column id = new Column("id", ColumnType.NUMBER, 4);
        Column name = new Column("name", ColumnType.CHAR, 32);
        Column age = new Column("age", ColumnType.NUMBER, 4);
        RowSerializer serializer = new RowSerializer(List.of(id, name, age));

        Map<String, Integer> offsets = serializer.getOffsets();
        assertEquals(0, offsets.get("id"));
        assertEquals(4, offsets.get("name"));
        assertEquals(36, offsets.get("age"));
        assertEquals(40, serializer.getRowSize());
    }

    @Test
    void invalidColumnNameThrows() {
        Column id = new Column("id", ColumnType.NUMBER, 4);
        RowSerializer serializer = new RowSerializer(List.of(id));

        Column bogus = new Column("bogus", ColumnType.NUMBER, 4);
        byte[] page = new byte[4096];

        assertThrows(IllegalArgumentException.class, () ->
                serializer.serialize(List.of(new ColumnValue(bogus, 1)), page, 0));
    }
}
