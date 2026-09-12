package com.redcodersgroup.numberblocks.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class GameEngine {
    private final int size;
    private final Tile[][] grid;
    private final SpawnSystem spawnSystem;
    private final MoveResolver moveResolver;
    private final GameOverSystem gameOverSystem;

    private int score;
    private int bestScore;
    private int combo;
    private int movesCount;
    private boolean isWon;
    private boolean isOver;
    private final Stack<BoardState> history;
    private final Set<Integer> unlockedMilestones = new HashSet<>();

    public interface Listener {
        void onScoreChanged(int score, int bestScore, int combo);
        void onGameOver(int finalScore, int bestScore, int highestTile);
        void onMilestoneReached(int milestone);
    }

    private Listener listener;

    public GameEngine(int size) {
        this.size = size;
        this.grid = new Tile[size][size];
        this.spawnSystem = new SpawnSystem();
        this.moveResolver = new MoveResolver();
        this.gameOverSystem = new GameOverSystem();
        this.history = new Stack<>();
        this.score = 0;
        this.bestScore = 0;
        this.combo = 0;
        this.movesCount = 0;
        this.isWon = false;
        this.isOver = false;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void startNewGame() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = null;
            }
        }
        history.clear();
        unlockedMilestones.clear();
        score = 0;
        combo = 0;
        movesCount = 0;
        isWon = false;
        isOver = false;

        spawnSystem.spawnTile(grid);
        spawnSystem.spawnTile(grid);

        if (listener != null) {
            listener.onScoreChanged(score, bestScore, combo);
        }
    }

    public MoveResult move(Direction direction) {
        if (isOver) {
            return new MoveResult(false, 0, null, null, combo);
        }

        BoardState preMoveState = new BoardState(grid, score, combo, movesCount);
        MoveResolver.StepResult step = moveResolver.resolveMove(grid, direction);

        if (!step.moved) {
            return new MoveResult(false, 0, null, null, combo);
        }

        history.push(preMoveState);
        if (history.size() > 10) {
            history.remove(0);
        }

        movesCount++;

        if (!step.mergedTiles.isEmpty()) {
            combo++;
        } else {
            combo = 0;
        }

        int comboBonus = (combo > 1) ? (step.scoreEarned * (combo - 1) / 4) : 0;
        int totalEarned = step.scoreEarned + comboBonus;
        score += totalEarned;

        if (score > bestScore) {
            bestScore = score;
        }

        // Check milestones (128, 256, 512, 1024, 2048, 4096...)
        for (Tile t : step.mergedTiles) {
            int val = t.getValue();
            if (val >= 128 && !unlockedMilestones.contains(val)) {
                unlockedMilestones.add(val);
                if (val == 2048) isWon = true;
                if (listener != null) {
                    listener.onMilestoneReached(val);
                }
            }
        }

        Tile spawned = spawnSystem.spawnTile(grid, direction);

        if (gameOverSystem.isGameOver(grid)) {
            isOver = true;
            if (listener != null) {
                listener.onGameOver(score, bestScore, getHighestTile());
            }
        }

        if (listener != null) {
            listener.onScoreChanged(score, bestScore, combo);
        }

        return new MoveResult(true, totalEarned, step.mergedTiles, spawned, combo);
    }

    public boolean undo() {
        if (history.isEmpty()) return false;

        BoardState prev = history.pop();
        this.score = prev.score;
        this.combo = prev.combo;
        this.movesCount = prev.movesCount;
        this.isOver = false;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int val = prev.values[r][c];
                grid[r][c] = (val > 0) ? new Tile(r, c, val) : null;
            }
        }

        if (listener != null) {
            listener.onScoreChanged(score, bestScore, combo);
        }
        return true;
    }

    public boolean revive() {
        this.isOver = false;

        // Collect all non-empty tiles on the grid
        List<Tile> tiles = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != null) {
                    tiles.add(grid[r][c]);
                }
            }
        }

        if (tiles.isEmpty()) {
            spawnSystem.spawnTile(grid);
            spawnSystem.spawnTile(grid);
            if (listener != null) {
                listener.onScoreChanged(score, bestScore, combo);
            }
            return true;
        }

        // Sort tiles by value ascending (lowest value tiles first)
        tiles.sort((a, b) -> Integer.compare(a.getValue(), b.getValue()));

        // Clear lowest-tier tiles to guarantee open playable space
        int tilesToClear = Math.min(tiles.size() - 2, Math.max(3, size));
        for (int i = 0; i < tilesToClear; i++) {
            Tile t = tiles.get(i);
            grid[t.getRow()][t.getCol()] = null;
        }

        // Push revived state so undo is enabled
        history.push(new BoardState(grid, score, combo, movesCount));
        if (history.size() > 10) {
            history.remove(0);
        }

        if (listener != null) {
            listener.onScoreChanged(score, bestScore, combo);
        }

        return true;
    }

    public GameSnapshot createSnapshot() {
        GameSnapshot s = new GameSnapshot();
        s.gridValues = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                s.gridValues[r][c] = (grid[r][c] != null) ? grid[r][c].getValue() : 0;
            }
        }
        s.score = this.score;
        s.bestScore = this.bestScore;
        s.combo = this.combo;
        s.movesCount = this.movesCount;
        s.isWon = this.isWon;
        s.isOver = this.isOver;
        s.undoHistory = new ArrayList<>(this.history);
        return s;
    }

    public boolean restoreFromSnapshot(GameSnapshot s) {
        if (s == null || s.gridValues == null) return false;
        this.score = s.score;
        this.bestScore = s.bestScore;
        this.combo = s.combo;
        this.movesCount = s.movesCount;
        this.isWon = s.isWon;
        this.isOver = s.isOver;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int val = s.gridValues[r][c];
                grid[r][c] = (val > 0) ? new Tile(r, c, val) : null;
            }
        }

        history.clear();
        if (s.undoHistory != null) {
            history.addAll(s.undoHistory);
        }

        if (listener != null) {
            listener.onScoreChanged(score, bestScore, combo);
        }
        return true;
    }

    public int getHighestTile() {
        int max = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] != null && grid[r][c].getValue() > max) {
                    max = grid[r][c].getValue();
                }
            }
        }
        return max;
    }

    public Tile[][] getGrid() { return grid; }
    public int getSize() { return size; }
    public int getScore() { return score; }
    public int getBestScore() { return bestScore; }
    public void setBestScore(int best) { this.bestScore = best; }
    public int getCombo() { return combo; }
    public int getMovesCount() { return movesCount; }
    public boolean isOver() { return isOver; }
    public boolean canUndo() { return !history.isEmpty(); }
    public int getUndoCount() { return history.size(); }
}
