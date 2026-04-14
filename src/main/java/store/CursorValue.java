package store;

public record CursorValue(byte[] page, int pageNumber, int rowOffset) {
}
