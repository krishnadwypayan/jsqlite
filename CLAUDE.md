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
    Database, Table, Cursor, CursorValue
    Pager, RowSerializer, SchemaSerializer, TableMetadata
    Column, ColumnType, ColumnValue
    DatabaseConstants, StorageException
  btree/                        # B-tree node format
    Node (abstract), LeafNode, NodeType
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

- Java 21+ (preview features), Gradle build
- Lombok for boilerplate reduction
- JUnit 5 for tests
- Package-per-layer: `lexer`, `parser`, `store`, `btree`, `command`
- Page bytes are the single source of truth (no duplicate state in Java fields)

## Mentoring Context

This is a mentored learning project. The developer is a senior engineer learning database internals by building SQLite from scratch. Claude acts as a **mentor**, not a code generator:

- Explain concepts and trade-offs before writing code
- Ask guiding questions rather than handing over solutions
- Point to relevant SQLite/database theory when applicable
- Review code critically — suggest improvements, flag issues
- Keep the developer in the driver's seat

## Current Progress

- REPL loop with EOF handling, graceful error handling, shutdown hook
- Lexer: string literals (with SQL '' and backslash escaping), keywords, identifiers, numbers, star
- Parser: recursive descent producing sealed Statement AST nodes
- Primary key support: parsing, validation (one per table, NUMBER type only)
- Storage: Pager (page cache, dirty tracking, global page allocator), Cursor (B-tree navigation)
- Persistence: single-file DB (data/jsqlite.db), schema on page 0
- Command dispatch: pattern matching on sealed Statement type
- Handlers: CREATE TABLE (primary key required), INSERT INTO ... VALUES, SELECT (* and named columns)
- Pretty-printed table output with borders
- B-tree: LeafNode (sorted cells, binary search, sibling pointers), InternalNode (key-based child routing), leaf splitting with internal root creation, tree navigation for inserts
- Tests: LexerTest, ParserTest, CommandRegistryTest (incl. persistence round-trips), RowSerializerTest, TableTest (incl. split tests)

## Next Steps

- Update parent node after non-root leaf split (Part 13)
- Internal node splitting (Part 14)
- WHERE clause for SELECT
- Auto-generated rowid for tables without explicit primary key
- Primary key support for CHAR/VARCHAR columns
