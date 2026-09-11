package com.numberblocksmerge.engine;

import java.util.ArrayList;
import java.util.List;

public class MoveResolver {

    public static class StepResult {
        public final boolean moved;
        public final int scoreEarned;
        public final List<Tile> mergedTiles;

        public StepResult(boolean moved, int scoreEarned, List<Tile> mergedTiles) {
            this.moved = moved;
            this.scoreEarned = scoreEarned;
            this.mergedTiles = mergedTiles;
        }
    }

    public StepResult resolveMove(Tile[][] grid, Direction direction) {
        int size = grid.length;
        boolean moved = false;
        int score = 0;
        List<Tile> mergedTiles = new ArrayList<>();

        // Save previous positions and reset flags
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != null) {
                    grid[r][c].savePosition();
                }
            }
        }

        // Determine traversal order based on direction
        int startR = 0, endR = size, stepR = 1;
        int startC = 0, endC = size, stepC = 1;

        if (direction == Direction.DOWN) {
            startR = size - 1;
            endR = -1;
            stepR = -1;
        } else if (direction == Direction.RIGHT) {
            startC = size - 1;
            endC = -1;
            stepC = -1;
        }

        int dr = 0;
        int dc = 0;
        switch (direction) {
            case UP:    dr = -1; break;
            case DOWN:  dr = 1;  break;
            case LEFT:  dc = -1; break;
            case RIGHT: dc = 1;  break;
        }

        for (int r = startR; r != endR; r += stepR) {
            for (int c = startC; c != endC; c += stepC) {
                Tile current = grid[r][c];
                if (current == null) continue;

                int currR = r;
                int currC = c;

                while (true) {
                    int nextR = currR + dr;
                    int nextC = currC + dc;

                    // Out of bounds
                    if (nextR < 0 || nextR >= size || nextC < 0 || nextC >= size) {
                        break;
                    }

                    Tile target = grid[nextR][nextC];
                    if (target == null) {
                        // Empty cell: slide into it
                        grid[nextR][nextC] = current;
                        grid[currR][currC] = null;
                        current.setRow(nextR);
                        current.setCol(nextC);
                        currR = nextR;
                        currC = nextC;
                        moved = true;
                    } else if (target.getValue() == current.getValue() && !target.isMerged()) {
                        // Merge!
                        int newValue = target.getValue() * 2;
                        target.setValue(newValue);
                        target.setMerged(true);
                        grid[currR][currC] = null;
                        score += newValue;
                        mergedTiles.add(target);
                        moved = true;
                        break;
                    } else {
                        // Blocked by another tile
                        break;
                    }
                }
            }
        }

        return new StepResult(moved, score, mergedTiles);
    }
}
