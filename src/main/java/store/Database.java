package store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private final Map<String, Table> tableMap;

    public Database() {
        tableMap = new HashMap<>();
    }

    public void createTable(String name, List<Column> columns) {
        tableMap.put(name, new Table(columns));
    }

    public Table getTable(String name) {
        return tableMap.get(name);
    }
}
