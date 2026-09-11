package com.numberblocksmerge.engine;

import java.util.ArrayList;
import java.util.List;

public class GameSnapshot {
    public int[][] gridValues;
    public int score;
    public int bestScore;
    public int combo;
    public int movesCount;
    public boolean isWon;
    public boolean isOver;
    public List<BoardState> undoHistory = new ArrayList<>();
}
