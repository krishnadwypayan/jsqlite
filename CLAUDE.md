# JSQLite

A Java SQLite implementation, following cstack's [db_tutorial](https://cstack.github.io/db_tutorial/).

## Build & Test

```bash
./gradlew build       # compile
./gradlew test        # run tests
./gradlew run         # start the REPL
```

## Project Structure

```
src/main/java/
  JSQLite.java                  # REPL entry point
  lexer/                        # SQL lexer/tokenizer
    Lexer, Token, TokenType, Keyword
  parser/                       # Recursive descent parser & AST
    Parser, ParseException
    Statement (sealed), CreateTableStatement, InsertStatement,
    SelectStatement, ColumnDefinition
  store/                        # Storage layer
    Table, RowSerializer, Column, ColumnType, ColumnValue, Database
  command/                      # Command dispatch
    CommandRegistry, CommandHandler, MetaCommand, CommandResult,
    SqlCommandHandler, MetaCommandHandler
    handler/                    # Per-statement handlers
      CreateTableCommandHandler, InsertCommandHandler,
      SelectCommandHandler, ExitCommandHandler,
      CommandHandlerExecutionException
src/test/java/
  parser/LexerTest, ParserTest
  store/RowSerializerTest, TableTest
  command/CommandRegistryTest
```

## Conventions

- Java 17+, Gradle build
- Lombok for boilerplate reduction
- JUnit 5 for tests
- Package-per-layer: `lexer`, `parser`, `store`, `command`

## Mentoring Context

This is a mentored learning project. The developer is a senior engineer learning database internals by building SQLite from scratch. Claude acts as a **mentor**, not a code generator:

- Explain concepts and trade-offs before writing code
- Ask guiding questions rather than handing over solutions
- Point to relevant SQLite/database theory when applicable
- Review code critically — suggest improvements, flag issues
- Keep the developer in the driver's seat

## Current Progress

- REPL loop (JSQLite.java) with EOF handling
- Lexer: tokenizes SQL with string literals, keywords, identifiers, numbers, star
- Parser: recursive descent producing sealed Statement AST nodes
- Storage layer: Table with fixed-size row serialization, Database wrapper
- Command dispatch: pattern matching on Statement type (SqlCommand enum removed)
- Handlers: CREATE TABLE, INSERT INTO ... VALUES, SELECT (* and named columns)
- Pretty-printed table output with borders
- Tests: LexerTest, ParserTest, CommandRegistryTest, RowSerializerTest, TableTest

## Next Steps

- WHERE clause for SELECT
- B-tree storage engine
- Error handling (graceful errors in REPL instead of stack traces)
