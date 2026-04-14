package store;

public interface DatabaseConstants {

    String DATABASE_FILE_PATH = "data/jsqlite.db";
    int PAGE_SIZE = 4096;
    int MAX_PAGES = 1_000;
    int TABLE_ROW_START_OFFSET = 4;
}
