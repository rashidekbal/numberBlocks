package com.redcodersgroup.numberblocks.games;

public class LeaderboardEntry {
    private final String rank;
    private final String displayName;
    private final long score;
    private final String formattedScore;
    private final boolean isCurrentPlayer;

    public LeaderboardEntry(String rank, String displayName, long score, String formattedScore, boolean isCurrentPlayer) {
        this.rank = rank;
        this.displayName = displayName;
        this.score = score;
        this.formattedScore = formattedScore;
        this.isCurrentPlayer = isCurrentPlayer;
    }

    public String getRank() {
        return rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getScore() {
        return score;
    }

    public String getFormattedScore() {
        return formattedScore;
    }

    public boolean isCurrentPlayer() {
        return isCurrentPlayer;
    }
}
