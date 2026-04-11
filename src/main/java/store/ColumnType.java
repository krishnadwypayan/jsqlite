package store;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ColumnType {

    NUMBER(4),
    CHAR(0),
    VARCHAR(0)
    ;

    private static final Map<String, ColumnType> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(c -> c.name().toLowerCase(), Function.identity()));

    @Getter
    private final int size;

    ColumnType(int size) {
        this.size = size;
    }

    public static Optional<ColumnType> fromInput(String input) {
        return Optional.ofNullable(LOOKUP.get(input.toLowerCase()));
    }

}
