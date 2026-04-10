package command;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import lexer.Token;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum SqlCommand {

    SELECT("select"),
    INSERT("insert"),
    CREATE("create table")
    ;

    private final String value;

    private static final Map<String, SqlCommand> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(c -> c.value.toLowerCase(), Function.identity()));

    SqlCommand(String value) {
        this.value = value;
    }

    public static Optional<SqlCommand> fromInput(List<Token> tokens) {
        int size = tokens.size();
        if (size > 1) {
            String keyword = String.join(" ", tokens.get(0).value(), tokens.get(1).value());
            if (LOOKUP.containsKey(keyword)) {
                return Optional.of(LOOKUP.get(keyword));
            }
        }

        if (CollectionUtils.isNotEmpty(tokens) && LOOKUP.containsKey(tokens.getFirst().value())) {
            return Optional.of(LOOKUP.get(tokens.getFirst().value()));
        }

        return Optional.empty();
    }

}
