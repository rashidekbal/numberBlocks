package com.redcodersgroup.numberblocks.analytics;

import android.content.Context;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsManager {
    private static AnalyticsManager instance;
    private FirebaseAnalytics firebaseAnalytics;

    private AnalyticsManager(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
    }

    public static synchronized AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context);
        }
        return instance;
    }

    public void logScreenView(String screenName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    public void logGameStart(int size) {
        Bundle bundle = new Bundle();
        bundle.putInt("board_size", size);
        firebaseAnalytics.logEvent("game_start", bundle);
    }

    public void logGameResume(int size, long score) {
        Bundle bundle = new Bundle();
        bundle.putInt("board_size", size);
        bundle.putLong("score", score);
        firebaseAnalytics.logEvent("game_resume", bundle);
    }

    public void logThemeChanged(String themeKey) {
        Bundle bundle = new Bundle();
        bundle.putString("theme_key", themeKey);
        firebaseAnalytics.logEvent("theme_changed", bundle);
    }
}
