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
            switch (c) {
                case ' ' -> i++;
                case '(' -> {
                    tokens.add(new Token(TokenType.LEFT_PARENTHESIS, String.valueOf(c)));
                    i++;
                }
                case ')' -> {
                    tokens.add(new Token(TokenType.RIGHT_PARENTHESIS, String.valueOf(c)));
                    i++;
                }
                case ',' -> {
                    tokens.add(new Token(TokenType.COMMA, String.valueOf(c)));
                    i++;
                }
                case ';' -> {
                    tokens.add(new Token(TokenType.SEMICOLON, String.valueOf(c)));
                    i++;
                }
                case '*' -> {
                    tokens.add(new Token(TokenType.STAR, String.valueOf(c)));
                    i++;
                }
                case '\'' -> {
                    i++;
                    StringBuilder builder = new StringBuilder();
                    while (i < length) {
                        if (input.charAt(i) == '\'') {
                            if (i+1 < length && input.charAt(i+1) == '\'') {
                                builder.append(input.charAt(i+1));
                                i += 2;
                            } else {
                                break;
                            }
                        } else if (input.charAt(i) == '\\') {
                            if (i+1 < length) {
                                builder.append(input.charAt(i+1));
                                i += 2;
                            } else {
                                throw new UnsupportedOperationException("Failed to parse input");
                            }
                        }
                        else {
                            builder.append(input.charAt(i));
                            i++;
                        }
                    }

                    if (i >= length || input.charAt(i) != '\'') {
                        throw new UnsupportedOperationException("Failed to parse input");
                    }

                    tokens.add(new Token(TokenType.STRING_LITERAL, builder.toString()));
                    i++;
                }
                default -> {
                    if (!Character.isLetterOrDigit(c) && c != '_') {
                        throw new UnsupportedOperationException("Failed to parse input");
                    }

                    StringBuilder builder = new StringBuilder();
                    while (i < length && isWordChar(input.charAt(i))) {
                        builder.append(input.charAt(i));
                        i++;
                    }
                    String word = builder.toString();

                    TokenType tokenType;
                    if (Keyword.fromInput(word).isPresent()) {
                        tokenType = TokenType.KEYWORD;
                    } else if (word.chars().allMatch(Character::isDigit)) {
                        tokenType = TokenType.NUMBER_LITERAL;
                    } else {
                        tokenType = TokenType.IDENTIFIER;
                    }
                    tokens.add(new Token(tokenType, word));
                }
            }
        }
        return tokens;
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

}
