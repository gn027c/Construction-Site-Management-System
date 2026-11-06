package view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Pagination<T> {
    private final int pageSize;
    private List<T> items;
    private int pageIndex;

    public Pagination(List<T> items, int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be positive.");
        }
        this.pageSize = pageSize;
        this.items = new ArrayList<>(items == null ? Collections.<T>emptyList() : items);
    }

    public List<T> currentPage() {
        int from = pageIndex * pageSize;
        int to = Math.min(from + pageSize, items.size());
        if (from >= items.size()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(items.subList(from, to));
    }

    public boolean next() {
        if (pageIndex + 1 >= pageCount()) {
            return false;
        }
        pageIndex++;
        return true;
    }

    public boolean previous() {
        if (pageIndex == 0) {
            return false;
        }
        pageIndex--;
        return true;
    }

    public void first() {
        pageIndex = 0;
    }

    public void last() {
        pageIndex = Math.max(0, pageCount() - 1);
    }

    public int pageNumber() {
        return pageIndex + 1;
    }

    public int pageCount() {
        return Math.max(1, (items.size() + pageSize - 1) / pageSize);
    }

    public int pageSize() {
        return pageSize;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
