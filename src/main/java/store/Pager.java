package store;

import java.io.IOException;
import java.io.RandomAccessFile;

import static store.DatabaseConstants.MAX_PAGES;
import static store.DatabaseConstants.PAGE_SIZE;

public class Pager {

    private final RandomAccessFile randomAccessFile;
    private final byte[][] pages;
    private final boolean[] dirtyPages;

    public Pager(String databaseFilePath) {
        try {
            randomAccessFile = new RandomAccessFile(databaseFilePath, "rw");
        } catch (IOException e) {
            throw new StorageException("Failed to open database file: " + databaseFilePath, e);
        }
        pages = new byte[MAX_PAGES][];
        dirtyPages = new boolean[MAX_PAGES];
    }

    public byte[] getPage(int pageNumber) {
        if (pages[pageNumber] == null) {
            pages[pageNumber] = new byte[PAGE_SIZE];

            try {
                long seekPos = (long) pageNumber * PAGE_SIZE;
                randomAccessFile.seek(seekPos);
                if (seekPos < randomAccessFile.length()) {
                    randomAccessFile.read(pages[pageNumber]);
                }
            } catch (IOException e) {
                throw new StorageException("Failed to read page " + pageNumber, e);
            }
        }
        return pages[pageNumber];
    }

    public void markDirty(int pageNumber) {
        dirtyPages[pageNumber] = true;
    }

    public void flush() {
        try {
            for (int i = 0; i < MAX_PAGES; i++) {
                if (dirtyPages[i]) {
                    long seekPos = (long) i * PAGE_SIZE;
                    randomAccessFile.seek(seekPos);
                    randomAccessFile.write(pages[i]);
                    dirtyPages[i] = false;
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to flush pages to disk", e);
        }
    }

    public void close() {
        try {
            flush();
            randomAccessFile.close();
        } catch (IOException e) {
            throw new StorageException("Failed to close database file", e);
        }
    }

}
