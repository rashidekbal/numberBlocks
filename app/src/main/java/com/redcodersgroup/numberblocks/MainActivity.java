package com.redcodersgroup.numberblocks;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.redcodersgroup.numberblocks.ads.AdsManager;
import com.redcodersgroup.numberblocks.analytics.AnalyticsManager;
import com.redcodersgroup.numberblocks.audio.HapticManager;
import com.redcodersgroup.numberblocks.audio.SoundManager;
import com.redcodersgroup.numberblocks.databinding.ActivityMainBinding;
import com.redcodersgroup.numberblocks.engine.Direction;
import com.redcodersgroup.numberblocks.engine.GameEngine;
import com.redcodersgroup.numberblocks.engine.GameSnapshot;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;
import com.redcodersgroup.numberblocks.ui.BoardView;
import com.redcodersgroup.numberblocks.ui.DialogHelper;
import com.redcodersgroup.numberblocks.ui.StatusBarHelper;

public class MainActivity extends AppCompatActivity implements GameEngine.Listener {
    public static final String EXTRA_BOARD_SIZE = "EXTRA_BOARD_SIZE";
    public static final String EXTRA_RESUME = "EXTRA_RESUME";
    public static final String EXTRA_FIRST_LAUNCH = "EXTRA_FIRST_LAUNCH";

    private ActivityMainBinding binding;
    private GameEngine gameEngine;
    private BoardView boardView;
    private TextView tvScore;
    private TextView tvBest;
    private TextView tvCombo;
    private TextView tvModeBadge;
    private Button btnUndo;
    private ViewGroup bannerContainer;
    private View rootLayout;
    private MaterialCardView cardScore, cardBest;
    private View cardMilestoneCelebration;
    private TextView tvCelebrationTile;
    private TextView tvCelebrationTitle;
    private final Handler milestoneHandler = new Handler(Looper.getMainLooper());

    private ImageButton btnPause;
    private ImageButton btnThemeToggle;
    private GestureDetector screenGestureDetector;
    private int freeUndosRemaining = 5;
    private int rewardedUndos = 0;

    private PreferencesManager prefs;
    private SoundManager soundManager;
    private HapticManager hapticManager;
    private AdsManager adsManager;
    private ThemeManager themeManager;
    private final Gson gson = new Gson();

    private int boardSize = 4;
    private Dialog currentDialog;
    private int previousScore = -1;
    private int previousBestScore = -1;
    private int previousCombo = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boardSize = getIntent().getIntExtra(EXTRA_BOARD_SIZE, 4);
        boolean shouldResume = getIntent().getBooleanExtra(EXTRA_RESUME, false);
        boolean isFirstLaunch = getIntent().getBooleanExtra(EXTRA_FIRST_LAUNCH, false);

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

        // View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rootLayout = binding.getRoot();
        StatusBarHelper.hideSystemBars(this);
        StatusBarHelper.applySystemBarInsets(rootLayout);
        boardView = binding.boardView;
        tvScore = binding.tvScore;
        tvBest = binding.tvBest;
        tvCombo = binding.tvCombo;
        tvModeBadge = binding.tvModeBadge;
        btnUndo = binding.btnUndo;
        bannerContainer = binding.bannerContainer;
        cardScore = binding.cardScore;
        cardBest = binding.cardBest;
        cardMilestoneCelebration = binding.cardMilestoneCelebration;
        tvCelebrationTile = binding.tvCelebrationTile;
        tvCelebrationTitle = binding.tvCelebrationTitle;
        btnPause = binding.btnPause;
        btnThemeToggle = binding.btnThemeToggle;

        // Set Mode Badge & Target Max Number Title
        if (boardSize == 5) {
            binding.tvTitle.setText(R.string.title_8192);
            tvModeBadge.setText(R.string.mode_5x5_badge);
        } else if (boardSize == 6) {
            binding.tvTitle.setText(R.string.title_16384);
            tvModeBadge.setText(R.string.mode_6x6_badge);
        } else {
            binding.tvTitle.setText(R.string.title_2048);
            tvModeBadge.setText(R.string.mode_4x4_badge);
        }
        tvModeBadge.setTextColor(themeManager.getCurrentTheme().textSecondaryColor);

