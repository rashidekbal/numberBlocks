package com.redcodersgroup.numberblocks.audio;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class HapticManager {
    private static HapticManager instance;
    private Vibrator vibrator;
    private boolean enabled = true;

    private HapticManager() {}

    public static synchronized HapticManager getInstance() {
        if (instance == null) instance = new HapticManager();
        return instance;
    }

    public void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vm != null) {
                vibrator = vm.getDefaultVibrator();
            }
        }
        if (vibrator == null) {
            initLegacyVibrator(context);
        }
    }

    @SuppressWarnings("deprecation")
    private void initLegacyVibrator(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    public void click() {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrateLegacy(15);
        }
    }

    public void heavyClick() {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrateLegacy(40);
        }
    }

    @SuppressWarnings("deprecation")
    private void vibrateLegacy(long milliseconds) {
        if (vibrator != null) {
            vibrator.vibrate(milliseconds);
        }
    }
}
