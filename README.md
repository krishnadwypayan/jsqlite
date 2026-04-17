# JSQLite

A SQLite clone built from scratch in Java, following [cstack's db_tutorial](https://cstack.github.io/db_tutorial/).

## What is this?

A learning project to understand database internals by implementing:
- A SQL lexer and recursive descent parser
- Sealed AST nodes with pattern-matching dispatch
- Row serialization and fixed-size page storage
- Disk persistence with a Pager (page cache + dirty tracking)
- Schema management (page 0 stores table metadata)
- B-tree leaf node format (in progress)
- A REPL for interactive queries

## Getting Started

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Start the REPL
./gradlew run
```

Data is stored in `data/jsqlite.db`.

## Project Structure

| Package   | Purpose                                              |
|-----------|------------------------------------------------------|
| `lexer`   | SQL lexer, tokens, keywords                          |
| `parser`  | Recursive descent parser, sealed AST nodes           |
| `store`   | Tables, rows, columns, Pager, Cursor, schema, DB     |
| `btree`   | B-tree nodes (Node, LeafNode, InternalNode)          |
| `command` | Command registry, handlers, dispatch                 |

## Status

- [x] REPL with graceful error handling
- [x] Lexer (string literals, escape sequences, keywords)
- [x] Parser / AST (sealed Statement, recursive descent)
- [x] Storage layer (Table, RowSerializer, Cursor)
- [x] Command dispatch (pattern matching on Statement type)
- [x] Disk persistence (Pager, SchemaSerializer, shutdown hook)
- [x] Primary key support (parsing + validation)
- [x] Pretty-printed SELECT output with borders
- [x] B-tree leaf node format with sorted insertion
- [x] Binary search and duplicate key detection
- [x] Leaf node splitting with internal root creation
- [x] Sibling pointers for sequential leaf traversal
- [x] Tree navigation for inserts (key-based child lookup)
- [ ] Parent node updates after non-root split (Part 13)
- [ ] Internal node splitting (Part 14)
- [ ] WHERE clause for SELECT
