package com.numberblocksmerge.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "number_blocks_prefs";
    private static PreferencesManager instance;
    private final SharedPreferences prefs;

    private PreferencesManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) instance = new PreferencesManager(context);
        return instance;
    }

    // Per-mode Best Scores (4x4, 5x5, 6x6)
    public int getBestScore(int size) {
        return prefs.getInt("best_score_" + size, 0);
    }

    public void setBestScore(int size, int score) {
        if (score > getBestScore(size)) {
            prefs.edit().putInt("best_score_" + size, score).apply();
        }
    }

    public String getTheme() { return prefs.getString("selected_theme", "alabaster"); }
    public void setTheme(String theme) { prefs.edit().putString("selected_theme", theme).apply(); }

    public boolean isSoundEnabled() { return prefs.getBoolean("sound_enabled", true); }
    public void setSoundEnabled(boolean enabled) { prefs.edit().putBoolean("sound_enabled", enabled).apply(); }

    public boolean isHapticsEnabled() { return prefs.getBoolean("haptics_enabled", true); }
    public void setHapticsEnabled(boolean enabled) { prefs.edit().putBoolean("haptics_enabled", enabled).apply(); }

    public int getGamesPlayed() { return prefs.getInt("games_played", 0); }
    public void incrementGamesPlayed() { prefs.edit().putInt("games_played", getGamesPlayed() + 1).apply(); }

    public int getHighestTile() { return prefs.getInt("highest_tile", 0); }
    public void recordHighestTile(int tile) {
        if (tile > getHighestTile()) prefs.edit().putInt("highest_tile", tile).apply();
    }

    public int getLastPlayedSize() { return prefs.getInt("last_played_size", 4); }
    public void setLastPlayedSize(int size) { prefs.edit().putInt("last_played_size", size).apply(); }

    // Active Game State Persistence per mode
    public void saveActiveGame(int size, String jsonState) {
        prefs.edit().putString("active_game_" + size, jsonState).apply();
        setLastPlayedSize(size);
    }

    public String getActiveGame(int size) {
        return prefs.getString("active_game_" + size, null);
    }

    public void clearActiveGame(int size) {
        prefs.edit().remove("active_game_" + size).apply();
    }

    public boolean hasAnyActiveGame() {
        return getActiveGame(4) != null || getActiveGame(5) != null || getActiveGame(6) != null;
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean("is_first_launch", true);
    }

    public void setFirstLaunch(boolean isFirstLaunch) {
        prefs.edit().putBoolean("is_first_launch", isFirstLaunch).apply();
    }
}
