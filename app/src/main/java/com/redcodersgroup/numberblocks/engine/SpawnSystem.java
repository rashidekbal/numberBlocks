package com.redcodersgroup.numberblocks.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnSystem {
    private final Random random;

    public SpawnSystem() {
        this.random = new Random();
    }

    public SpawnSystem(long seed) {
        this.random = new Random(seed);
    }

    public List<Position> getEmptyPositions(Tile[][] grid) {
        List<Position> empty = new ArrayList<>();
        int size = grid.length;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == null) {
                    empty.add(new Position(r, c));
                }
            }
        }
        return empty;
    }

    public Tile spawnTile(Tile[][] grid) {
        return spawnTile(grid, null);
    }

    public Tile spawnTile(Tile[][] grid, Direction lastDirection) {
        int size = grid.length;
        List<Position> candidatePositions = new ArrayList<>();

        // Directional edge-spawning: tiles enter from the edge opposite to movement
        if (lastDirection == Direction.DOWN) {
            // Swiped DOWN -> must spawn in top row (row 0)
            for (int c = 0; c < size; c++) {
                if (grid[0][c] == null) {
                    candidatePositions.add(new Position(0, c));
                }
            }
        } else if (lastDirection == Direction.UP) {
            // Swiped UP -> must spawn in bottom row (row size - 1)
            for (int c = 0; c < size; c++) {
                if (grid[size - 1][c] == null) {
                    candidatePositions.add(new Position(size - 1, c));
                }
            }
        } else if (lastDirection == Direction.LEFT) {
            // Swiped LEFT -> must spawn in rightmost column (col size - 1)
            for (int r = 0; r < size; r++) {
                if (grid[r][size - 1] == null) {
                    candidatePositions.add(new Position(r, size - 1));
                }
            }
        } else if (lastDirection == Direction.RIGHT) {
            // Swiped RIGHT -> must spawn in leftmost column (col 0)
            for (int r = 0; r < size; r++) {
                if (grid[r][0] == null) {
                    candidatePositions.add(new Position(r, 0));
                }
            }
        }

        // If no candidate on the preferred edge, fall back to any empty position
        if (candidatePositions.isEmpty()) {
            candidatePositions = getEmptyPositions(grid);
        }

        if (candidatePositions.isEmpty()) {
            return null;
        }

        Position pos = candidatePositions.get(random.nextInt(candidatePositions.size()));
        // SRS: 90% chance of 2, 10% chance of 4
        int value = (random.nextFloat() < 0.9f) ? 2 : 4;
        Tile tile = new Tile(pos.row, pos.col, value);
        grid[pos.row][pos.col] = tile;
        return tile;
    }
}
