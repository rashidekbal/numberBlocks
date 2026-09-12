package com.redcodersgroup.numberblocks;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.redcodersgroup.numberblocks.ads.AdsManager;
import com.redcodersgroup.numberblocks.analytics.AnalyticsManager;
import com.redcodersgroup.numberblocks.audio.HapticManager;
import com.redcodersgroup.numberblocks.audio.SoundManager;
import com.redcodersgroup.numberblocks.databinding.ActivityHomeBinding;
import com.redcodersgroup.numberblocks.engine.GameSnapshot;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;
import com.redcodersgroup.numberblocks.ui.DialogHelper;
import com.redcodersgroup.numberblocks.ui.StatusBarHelper;
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

        AnalyticsManager.getInstance(this).logScreenView("Home");

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

            // Top Bar
            binding.tvHomeTitle.setTextColor(theme.textPrimaryColor);
            binding.tvHomeSubtitle.setTextColor(theme.textSecondaryColor);

            GradientDrawable settingsCircle = new GradientDrawable();
            settingsCircle.setShape(GradientDrawable.OVAL);
            settingsCircle.setColor(theme.btnSurfaceColor);
            settingsCircle.setStroke((int) (1 * getResources().getDisplayMetrics().density), theme.btnStrokeColor);
            binding.btnHomeSettings.setBackground(settingsCircle);
            binding.btnHomeSettings.setImageTintList(ColorStateList.valueOf(theme.textPrimaryColor));

            // Resume Active Run Card
            binding.cardHomeResume.setCardBackgroundColor(theme.cardBackgroundColor);
            binding.cardHomeResume.setStrokeColor(theme.cardStrokeColor);
            binding.tvHomeResumeScore.setTextColor(theme.textPrimaryColor);
            if (theme.isDark) {
                binding.tvHomeResumeBadge.setBackgroundColor(Color.parseColor("#3D2612"));
                binding.tvHomeResumeBadge.setTextColor(Color.parseColor("#FBBF24"));
                binding.btnHomeResumeRun.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F3F7")));
                binding.btnHomeResumeRun.setTextColor(Color.parseColor("#181A1F"));
            } else {
                binding.tvHomeResumeBadge.setBackgroundColor(Color.parseColor("#FEF3C7"));
                binding.tvHomeResumeBadge.setTextColor(Color.parseColor("#B45309"));
                binding.btnHomeResumeRun.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E2024")));
                binding.btnHomeResumeRun.setTextColor(Color.parseColor("#FFFFFF"));
            }

            // Mode Cards (4x4, 5x5, 6x6)
            binding.cardMode4.setCardBackgroundColor(theme.cardBackgroundColor);
            binding.cardMode4.setStrokeColor(theme.cardStrokeColor);
            binding.tvMode4Title.setTextColor(theme.textPrimaryColor);
            binding.tvMode4Desc.setTextColor(theme.textSecondaryColor);

            binding.cardMode5.setCardBackgroundColor(theme.cardBackgroundColor);
            binding.cardMode5.setStrokeColor(theme.cardStrokeColor);
            binding.tvMode5Title.setTextColor(theme.textPrimaryColor);
            binding.tvMode5Desc.setTextColor(theme.textSecondaryColor);

            binding.cardMode6.setCardBackgroundColor(theme.cardBackgroundColor);
            binding.cardMode6.setStrokeColor(theme.cardStrokeColor);
            binding.tvMode6Title.setTextColor(theme.textPrimaryColor);
            binding.tvMode6Desc.setTextColor(theme.textSecondaryColor);

            if (theme.isDark) {
                // 4x4 badge dark
                binding.cardMode4Badge.setCardBackgroundColor(Color.parseColor("#2E2822"));
                binding.cardMode4Badge.setStrokeColor(Color.parseColor("#4D3A2A"));
                binding.tvMode4Badge.setTextColor(Color.parseColor("#FDBA74"));

                // 5x5 badge dark
                binding.cardMode5Badge.setCardBackgroundColor(Color.parseColor("#212C3B"));
                binding.cardMode5Badge.setStrokeColor(Color.parseColor("#2D4159"));
                binding.tvMode5Badge.setTextColor(Color.parseColor("#60A5FA"));

                // 6x6 badge dark
                binding.cardMode6Badge.setCardBackgroundColor(Color.parseColor("#2E223B"));
                binding.cardMode6Badge.setStrokeColor(Color.parseColor("#48315E"));
                binding.tvMode6Badge.setTextColor(Color.parseColor("#C084FC"));

                // Best score chips dark
                int chipBgDark = Color.parseColor("#2A2F3B");
                int chipTextDark = Color.parseColor("#FBBF24");
                binding.tvBest4.setBackgroundColor(chipBgDark);
                binding.tvBest4.setTextColor(chipTextDark);
                binding.tvBest5.setBackgroundColor(chipBgDark);
                binding.tvBest5.setTextColor(chipTextDark);
                binding.tvBest6.setBackgroundColor(chipBgDark);
                binding.tvBest6.setTextColor(chipTextDark);
            } else {
                // 4x4 badge light
                binding.cardMode4Badge.setCardBackgroundColor(getColor(R.color.game_mode_4_bg));
                binding.cardMode4Badge.setStrokeColor(Color.parseColor("#E3D8C8"));
                binding.tvMode4Badge.setTextColor(getColor(R.color.game_mode_4_accent));

                // 5x5 badge light
                binding.cardMode5Badge.setCardBackgroundColor(getColor(R.color.game_mode_5_bg));
                binding.cardMode5Badge.setStrokeColor(Color.parseColor("#D1DFEE"));
                binding.tvMode5Badge.setTextColor(getColor(R.color.game_mode_5_accent));

                // 6x6 badge light
                binding.cardMode6Badge.setCardBackgroundColor(getColor(R.color.game_mode_6_bg));
                binding.cardMode6Badge.setStrokeColor(Color.parseColor("#DFD3EB"));
                binding.tvMode6Badge.setTextColor(getColor(R.color.game_mode_6_accent));

                // Best score chips light
                int chipBgLight = Color.parseColor("#FAF6EB");
                int chipTextLight = Color.parseColor("#92400E");
                binding.tvBest4.setBackgroundColor(chipBgLight);
                binding.tvBest4.setTextColor(chipTextLight);
                binding.tvBest5.setBackgroundColor(chipBgLight);
                binding.tvBest5.setTextColor(chipTextLight);
                binding.tvBest6.setBackgroundColor(chipBgLight);
                binding.tvBest6.setTextColor(chipTextLight);
            }

            // Bottom Nav Buttons
            styleOutlinedNavButton(binding.btnHomeStats, theme);
            styleOutlinedNavButton(binding.btnHomeThemes, theme);
            styleOutlinedNavButton(binding.btnHomeHelp, theme);
        }
        StatusBarHelper.updateSystemBars(this, theme.backgroundColor);
    }

    private void styleOutlinedNavButton(View btn, Theme theme) {
        if (btn instanceof MaterialButton) {
            MaterialButton matBtn = (MaterialButton) btn;
            matBtn.setBackgroundColor(theme.cardBackgroundColor);
            matBtn.setTextColor(theme.textPrimaryColor);
            matBtn.setStrokeColor(ColorStateList.valueOf(theme.btnStrokeColor));
        }
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

        if (resume) {
            long score = 0;
            try {
                String activeState = prefs.getActiveGame(size);
                if (activeState != null) {
                    GameSnapshot snapshot = gson.fromJson(activeState, GameSnapshot.class);
                    if (snapshot != null) score = snapshot.score;
                }
            } catch (Exception ignored) {}
            AnalyticsManager.getInstance(this).logGameResume(size, score);
        } else {
            AnalyticsManager.getInstance(this).logGameStart(size);
        }

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
            AnalyticsManager.getInstance(this).logThemeChanged(themeKey);
        });
    }

    private void showHelpDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        hapticManager.click();
        currentDialog = DialogHelper.showFirstTimeInstruction(this, null);
    }
}
