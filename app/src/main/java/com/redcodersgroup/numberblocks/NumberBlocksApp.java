package com.redcodersgroup.numberblocks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.redcodersgroup.numberblocks.audio.AmbientMusicManager;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;

/**
 * Main Application class that coordinates continuous app-wide background ambient music
 * across all activities (Splash -> Home -> Gameplay -> Info) with smooth lifecycle transitions.
 */
public class NumberBlocksApp extends Application implements Application.ActivityLifecycleCallbacks {

    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pauseTask;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        PreferencesManager prefs = PreferencesManager.getInstance(this);
        AmbientMusicManager musicManager = AmbientMusicManager.getInstance();
        musicManager.setEnabled(prefs.isMusicEnabled());
        if (prefs.isMusicEnabled()) {
            musicManager.start();
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App comes to foreground from background
            cancelPendingPause();
            PreferencesManager prefs = PreferencesManager.getInstance(this);
            if (prefs.isMusicEnabled()) {
                AmbientMusicManager.getInstance().resume();
            }
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        cancelPendingPause();
        PreferencesManager prefs = PreferencesManager.getInstance(this);
        if (prefs.isMusicEnabled()) {
            AmbientMusicManager.getInstance().resume();
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        schedulePauseCheck();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            schedulePauseCheck();
        }
    }

    private void cancelPendingPause() {
        if (pauseTask != null) {
            handler.removeCallbacks(pauseTask);
            pauseTask = null;
        }
    }

    private void schedulePauseCheck() {
        cancelPendingPause();
        pauseTask = () -> {
            if (activityReferences <= 0) {
                AmbientMusicManager.getInstance().pause();
            }
        };
        // 500ms grace window so transitioning between Splash -> Home -> Game doesn't pause the music
        handler.postDelayed(pauseTask, 500);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
