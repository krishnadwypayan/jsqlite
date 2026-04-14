package store;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RowSerializerTest {

    @Test
    void serializeAndDeserializeRoundTrip() {
        Column id = new Column("id", ColumnType.NUMBER, 4, true);
        Column name = new Column("name", ColumnType.CHAR, 32, false);
        RowSerializer serializer = new RowSerializer(List.of(id, name));

        List<ColumnValue> row = List.of(
                new ColumnValue(id, 42),
                new ColumnValue(name, "alice")
        );
        byte[] rowBytes = serializer.serialize(row);

        List<ColumnValue> result = serializer.deserialize(rowBytes);
        assertEquals(2, result.size());
        assertEquals(42, result.get(0).value());
        assertEquals("alice", result.get(1).value());
    }

    @Test
    void shortStringIsPaddedToFixedSize() {
        Column name = new Column("name", ColumnType.CHAR, 32, false);
        RowSerializer serializer = new RowSerializer(List.of(name));

        byte[] rowBytes = serializer.serialize(List.of(new ColumnValue(name, "hi")));

        List<ColumnValue> result = serializer.deserialize(rowBytes);
        assertEquals("hi", result.get(0).value());
    }

    @Test
    void longStringIsTruncated() {
        Column name = new Column("name", ColumnType.CHAR, 4, false);
        RowSerializer serializer = new RowSerializer(List.of(name));

        byte[] rowBytes = serializer.serialize(List.of(new ColumnValue(name, "abcdefgh")));

        List<ColumnValue> result = serializer.deserialize(rowBytes);
        assertEquals("abcd", result.get(0).value());
    }

    @Test
    void offsetsAreCorrect() {
        Column id = new Column("id", ColumnType.NUMBER, 4, true);
        Column name = new Column("name", ColumnType.CHAR, 32, false);
        Column age = new Column("age", ColumnType.NUMBER, 4, false);
        RowSerializer serializer = new RowSerializer(List.of(id, name, age));

        Map<String, Integer> offsets = serializer.getOffsets();
        assertEquals(0, offsets.get("id"));
        assertEquals(4, offsets.get("name"));
        assertEquals(36, offsets.get("age"));
        assertEquals(40, serializer.getRowSize());
    }

    @Test
    void invalidColumnNameThrows() {
        Column id = new Column("id", ColumnType.NUMBER, 4, false);
        RowSerializer serializer = new RowSerializer(List.of(id));

        Column bogus = new Column("bogus", ColumnType.NUMBER, 4, false);

        assertThrows(IllegalArgumentException.class, () ->
                serializer.serialize(List.of(new ColumnValue(bogus, 1))));
    }
}
