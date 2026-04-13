package store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private static final String DATABASE_FILE_PATH = "data/jsqlite.db";

    private final Map<String, Table> tableMap;
    private final Pager pager;
    private final SchemaSerializer schemaSerializer;
    private List<TableMetadata> tableMetadataList;

    private int nextFreePage = 1;
    private static final int PAGES_PER_TABLE = 100;

    public Database() {
        tableMap = new HashMap<>();
        pager = new Pager(DATABASE_FILE_PATH);
        schemaSerializer = new SchemaSerializer();
        init();
    }

    private void init() {
        byte[] schemaPage = pager.getPage(0);
        tableMetadataList = schemaSerializer.deserialize(schemaPage);

        for (TableMetadata tableMetadata : tableMetadataList) {
            tableMap.put(tableMetadata.tableName(), new Table(pager, tableMetadata.startPage(), tableMetadata.columns()));
            nextFreePage = tableMetadata.startPage() + PAGES_PER_TABLE;
        }
    }

    public void createTable(String name, List<Column> columns) {
        int startPage = getNextFreePage();
        tableMetadataList.add(new TableMetadata(name, startPage, columns));
        tableMap.put(name, new Table(pager, startPage, columns));
        schemaSerializer.serialize(tableMetadataList, pager.getPage(0));
        pager.markDirty(0);
    }

    public Table getTable(String name) {
        return tableMap.get(name);
    }

    public void close() {
        pager.close();
    }

    private int getNextFreePage() {
        int retVal = nextFreePage;
        nextFreePage += PAGES_PER_TABLE;
        return retVal;
    }
}
