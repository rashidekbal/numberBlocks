package com.redcodersgroup.numberblocks;

import com.redcodersgroup.numberblocks.engine.Direction;
import com.redcodersgroup.numberblocks.engine.GameEngine;
import com.redcodersgroup.numberblocks.engine.GameSnapshot;
import com.redcodersgroup.numberblocks.engine.MoveResult;
import com.redcodersgroup.numberblocks.engine.Tile;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameEngineTest {

    @Test
    public void testClassic4x4StartsWithTwoTiles() {
        GameEngine engine = new GameEngine(4);
        engine.startNewGame();
        Tile[][] grid = engine.getGrid();
        int count = 0;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (grid[r][c] != null) count++;
            }
        }
        assertEquals("4x4 grid must start with exactly 2 tiles", 2, count);
    }

    @Test
    public void testExpanded5x5StartsWithTwoTiles() {
        GameEngine engine = new GameEngine(5);
        engine.startNewGame();
        Tile[][] grid = engine.getGrid();
        int count = 0;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (grid[r][c] != null) count++;
            }
        }
        assertEquals("5x5 grid must start with exactly 2 tiles", 2, count);
    }

    @Test
    public void testMaster6x6StartsWithTwoTiles() {
        GameEngine engine = new GameEngine(6);
        engine.startNewGame();
        Tile[][] grid = engine.getGrid();
        int count = 0;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                if (grid[r][c] != null) count++;
            }
        }
        assertEquals("6x6 grid must start with exactly 2 tiles", 2, count);
    }

    @Test
    public void testUndoStackRestoresState() {
        GameEngine engine = new GameEngine(4);
        engine.startNewGame();
        Tile[][] grid = engine.getGrid();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) grid[r][c] = null;
        }
        grid[0][0] = new Tile(0, 0, 2);
        grid[0][1] = new Tile(0, 1, 2);

        MoveResult res = engine.move(Direction.LEFT);
        assertTrue("Move left must merge the two 2s", res.isMoved());
        assertEquals("Score should be 4", 4, engine.getScore());
        assertTrue("Undo must be available", engine.canUndo());

        boolean undone = engine.undo();
        assertTrue("Undo should succeed", undone);
        assertEquals("Score should be reset to 0", 0, engine.getScore());
    }

    @Test
    public void testAdjacentIdenticalMerge() {
        GameEngine engine = new GameEngine(4);
        engine.startNewGame();
        Tile[][] grid = engine.getGrid();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) grid[r][c] = null;
        }
        grid[0][0] = new Tile(0, 0, 4);
        grid[0][1] = new Tile(0, 1, 4);
        grid[0][2] = new Tile(0, 2, 4);
        grid[0][3] = new Tile(0, 3, 4);

        MoveResult res = engine.move(Direction.LEFT);
        assertTrue(res.isMoved());
        assertEquals("Should merge 4+4 and 4+4 = 8, 8", 16, res.getScoreEarned());
    }

    @Test
    public void testSnapshotAndRestore() {
        GameEngine engine = new GameEngine(5);
        engine.startNewGame();
        Tile[][] grid = engine.getGrid();
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) grid[r][c] = null;
        }
        grid[1][1] = new Tile(1, 1, 1024);
        grid[2][2] = new Tile(2, 2, 512);

        GameSnapshot snapshot = engine.createSnapshot();
        assertNotNull(snapshot);
        assertEquals(1024, snapshot.gridValues[1][1]);
        assertEquals(512, snapshot.gridValues[2][2]);

        GameEngine restoredEngine = new GameEngine(5);
        boolean ok = restoredEngine.restoreFromSnapshot(snapshot);
        assertTrue(ok);
        assertEquals(1024, restoredEngine.getHighestTile());
    }

    @Test
    public void testDirectionalSpawningOnMoveDown() {
        GameEngine engine = new GameEngine(4);
        engine.startNewGame();

        for (int i = 0; i < 20; i++) {
            MoveResult result = engine.move(Direction.DOWN);
            if (result.isMoved() && result.getNewTile() != null) {
                // If top row had empty cells, the new tile must be in row 0
                assertEquals("When swiping DOWN, new tile must spawn in top row (row 0)", 0, result.getNewTile().getRow());
            }
        }
    }

    @Test
    public void testMergeBeyond2048ToArbitraryHighValues() {
        GameEngine engine = new GameEngine(4);
        engine.startNewGame();
        Tile[][] grid = engine.getGrid();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) grid[r][c] = null;
        }

        // Place two 2048 tiles
        grid[0][0] = new Tile(0, 0, 2048);
        grid[0][1] = new Tile(0, 1, 2048);

        final boolean[] milestone4096Triggered = {false};
        engine.setListener(new GameEngine.Listener() {
            @Override
            public void onScoreChanged(int score, int bestScore, int combo) {}
            @Override
            public void onGameOver(int score, int bestScore, int highestTile) {}
            @Override
            public void onMilestoneReached(int milestone) {
                if (milestone == 4096) {
                    milestone4096Triggered[0] = true;
                }
            }
        });

        MoveResult res = engine.move(Direction.LEFT);
        assertTrue(res.isMoved());
        assertEquals("Merged value should be 4096", 4096, engine.getHighestTile());
        assertTrue("Milestone 4096 must trigger", milestone4096Triggered[0]);
        assertFalse("Game must continue and not be over after reaching 4096", engine.isOver());

        // Now place another 4096 and merge to 8192
        grid[0][1] = new Tile(0, 1, 4096);
        MoveResult res2 = engine.move(Direction.LEFT);
        assertTrue(res2.isMoved());
        assertEquals("Merged value should be 8192", 8192, engine.getHighestTile());
        assertFalse("Game must continue and not be over after reaching 8192", engine.isOver());
    }
}
