package com.redcodersgroup.numberblocks;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;

import com.redcodersgroup.numberblocks.analytics.AnalyticsManager;
import com.redcodersgroup.numberblocks.audio.HapticManager;
import com.redcodersgroup.numberblocks.audio.SoundManager;
import com.redcodersgroup.numberblocks.databinding.ActivitySplashBinding;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;
import com.redcodersgroup.numberblocks.ui.StatusBarHelper;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private ValueAnimator progressAnim;
    private float touchDownX, touchDownY;
    private long touchDownTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsManager.getInstance(this).logScreenView("Splash");
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PreferencesManager prefs = PreferencesManager.getInstance(this);
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.setTheme(prefs.getTheme());
        Theme theme = themeManager.getCurrentTheme();

        StatusBarHelper.hideSystemBars(this);
        StatusBarHelper.applySystemBarInsets(binding.splashContent);
        StatusBarHelper.updateSystemBars(this, theme.backgroundColor);

        SoundManager soundManager = SoundManager.getInstance();
        soundManager.setEnabled(prefs.isSoundEnabled());
        HapticManager hapticManager = HapticManager.getInstance();
        hapticManager.init(this);
        hapticManager.setEnabled(prefs.isHapticsEnabled());

        // Apply dynamic theme palette to splash elements
        applyTheme(theme);

        // 1. Initial State Setup
        binding.splashBoardWell.setScaleX(0.35f);
        binding.splashBoardWell.setScaleY(0.35f);
        binding.splashBoardWell.setAlpha(0.0f);

        binding.splashTile2.setScaleX(0.0f);
        binding.splashTile2.setScaleY(0.0f);
        binding.splashTile2.setAlpha(0.0f);

        binding.splashTile4.setScaleX(0.0f);
        binding.splashTile4.setScaleY(0.0f);
        binding.splashTile4.setAlpha(0.0f);

        binding.splashTile8.setScaleX(0.0f);
        binding.splashTile8.setScaleY(0.0f);
        binding.splashTile8.setAlpha(0.0f);

        binding.splashTile2048.setScaleX(0.0f);
        binding.splashTile2048.setScaleY(0.0f);
        binding.splashTile2048.setAlpha(0.0f);

        binding.splashTitle.setAlpha(0.0f);
        binding.splashTitle.setTranslationY(30f);

        binding.splashTaglineBadge.setAlpha(0.0f);
        binding.splashTaglineBadge.setTranslationY(20f);

        binding.splashProgressTrack.setAlpha(0.0f);
        binding.splashFooter.setAlpha(0.0f);

        // 2. Animate Board Well Pop
        binding.splashBoardWell.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.25f))
                .start();

        // 3. Cascading Tile Spring Pops
        binding.splashTile2.animate()
                .scaleX(1.0f).scaleY(1.0f).alpha(1.0f)
                .setStartDelay(100)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        binding.splashTile4.animate()
                .scaleX(1.0f).scaleY(1.0f).alpha(1.0f)
                .setStartDelay(170)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        binding.splashTile8.animate()
                .scaleX(1.0f).scaleY(1.0f).alpha(1.0f)
                .setStartDelay(240)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        binding.splashTile2048.animate()
                .scaleX(1.0f).scaleY(1.0f).alpha(1.0f)
                .setStartDelay(310)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        // 4. Title, Tagline Badge & Footer Fade-in
        binding.splashTitle.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(380)
                .setDuration(350)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        binding.splashTaglineBadge.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(460)
                .setDuration(320)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        binding.splashProgressTrack.animate()
                .alpha(1.0f)
                .setStartDelay(500)
                .setDuration(250)
                .start();

        binding.splashFooter.animate()
                .alpha(1.0f)
                .setStartDelay(540)
                .setDuration(300)
                .start();

        // 5. Arcade Progress Bar Fill Animation
        final int targetWidth = (int) (140 * getResources().getDisplayMetrics().density);
        progressAnim = ValueAnimator.ofInt(0, targetWidth);
        progressAnim.setStartDelay(500);
        progressAnim.setDuration(700);
        progressAnim.setInterpolator(new DecelerateInterpolator());
        progressAnim.addUpdateListener(anim -> {
            if (binding != null && binding.splashProgressFill != null) {
                ViewGroup.LayoutParams lp = binding.splashProgressFill.getLayoutParams();
                lp.width = (int) anim.getAnimatedValue();
                binding.splashProgressFill.setLayoutParams(lp);
            }
        });
        progressAnim.start();

        // 6. Navigate after animation completes
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            Intent intent;
            if (prefs.isFirstLaunch()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_BOARD_SIZE, 4);
                intent.putExtra(MainActivity.EXTRA_FIRST_LAUNCH, true);
            } else {
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            }
            startActivity(intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,
                        android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            finish();
        }, 1350);
    }

    private void applyTheme(Theme theme) {
        if (theme == null || binding == null) return;

        // 1. Root & Ambient Background View
        binding.getRoot().setBackgroundColor(theme.backgroundColor);
        if (binding.splashBackgroundView != null) {
            binding.splashBackgroundView.setTheme(theme);
        }

        // 2. Showcase Board Well
        binding.splashBoardWell.setCardBackgroundColor(theme.boardColor);
        binding.splashBoardWell.setStrokeColor(theme.btnStrokeColor);

        // 3. Mini Showcase Tiles
        binding.splashTile2.setCardBackgroundColor(theme.getTileColor(2));
        binding.tvSplashTile2.setTextColor(theme.getTextColor(2));

        binding.splashTile4.setCardBackgroundColor(theme.getTileColor(4));
        binding.tvSplashTile4.setTextColor(theme.getTextColor(4));

        binding.splashTile8.setCardBackgroundColor(theme.getTileColor(8));
        binding.tvSplashTile8.setTextColor(theme.getTextColor(8));

        binding.splashTile2048.setCardBackgroundColor(theme.getTileColor(2048));
        binding.tvSplashTile2048.setTextColor(theme.getTextColor(2048));

        // 4. Game Title
        binding.splashTitle.setTextColor(theme.textPrimaryColor);

        // 5. Tagline Badge
        binding.splashTaglineBadge.setCardBackgroundColor(theme.btnSurfaceColor);
        binding.splashTaglineBadge.setStrokeColor(theme.btnStrokeColor);
        binding.splashSubtitle.setTextColor(theme.textSecondaryColor);

        // 6. Arcade Progress Bar Track & Fill
        binding.splashProgressTrack.setBackgroundTintList(ColorStateList.valueOf(theme.btnSurfaceColor));
        int fillAccent = theme.isDark ? theme.getTileColor(2048) : Color.parseColor("#F59E0B");
        binding.splashProgressFill.setBackgroundTintList(ColorStateList.valueOf(fillAccent));

        // 7. Studio Footer Branding
        binding.tvSplashPoweredBy.setTextColor(theme.textSecondaryColor);
        binding.tvSplashBrand.setTextColor(theme.textPrimaryColor);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (binding != null && binding.splashBackgroundView != null) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    touchDownX = ev.getRawX();
                    touchDownY = ev.getRawY();
                    touchDownTime = System.currentTimeMillis();
                    binding.splashBackgroundView.setCursorPosition(ev.getX(), ev.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    binding.splashBackgroundView.setCursorPosition(ev.getX(), ev.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    binding.splashBackgroundView.clearCursor();
                    float dx = Math.abs(ev.getRawX() - touchDownX);
                    float dy = Math.abs(ev.getRawY() - touchDownY);
                    long duration = System.currentTimeMillis() - touchDownTime;
                    float density = getResources().getDisplayMetrics().density;
                    if (dx < 14 * density && dy < 14 * density && duration < 350) {
                        binding.splashBackgroundView.handleTap(ev.getX(), ev.getY());
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    binding.splashBackgroundView.clearCursor();
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatusBarHelper.hideSystemBars(this);
        if (binding != null && binding.splashBackgroundView != null) {
            binding.splashBackgroundView.resumeAnimation();
        }
    }

    @Override
    protected void onPause() {
        if (binding != null && binding.splashBackgroundView != null) {
            binding.splashBackgroundView.pauseAnimation();
        }
        super.onPause();
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
        if (progressAnim != null && progressAnim.isRunning()) {
            progressAnim.cancel();
        }
        if (binding != null && binding.splashBackgroundView != null) {
            binding.splashBackgroundView.pauseAnimation();
        }
        super.onDestroy();
    }
}
