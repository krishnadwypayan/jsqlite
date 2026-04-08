# JSQLite

A SQLite clone built from scratch in Java, following [cstack's db_tutorial](https://cstack.github.io/db_tutorial/).

## What is this?

A learning project to understand database internals by implementing:
- A SQL lexer and parser
- Row serialization and fixed-size page storage
- A REPL for interactive queries
- (Planned) B-tree indexing, persistence, and more

## Getting Started

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Start the REPL
./gradlew run
```

## Project Structure

| Package   | Purpose                                    |
|-----------|--------------------------------------------|
| `parser`  | SQL lexer, tokens, keywords                |
| `store`   | Tables, rows, columns, serialization       |
| `command` | Command registry, handlers, dispatch       |

## Status

- [x] REPL
- [x] Lexer / tokenizer
- [x] Storage layer (Table, RowSerializer)
- [x] Command dispatch (insert, select, create table)
- [ ] Parser / AST
- [ ] B-tree storage engine
- [ ] Persistence to disk
