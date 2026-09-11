package com.numberblocksmerge.engine;

public class BoardState {
    public final int[][] values;
    public final int score;
    public final int combo;
    public final int movesCount;

    public BoardState(Tile[][] grid, int score, int combo, int movesCount) {
        int size = grid.length;
        this.values = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                this.values[r][c] = (grid[r][c] != null) ? grid[r][c].getValue() : 0;
            }
        }
        this.score = score;
        this.combo = combo;
        this.movesCount = movesCount;
    }

    public BoardState(int[][] values, int score, int combo, int movesCount) {
        this.values = values;
        this.score = score;
        this.combo = combo;
        this.movesCount = movesCount;
    }
}
