package lexer;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Keyword {

    CREATE,
    TABLE,
    INSERT,
    SELECT,
    INTO,
    NUMBER,
    CHAR,
    VARCHAR,
    VALUES,
    FROM,
    WHERE
    ;

    private static final Map<String, Keyword> LOOKUP = Arrays.stream(Keyword.values())
            .collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

    public static Optional<Keyword> fromInput(String input) {
        return Optional.ofNullable(LOOKUP.get(input.toUpperCase()));
    }

}
