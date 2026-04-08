package parser;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int length = input.length();
        int i = 0;
        while (i < length) {
            char c = input.charAt(i);
            if (c == '(' || c == ')' || c == ',' || c == ';') {
                tokens.add(new Token(getTokenType(c), String.valueOf(c)));
            } else if (c != ' ') {
                TokenType tokenType;
                if (c >= '0' && c <= '9') { // NUMBER_LITERAL
                    tokenType = TokenType.NUMBER_LITERAL;
                } else if (c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    tokenType = TokenType.IDENTIFIER;
                } else {
                    throw new UnsupportedOperationException("Failed to parse input");
                }

                StringBuilder builder = new StringBuilder();
                while (i < length && isWordChar(input.charAt(i))) {
                    builder.append(input.charAt(i));
                    i++;
                }
                String token = builder.toString();
                if (Keyword.fromInput(token).isPresent()) {
                    tokenType = TokenType.KEYWORD;
                }
                tokens.add(new Token(tokenType, token));
                continue;
            }
            i++;
        }
        return tokens;
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private TokenType getTokenType(char c) {
        if (c == '(') {
            return TokenType.LEFT_PARENTHESIS;
        }
        if (c == ')') {
            return TokenType.RIGHT_PARENTHESIS;
        }
        if (c == ',') {
            return TokenType.COMMA;
        }
        if (c == ';') {
            return TokenType.SEMICOLON;
        }

        throw new UnsupportedOperationException("unsupported token");
    }

}
