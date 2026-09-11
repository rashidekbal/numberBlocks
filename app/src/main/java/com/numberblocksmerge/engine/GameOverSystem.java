package com.numberblocksmerge.engine;

public class GameOverSystem {

    public boolean isGameOver(Tile[][] grid) {
        int size = grid.length;

        // Check for any empty cells
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == null) {
                    return false;
                }
            }
        }

        // Check for any possible adjacent merges
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int val = grid[r][c].getValue();

                // Check right neighbor
                if (c + 1 < size && grid[r][c + 1] != null && grid[r][c + 1].getValue() == val) {
                    return false;
                }

                // Check bottom neighbor
                if (r + 1 < size && grid[r + 1][c] != null && grid[r + 1][c].getValue() == val) {
                    return false;
                }
            }
        }

        return true;
    }
}
