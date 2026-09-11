package com.redcodersgroup.numberblocks.engine;

public class Tile {
    private final long id;
    private int value;
    private int row;
    private int col;
    private int prevRow;
    private int prevCol;
    private boolean merged;
    private boolean isNew;

    private static long ID_COUNTER = 1;

    public Tile(int row, int col, int value) {
        this.id = ID_COUNTER++;
        this.row = row;
        this.col = col;
        this.prevRow = row;
        this.prevCol = col;
        this.value = value;
        this.merged = false;
        this.isNew = true;
    }

    public Tile(long id, int row, int col, int value, int prevRow, int prevCol, boolean merged, boolean isNew) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.prevRow = prevRow;
        this.prevCol = prevCol;
        this.value = value;
        this.merged = merged;
        this.isNew = isNew;
    }

    public Tile copy() {
        return new Tile(id, row, col, value, prevRow, prevCol, merged, isNew);
    }

    public long getId() { return id; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
    public int getPrevRow() { return prevRow; }
    public void setPrevRow(int prevRow) { this.prevRow = prevRow; }
    public int getPrevCol() { return prevCol; }
    public void setPrevCol(int prevCol) { this.prevCol = prevCol; }
    public boolean isMerged() { return merged; }
    public void setMerged(boolean merged) { this.merged = merged; }
    public boolean isNew() { return isNew; }
    public void setNew(boolean isNew) { this.isNew = isNew; }

    public void savePosition() {
        this.prevRow = this.row;
        this.prevCol = this.col;
        this.merged = false;
        this.isNew = false;
    }
}
