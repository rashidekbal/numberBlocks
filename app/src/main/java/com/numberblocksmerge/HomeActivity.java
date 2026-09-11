package com.numberblocksmerge;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.numberblocksmerge.ads.AdsManager;
import com.numberblocksmerge.audio.HapticManager;
import com.numberblocksmerge.audio.SoundManager;
import com.numberblocksmerge.databinding.ActivityHomeBinding;
import com.numberblocksmerge.engine.GameSnapshot;
import com.numberblocksmerge.storage.PreferencesManager;
import com.numberblocksmerge.theme.Theme;
import com.numberblocksmerge.theme.ThemeManager;
import com.numberblocksmerge.ui.DialogHelper;
import com.numberblocksmerge.ui.StatusBarHelper;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private PreferencesManager prefs;
    private SoundManager soundManager;
    private HapticManager hapticManager;
    private ThemeManager themeManager;
    private AdsManager adsManager;
    private final Gson gson = new Gson();

    private Dialog currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Core Managers
        prefs = PreferencesManager.getInstance(this);
        soundManager = SoundManager.getInstance();
        soundManager.setEnabled(prefs.isSoundEnabled());
        hapticManager = HapticManager.getInstance();
        hapticManager.init(this);
        hapticManager.setEnabled(prefs.isHapticsEnabled());
        themeManager = ThemeManager.getInstance();
        themeManager.setTheme(prefs.getTheme());
        adsManager = AdsManager.getInstance();
        adsManager.init(this);

        // Apply system bar insets to prevent status/navigation bar overlap
        StatusBarHelper.applySystemBarInsets(binding.getRoot());
        applyTheme();

        // Mode clicks
        binding.cardMode4.setOnClickListener(v -> onSelectMode(4));
        binding.cardMode5.setOnClickListener(v -> onSelectMode(5));
        binding.cardMode6.setOnClickListener(v -> onSelectMode(6));

        // Resume active game
        binding.btnHomeResumeRun.setOnClickListener(v -> {
            int lastSize = prefs.getLastPlayedSize();
            launchGame(lastSize, true);
        });

        // Quick Controls
        binding.btnHomeSettings.setOnClickListener(v -> showSettingsDialog());
        binding.btnHomeStats.setOnClickListener(v -> showStatsDialog());
        binding.btnHomeThemes.setOnClickListener(v -> showThemeDialog());
        binding.btnHomeHelp.setOnClickListener(v -> showHelpDialog());

        // Load banner
        adsManager.loadBanner(this, binding.homeBannerContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
        updateDashboard();
    }

    @Override
    protected void onDestroy() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        super.onDestroy();
    }

    public void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        if (binding != null) {
            binding.getRoot().setBackgroundColor(theme.backgroundColor);
        }
        StatusBarHelper.updateSystemBars(this, theme.backgroundColor);
    }

    private void updateDashboard() {
        binding.tvBest4.setText(getString(R.string.best_format, String.format(Locale.getDefault(), "%,d", prefs.getBestScore(4))));
        binding.tvBest5.setText(getString(R.string.best_format, String.format(Locale.getDefault(), "%,d", prefs.getBestScore(5))));
        binding.tvBest6.setText(getString(R.string.best_format, String.format(Locale.getDefault(), "%,d", prefs.getBestScore(6))));

        // Check for active in-progress game
        int lastSize = prefs.getLastPlayedSize();
        String activeState = prefs.getActiveGame(lastSize);
        if (activeState != null) {
            try {
                GameSnapshot snapshot = gson.fromJson(activeState, GameSnapshot.class);
                if (snapshot != null && !snapshot.isOver) {
                    int size = (snapshot.gridValues != null) ? snapshot.gridValues.length : lastSize;
                    binding.cardHomeResume.setVisibility(View.VISIBLE);
                    binding.tvHomeResumeBadge.setText(getString(R.string.active_run_format, size + "×" + size));
                    binding.tvHomeResumeScore.setText(String.format(Locale.getDefault(), "%,d", snapshot.score));
                    return;
                }
            } catch (Exception ignored) {}
        }
        binding.cardHomeResume.setVisibility(View.GONE);
    }

    private void onSelectMode(int size) {
        String activeState = prefs.getActiveGame(size);
        if (activeState != null) {
            try {
                GameSnapshot snapshot = gson.fromJson(activeState, GameSnapshot.class);
                if (snapshot != null && !snapshot.isOver) {
                    currentDialog = DialogHelper.showResumePrompt(this, size, snapshot.score,
                            () -> launchGame(size, true),
                            () -> {
                                prefs.clearActiveGame(size);
                                launchGame(size, false);
                            });
                    return;
                }
            } catch (Exception ignored) {}
        }
        launchGame(size, false);
    }

    private void launchGame(int size, boolean resume) {
        hapticManager.click();
        soundManager.playMove();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_BOARD_SIZE, size);
        intent.putExtra(MainActivity.EXTRA_RESUME, resume);
        startActivity(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void showSettingsDialog() {
        hapticManager.click();
        currentDialog = DialogHelper.showSettings(this, soundManager.isEnabled(), hapticManager.isEnabled(),
                new DialogHelper.SettingsListener() {
                    @Override
                    public void onSoundToggled(boolean enabled) {
                        soundManager.setEnabled(enabled);
                        prefs.setSoundEnabled(enabled);
                    }

                    @Override
                    public void onHapticsToggled(boolean enabled) {
                        hapticManager.setEnabled(enabled);
                        prefs.setHapticsEnabled(enabled);
                    }

                    @Override
                    public void onChangeThemeRequested() {
                        showThemeDialog();
                    }

                    @Override
                    public void onInfoClicked() {
                        hapticManager.click();
                        Intent intent = new Intent(HomeActivity.this, InfoActivity.class);
                        startActivity(intent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,
                                    android.R.anim.fade_in, android.R.anim.fade_out);
                        } else {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        }
                    }
                });
    }

    private void showStatsDialog() {
        hapticManager.click();
        currentDialog = DialogHelper.showCareerStats(this, prefs.getGamesPlayed(), prefs.getHighestTile(),
                prefs.getBestScore(4), prefs.getBestScore(5), prefs.getBestScore(6));
    }

    private void showThemeDialog() {
        hapticManager.click();
        currentDialog = DialogHelper.showThemePicker(this, themeManager.getCurrentTheme().id, themeKey -> {
            themeManager.setTheme(themeKey);
            prefs.setTheme(themeKey);
            applyTheme();
        });
    }

    private void showHelpDialog() {
        hapticManager.click();
        currentDialog = DialogHelper.showTutorial(this);
    }
}
