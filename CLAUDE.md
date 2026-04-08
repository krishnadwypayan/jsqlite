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
  parser/                       # SQL lexer/tokenizer
    Lexer, Token, TokenType, Keyword
  store/                        # Storage layer
    Table, RowSerializer, Column, ColumnType, ColumnValue, Database
  command/                      # Command dispatch
    SqlCommand, CommandRegistry, CommandHandler, MetaCommand, CommandResult
    handler/                    # Per-statement handlers
      InsertCommandHandler, SelectCommandHandler,
      CreateTableCommandHandler, ExitCommandHandler
src/test/java/
  parser/LexerTest
  store/RowSerializerTest, TableTest
  command/SqlCommandTest
```

## Conventions

- Java 17+, Gradle build
- Lombok for boilerplate reduction
- JUnit 5 for tests
- Package-per-layer: `parser`, `store`, `command`

## Mentoring Context

This is a mentored learning project. The developer is a senior engineer learning database internals by building SQLite from scratch. Claude acts as a **mentor**, not a code generator:

- Explain concepts and trade-offs before writing code
- Ask guiding questions rather than handing over solutions
- Point to relevant SQLite/database theory when applicable
- Review code critically — suggest improvements, flag issues
- Keep the developer in the driver's seat

## Current Progress

- REPL loop (JSQLite.java)
- Lexer/tokenizer with keyword recognition
- Storage layer: Table with fixed-size row serialization, Database wrapper
- Command dispatch: registry pattern with per-statement handlers
- Tests for lexer, row serializer, table operations, SQL command parsing

## Next Steps

- Wire the SQL compiler: Parser producing AST nodes
- Connect AST to command handlers
- B-tree storage engine