        // Game Engine initialized with dynamic board size
        gameEngine = new GameEngine(boardSize);
        gameEngine.setBestScore(prefs.getBestScore(boardSize));
        gameEngine.setListener(this);
        boardView.setGameEngine(gameEngine);

        boardView.setOnMoveListener(result -> {
            updateUI();
            saveCurrentGame();
        });

        btnUndo.setOnClickListener(v -> handleUndoClick());

        if (btnPause != null) {
            btnPause.setOnClickListener(v -> showPauseMenuDialog());
        }
        if (btnThemeToggle != null) {
            btnThemeToggle.setOnClickListener(v -> showThemeDialog());
        }

        // Screen-wide swipe gesture detector (active everywhere outside option buttons)
        screenGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 45;
            private static final int SWIPE_VELOCITY_THRESHOLD = 90;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null || boardView == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        boardView.handleMove(diffX > 0 ? Direction.RIGHT : Direction.LEFT);
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        boardView.handleMove(diffY > 0 ? Direction.DOWN : Direction.UP);
                        return true;
                    }
                }
                return false;
            }
        });

        // Back button opens pause menu
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentDialog != null && currentDialog.isShowing()) {
                    currentDialog.dismiss();
                    currentDialog = null;
                } else {
                    showPauseMenuDialog();
                }
            }
        });

        applyTheme();

        // First launch instruction check
        if (isFirstLaunch || prefs.isFirstLaunch()) {
            prefs.setFirstLaunch(false);
            showFirstTimeTutorial();
        }

        // Check if resuming active game
        if (shouldResume) {
            String savedState = prefs.getActiveGame(boardSize);
            if (savedState != null) {
                try {
                    GameSnapshot snapshot = gson.fromJson(savedState, GameSnapshot.class);
                    if (snapshot != null && !snapshot.isOver && snapshot.score > 0) {
                        gameEngine.restoreFromSnapshot(snapshot);
                        this.freeUndosRemaining = snapshot.freeUndos;
                        this.rewardedUndos = snapshot.rewardedUndos;
                    } else {
                        prefs.clearActiveGame(boardSize);
                        startFreshGame();
                    }
                } catch (Exception e) {
                    prefs.clearActiveGame(boardSize);
                    startFreshGame();
                }
            } else {
                startFreshGame();
            }
        } else {
            startFreshGame();
        }

        updateUI();

        // Load AdMob Banner
        adsManager.loadBanner(this, bannerContainer);
    }

    private void handleBackToHome() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        currentDialog = DialogHelper.showConfirmExit(this, this::navigateToHome);
    }

    private void navigateToHome() {
        saveCurrentGame();
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void startFreshGame() {
        prefs.incrementGamesPlayed();
        freeUndosRemaining = 5;
        rewardedUndos = 0;
        gameEngine.startNewGame();
        saveCurrentGame();
    }

    private void saveCurrentGame() {
        if (gameEngine != null && !gameEngine.isOver() && gameEngine.getScore() > 0) {
            GameSnapshot snapshot = gameEngine.createSnapshot();
            snapshot.freeUndos = this.freeUndosRemaining;
            snapshot.rewardedUndos = this.rewardedUndos;
            prefs.saveActiveGame(boardSize, gson.toJson(snapshot));
        } else {
            prefs.clearActiveGame(boardSize);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatusBarHelper.hideSystemBars(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            StatusBarHelper.hideSystemBars(this);
        }
    }

    private void updateUI() {
        int currentScore = gameEngine.getScore();
        int currentBest = gameEngine.getBestScore();
        int currentCombo = gameEngine.getCombo();

        tvScore.setText(String.format(Locale.getDefault(), "%,d", currentScore));
        tvBest.setText(String.format(Locale.getDefault(), "%,d", currentBest));

        // Tactile micro-animation: Score punch on points earned
        if (previousScore != -1 && currentScore > previousScore && cardScore != null) {
            cardScore.animate().cancel();
            cardScore.setScaleX(1.06f);
            cardScore.setScaleY(1.06f);
            cardScore.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(160)
                    .setInterpolator(new OvershootInterpolator(1.8f))
                    .start();
        }

        // Tactile micro-animation: Best score record bump
        if (previousBestScore != -1 && currentBest > previousBestScore && cardBest != null) {
            cardBest.animate().cancel();
            cardBest.setScaleX(1.06f);
            cardBest.setScaleY(1.06f);
            cardBest.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(160)
                    .setInterpolator(new OvershootInterpolator(1.8f))
                    .start();
        }

        int totalUndos = freeUndosRemaining + rewardedUndos;
        boolean canUndo = gameEngine.canUndo();
        if (totalUndos > 0) {
            btnUndo.setText(getString(R.string.undo_format, totalUndos));
        } else {
            btnUndo.setText(R.string.undo_ad);
        }
        btnUndo.setEnabled(canUndo || totalUndos == 0);
        btnUndo.setAlpha(canUndo || totalUndos == 0 ? 1.0f : 0.5f);

        if (currentCombo > 1) {
            tvCombo.setText(getString(R.string.combo_format, currentCombo));
            tvCombo.setTextColor(themeManager.getCurrentTheme().textPrimaryColor);
            if (currentCombo != previousCombo) {
                tvCombo.animate().cancel();
                tvCombo.setScaleX(1.22f);
                tvCombo.setScaleY(1.22f);
                tvCombo.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(180)
                        .setInterpolator(new OvershootInterpolator(2.2f))
                        .start();
            }
        } else {
            String targetFormatted = String.format(Locale.getDefault(), "%,d", gameEngine.getTargetTile());
            tvCombo.setText(getString(R.string.default_combo_message, targetFormatted));
            tvCombo.setTextColor(themeManager.getCurrentTheme().textSecondaryColor);
        }

        previousScore = currentScore;
        previousBestScore = currentBest;
        previousCombo = currentCombo;

        prefs.setBestScore(boardSize, gameEngine.getBestScore());
        prefs.recordHighestTile(gameEngine.getHighestTile());
    }

    private void handleUndoClick() {
        if (!gameEngine.canUndo()) {
            Toast.makeText(this, R.string.no_moves_undo, Toast.LENGTH_SHORT).show();
            return;
        }

        int totalUndos = freeUndosRemaining + rewardedUndos;
        if (totalUndos > 0) {
            if (freeUndosRemaining > 0) {
                freeUndosRemaining--;
            } else {
                rewardedUndos--;
            }
            performUndo();
        } else {
            Toast.makeText(this, R.string.watch_ad_undo, Toast.LENGTH_SHORT).show();
            adsManager.showRewarded(this, reward -> {
                performUndo();
            });
        }
    }

    private void performUndo() {
        hapticManager.click();
        soundManager.playMove();

        // Game-like tactile spring press feedback
        if (btnUndo != null) {
            btnUndo.animate().cancel();
            btnUndo.setScaleX(0.88f);
            btnUndo.setScaleY(0.88f);
            btnUndo.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(180)
                    .setInterpolator(new OvershootInterpolator(2.2f))
                    .start();
        }

        gameEngine.undo();
        boardView.invalidate();
        updateUI();
        saveCurrentGame();
    }

    private void applyTheme() {
        Theme theme = themeManager.getCurrentTheme();
        rootLayout.setBackgroundColor(theme.backgroundColor);

        if (binding != null) {
            binding.tvTitle.setTextColor(theme.textPrimaryColor);
            tvScore.setTextColor(theme.textPrimaryColor);
            tvBest.setTextColor(theme.textPrimaryColor);
            binding.tvScoreLabel.setTextColor(theme.textSecondaryColor);
            binding.tvBestLabel.setTextColor(theme.textSecondaryColor);
            tvModeBadge.setTextColor(theme.textSecondaryColor);

            cardScore.setCardBackgroundColor(theme.hudCardColor);
            cardScore.setStrokeColor(theme.cardStrokeColor);
            cardBest.setCardBackgroundColor(theme.hudCardColor);
            cardBest.setStrokeColor(theme.cardStrokeColor);

            // Circular header buttons (Pause & Theme toggle)
            android.graphics.drawable.GradientDrawable circleBg = new android.graphics.drawable.GradientDrawable();
            circleBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circleBg.setColor(theme.btnSurfaceColor);
            circleBg.setStroke((int) (1 * getResources().getDisplayMetrics().density), theme.btnStrokeColor);
            btnPause.setBackground(circleBg);
            if (circleBg.getConstantState() != null) {
                btnThemeToggle.setBackground(circleBg.getConstantState().newDrawable().mutate());
            } else {
                btnThemeToggle.setBackground(circleBg);
            }
            btnPause.setImageTintList(ColorStateList.valueOf(theme.textPrimaryColor));
            btnThemeToggle.setImageTintList(ColorStateList.valueOf(theme.textPrimaryColor));

            // Undo action button (Game-like capsule styling)
            btnUndo.setTextColor(theme.textPrimaryColor);
            if (btnUndo instanceof com.google.android.material.button.MaterialButton) {
                com.google.android.material.button.MaterialButton matBtn = (com.google.android.material.button.MaterialButton) btnUndo;
                matBtn.setStrokeColor(ColorStateList.valueOf(theme.btnStrokeColor));
                matBtn.setBackgroundColor(theme.btnSurfaceColor);
                matBtn.setIconTint(ColorStateList.valueOf(theme.textPrimaryColor));
            }
        }

        boardView.invalidate();
        StatusBarHelper.updateSystemBars(this, theme.backgroundColor);
    }

    private void confirmRestart() {
        hapticManager.click();
        currentDialog = DialogHelper.showRestartConfirm(this, () -> {
            prefs.clearActiveGame(boardSize);
            startFreshGame();
            boardView.invalidate();
            updateUI();
        });
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
                        Intent intent = new Intent(MainActivity.this, InfoActivity.class);
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

    private void showThemeDialog() {
        currentDialog = DialogHelper.showThemePicker(this, prefs.getTheme(), themeKey -> {
            themeManager.setTheme(themeKey);
            prefs.setTheme(themeKey);
            applyTheme();
            soundManager.playMilestone();
            hapticManager.heavyClick();
            Toast.makeText(this, getString(R.string.theme_applied, themeManager.getCurrentTheme().name), Toast.LENGTH_SHORT).show();
            AnalyticsManager.getInstance(this).logThemeChanged(themeKey);
        });
    }

    private void showStatsDialog() {
        hapticManager.click();
        currentDialog = DialogHelper.showCareerStats(this, prefs.getGamesPlayed(), prefs.getHighestTile(),
                prefs.getBestScore(4), prefs.getBestScore(5), prefs.getBestScore(6));
    }

    private void showHelpDialog() {
        hapticManager.click();
        showFirstTimeTutorial();
    }

    private void showPauseMenuDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        hapticManager.click();
        currentDialog = DialogHelper.showPauseMenu(this, soundManager.isEnabled(), hapticManager.isEnabled(),
                new DialogHelper.PauseMenuListener() {
                    @Override
                    public void onSoundToggled(boolean enabled) {
                        soundManager.setEnabled(enabled);
                        prefs.setSoundEnabled(enabled);
                    }

                    @Override
                    public void onVibrationToggled(boolean enabled) {
                        hapticManager.setEnabled(enabled);
                        prefs.setHapticsEnabled(enabled);
                    }

                    @Override
                    public void onHowToPlayClicked() {
                        showFirstTimeTutorial();
                    }

                    @Override
                    public void onRestartClicked() {
                        confirmRestart();
                    }

                    @Override
                    public void onHomeClicked() {
                        navigateToHome();
                    }
                });
    }

    private void showFirstTimeTutorial() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        currentDialog = DialogHelper.showFirstTimeInstruction(this, () -> {
            // User confirmed "Let's play!"
        });
    }

    @Override
    public void onScoreChanged(int score, int bestScore, int combo) {
        updateUI();
    }

    @Override
    public void onGameOver(int finalScore, int bestScore, int highestTile) {
        soundManager.playGameOver();
        hapticManager.heavyClick();
        adsManager.showInterstitial(this);
        prefs.clearActiveGame(boardSize);

        currentDialog = DialogHelper.showGameOver(this, boardSize, finalScore, bestScore, highestTile,
                () -> {
                    // Retry / Play Again
                    startFreshGame();
                    boardView.invalidate();
                    updateUI();
                },
                () -> {
                    // Revive (Watch Rewarded Ad)
                    adsManager.showRewarded(this, reward -> {
                        boolean revived = gameEngine.revive();
                        if (revived) {
                            boardView.invalidate();
                            updateUI();
                            saveCurrentGame();
                            soundManager.playMilestone();
                            hapticManager.heavyClick();
                            android.widget.Toast.makeText(this, R.string.revive_success, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                },
                () -> {
                    // Return to Home
                    navigateToHome();
                });
    }

    @Override
    public void onMilestoneReached(int milestone) {
        soundManager.playMilestone();
        hapticManager.heavyClick();
        showMilestoneCelebration(milestone);
    }

    private void showMilestoneCelebration(int milestone) {
        if (cardMilestoneCelebration == null) return;

        milestoneHandler.removeCallbacksAndMessages(null);

        tvCelebrationTile.setText(String.valueOf(milestone));
        int len = String.valueOf(milestone).length();
        if (len >= 6) {
            tvCelebrationTile.setTextSize(9);
        } else if (len >= 5) {
            tvCelebrationTile.setTextSize(10);
        } else if (len >= 4) {
            tvCelebrationTile.setTextSize(11);
        } else {
            tvCelebrationTile.setTextSize(13);
        }
        tvCelebrationTile.setBackgroundTintList(ColorStateList.valueOf(getMilestoneColor(milestone)));

        tvCelebrationTitle.setText(milestone >= gameEngine.getTargetTile()
                ? getString(R.string.tile_reached_format, milestone)
                : getString(R.string.tile_unlocked_format, milestone));

        cardMilestoneCelebration.setVisibility(View.VISIBLE);
        cardMilestoneCelebration.setScaleX(0.4f);
        cardMilestoneCelebration.setScaleY(0.4f);
        cardMilestoneCelebration.setAlpha(0.0f);
        cardMilestoneCelebration.setTranslationY(30f);

        cardMilestoneCelebration.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(350)
                .setInterpolator(new OvershootInterpolator(1.3f))
                .start();

        milestoneHandler.postDelayed(() -> {
            cardMilestoneCelebration.animate()
                    .alpha(0.0f)
                    .translationY(-35f)
                    .setDuration(300)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> cardMilestoneCelebration.setVisibility(View.GONE))
                    .start();
        }, 2200);

        cardMilestoneCelebration.setOnClickListener(v -> {
            milestoneHandler.removeCallbacksAndMessages(null);
            cardMilestoneCelebration.animate()
                    .alpha(0.0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .withEndAction(() -> cardMilestoneCelebration.setVisibility(View.GONE))
                    .start();
        });
    }

    private int getMilestoneColor(int val) {
        switch (val) {
            case 128: return 0xFFEDCF72;
            case 256: return 0xFFEDCC61;
            case 512: return 0xFFEDC850;
            case 1024: return 0xFFEDC53F;
            case 2048: return 0xFFEDC22E;
            case 4096: return 0xFFE67E22;
            case 8192: return 0xFFE74C3C;
            case 16384: return 0xFF9B59B6;
            case 32768: return 0xFF2980B9;
            case 65536: return 0xFF1ABC9C;
            case 131072: return 0xFF16A085;
            default: return 0xFF1E2024;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (currentDialog != null && currentDialog.isShowing()) {
            return super.dispatchTouchEvent(ev);
        }

        float rawX = ev.getRawX();
        float rawY = ev.getRawY();

        boolean overButtons = isTouchOverView(btnPause, rawX, rawY)
                || isTouchOverView(btnThemeToggle, rawX, rawY)
                || isTouchOverView(btnUndo, rawX, rawY)
                || isTouchOverView(bannerContainer, rawX, rawY);

        if (!overButtons && screenGestureDetector != null) {
            screenGestureDetector.onTouchEvent(ev);
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean isTouchOverView(View view, float rawX, float rawY) {
        if (view == null || view.getVisibility() != View.VISIBLE) return false;
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int w = view.getWidth();
        int h = view.getHeight();
        return rawX >= x && rawX <= (x + w) && rawY >= y && rawY <= (y + h);
    }
}
