package com.godot.community.entity;

/**
 * Encapsulation about paging.
 */
public class Page {

    // current page idx
    private int current = 1;
    // maximum
    private int limit = 10;
    // data counts (calculate total pages)
    private int rows;
    // query path (reuse paging link)
    private String path;
    private int offset;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1)
            this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 & limit <= 100)
            this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0)
            this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get current begin (initial) row
     *
     * @return
     */
    public int getOffSet() {
        // current page * limit - limit
        return (current - 1) * limit;
    }

    /**
     * Get total pages
     */
    public int getTotal() {
        // rows/ limit +1
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * Get initial page number
     */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * Get fin page number
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }


}
