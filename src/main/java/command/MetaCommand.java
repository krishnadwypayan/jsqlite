package command;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum MetaCommand {

    EXIT(".exit")
    ;

    private final String value;

    private static final Map<String, MetaCommand> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(c -> c.value.toLowerCase(), Function.identity()));

    MetaCommand(String value) {
        this.value = value;
    }

    public static Optional<MetaCommand> fromInput(String input) {
        return Optional.ofNullable(LOOKUP.get(input.toLowerCase()));
    }
}
