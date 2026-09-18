package com.redcodersgroup.numberblocks;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
    private int currentSelectedSize = 4;
    private GestureDetector gestureDetector;
    private long lastModeSelectTime = 0;

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
        StatusBarHelper.hideSystemBars(this);
        StatusBarHelper.applySystemBarInsets(binding.getRoot());

        int lastSize = prefs.getLastPlayedSize();
        if (lastSize >= 4 && lastSize <= 6) {
            currentSelectedSize = lastSize;
        } else {
            currentSelectedSize = 4;
        }

        applyTheme();

        AnalyticsManager.getInstance(this).logScreenView("Home");

        // Mode switch tabs
        binding.tabHero4.setOnClickListener(v -> selectMode(4));
        binding.tabHero5.setOnClickListener(v -> selectMode(5));
        binding.tabHero6.setOnClickListener(v -> selectMode(6));

        // Swipe & tap gesture detector for hero board
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 80;
            private static final int SWIPE_VELOCITY_THRESHOLD = 80;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                binding.cardHeroBoard.performClick();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Swiped right -> go to smaller/previous grid
                            if (currentSelectedSize > 4) {
                                selectMode(currentSelectedSize - 1);
                                hapticManager.click();
                            }
                        } else {
                            // Swiped left -> go to larger/next grid
                            if (currentSelectedSize < 6) {
                                selectMode(currentSelectedSize + 1);
                                hapticManager.click();
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        binding.cardHeroBoard.setOnClickListener(v -> onSelectMode(currentSelectedSize));
        binding.cardHeroBoard.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        selectMode(currentSelectedSize);

        // Active Run Resume Click Listeners
        binding.cardActiveRun.setOnClickListener(v -> launchGame(currentSelectedSize, true));
        binding.btnActiveRunResume.setOnClickListener(v -> launchGame(currentSelectedSize, true));

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
        StatusBarHelper.hideSystemBars(this);
        applyTheme();
        updateActiveRunCard();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            StatusBarHelper.hideSystemBars(this);
        }
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

            // Active Run Resume Card
            if (binding.cardActiveRun != null) {
                binding.cardActiveRun.setCardBackgroundColor(theme.cardBackgroundColor);
                binding.cardActiveRun.setStrokeColor(theme.cardStrokeColor);
                binding.tvActiveRunTag.setTextColor(theme.textSecondaryColor);
                binding.tvActiveRunScore.setTextColor(theme.textPrimaryColor);

                int resumeBtnBg = theme.isDark ? theme.textPrimaryColor : Color.parseColor("#1E2024");
                int resumeBtnText = theme.isDark ? theme.backgroundColor : Color.parseColor("#FFFFFF");
                binding.btnActiveRunResume.setBackgroundTintList(ColorStateList.valueOf(resumeBtnBg));
                binding.btnActiveRunResume.setTextColor(resumeBtnText);
            }

            // Top Tabs Container
            binding.containerHeroTabs.setCardBackgroundColor(theme.hudCardColor);
            binding.containerHeroTabs.setStrokeColor(theme.btnStrokeColor);

            // Hero Card & Preview
            binding.cardHeroBoard.setCardBackgroundColor(theme.cardBackgroundColor);
            binding.cardHeroBoard.setStrokeColor(theme.cardStrokeColor);
            binding.tvHeroGridSize.setTextColor(theme.textPrimaryColor);
            binding.previewHeroBoard.setTheme(theme);

            // Tabs & Dots
            updateHeroTabsAndDots(theme);

            // Bottom Nav Buttons
            styleOutlinedNavButton(binding.btnHomeStats, theme);
            styleOutlinedNavButton(binding.btnHomeThemes, theme);
            styleOutlinedNavButton(binding.btnHomeHelp, theme);
        }
        StatusBarHelper.updateSystemBars(this, theme.backgroundColor);
    }

    private void selectMode(int size) {
        currentSelectedSize = size;
        if (binding != null) {
            binding.previewHeroBoard.setGridSize(size);
            binding.tvHeroGridSize.setText(size + " × " + size);
            updateHeroTabsAndDots(themeManager.getCurrentTheme());
            updateActiveRunCard();
        }
    }

    private void updateActiveRunCard() {
        if (binding == null) return;

        String activeState = prefs.getActiveGame(currentSelectedSize);
        if (activeState != null) {
            try {
                GameSnapshot snapshot = gson.fromJson(activeState, GameSnapshot.class);
                if (snapshot != null && !snapshot.isOver && snapshot.score > 0) {
                    binding.cardActiveRun.setVisibility(View.VISIBLE);
                    binding.tvActiveRunTag.setText(getString(R.string.active_run_format, currentSelectedSize + "×" + currentSelectedSize));
                    binding.tvActiveRunScore.setText(String.format(Locale.getDefault(), "%,d", snapshot.score));
                    return;
                }
            } catch (Exception ignored) {}
        }
        binding.cardActiveRun.setVisibility(View.GONE);
    }

    private void updateHeroTabsAndDots(Theme theme) {
        if (binding == null) return;
        float density = getResources().getDisplayMetrics().density;

        // Tabs
        setTabState(binding.tabHero4, binding.tvTabHero4, currentSelectedSize == 4, theme);
        setTabState(binding.tabHero5, binding.tvTabHero5, currentSelectedSize == 5, theme);
        setTabState(binding.tabHero6, binding.tvTabHero6, currentSelectedSize == 6, theme);

        // Dots
        setDotState(binding.dotHero4, currentSelectedSize == 4, theme, density);
        setDotState(binding.dotHero5, currentSelectedSize == 5, theme, density);
        setDotState(binding.dotHero6, currentSelectedSize == 6, theme, density);
    }

    private void setTabState(MaterialCardView card, TextView tv, boolean isSelected, Theme theme) {
        float density = getResources().getDisplayMetrics().density;
        if (isSelected) {
            card.setCardBackgroundColor(theme.cardBackgroundColor);
            card.setStrokeColor(theme.cardStrokeColor != 0 ? theme.cardStrokeColor : Color.TRANSPARENT);
            card.setStrokeWidth((int) (1 * density));
            card.setCardElevation(2.5f * density);
            tv.setTextColor(theme.textPrimaryColor);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setAlpha(1.0f);
        } else {
            card.setCardBackgroundColor(Color.TRANSPARENT);
            card.setStrokeColor(Color.TRANSPARENT);
            card.setStrokeWidth(0);
            card.setCardElevation(0);
            tv.setTextColor(theme.textSecondaryColor);
            tv.setTypeface(null, Typeface.NORMAL);
            tv.setAlpha(0.70f);
        }
    }

    private void setDotState(View dot, boolean isActive, Theme theme, float density) {
        ViewGroup.LayoutParams params = dot.getLayoutParams();
        params.width = (int) ((isActive ? 22 : 7) * density);
        params.height = (int) (7 * density);
        dot.setLayoutParams(params);

        GradientDrawable pill = new GradientDrawable();
        pill.setShape(GradientDrawable.RECTANGLE);
        pill.setCornerRadius(3.5f * density);
        if (isActive) {
            pill.setColor(theme.textPrimaryColor);
        } else {
            pill.setColor(theme.isDark ? Color.parseColor("#3F4450") : theme.btnStrokeColor);
        }
        dot.setBackground(pill);
    }

    private void styleOutlinedNavButton(View btn, Theme theme) {
        if (btn instanceof MaterialButton) {
            MaterialButton matBtn = (MaterialButton) btn;
            matBtn.setBackgroundColor(theme.cardBackgroundColor);
            matBtn.setTextColor(theme.textPrimaryColor);
            matBtn.setStrokeColor(ColorStateList.valueOf(theme.btnStrokeColor));
        }
    }

    private void onSelectMode(int size) {
        long now = System.currentTimeMillis();
        if (now - lastModeSelectTime < 500) {
            return;
        }
        lastModeSelectTime = now;

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        String activeState = prefs.getActiveGame(size);
        if (activeState != null) {
            try {
                GameSnapshot snapshot = gson.fromJson(activeState, GameSnapshot.class);
                if (snapshot != null && !snapshot.isOver && snapshot.score > 0) {
                    currentDialog = DialogHelper.showResumePrompt(this, size, snapshot.score,
                            () -> launchGame(size, true),
                            () -> {
                                prefs.clearActiveGame(size);
                                launchGame(size, false);
                            });
                    return;
                } else if (snapshot != null && (snapshot.isOver || snapshot.score == 0)) {
                    prefs.clearActiveGame(size);
                }
            } catch (Exception ignored) {
                prefs.clearActiveGame(size);
            }
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
            soundManager.playMilestone();
            hapticManager.heavyClick();
            Toast.makeText(this, getString(R.string.theme_applied, themeManager.getCurrentTheme().name), Toast.LENGTH_SHORT).show();
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
