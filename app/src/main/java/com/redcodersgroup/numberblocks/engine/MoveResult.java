package com.redcodersgroup.numberblocks.engine;

import java.util.ArrayList;
import java.util.List;

public class MoveResult {
    private final boolean moved;
    private final int scoreEarned;
    private final List<Tile> mergedTiles;
    private final Tile newTile;
    private final int combo;

    public MoveResult(boolean moved, int scoreEarned, List<Tile> mergedTiles, Tile newTile, int combo) {
        this.moved = moved;
        this.scoreEarned = scoreEarned;
        this.mergedTiles = mergedTiles != null ? mergedTiles : new ArrayList<>();
        this.newTile = newTile;
        this.combo = combo;
    }

    public boolean isMoved() { return moved; }
    public int getScoreEarned() { return scoreEarned; }
    public List<Tile> getMergedTiles() { return mergedTiles; }
    public Tile getNewTile() { return newTile; }
    public int getCombo() { return combo; }
}
