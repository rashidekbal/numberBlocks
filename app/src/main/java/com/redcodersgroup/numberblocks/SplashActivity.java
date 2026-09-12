package com.redcodersgroup.numberblocks;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;

import com.redcodersgroup.numberblocks.analytics.AnalyticsManager;
import com.redcodersgroup.numberblocks.audio.HapticManager;
import com.redcodersgroup.numberblocks.audio.SoundManager;
import com.redcodersgroup.numberblocks.databinding.ActivitySplashBinding;
import com.redcodersgroup.numberblocks.storage.PreferencesManager;
import com.redcodersgroup.numberblocks.ui.StatusBarHelper;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private ValueAnimator progressAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsManager.getInstance(this).logScreenView("Splash");
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        StatusBarHelper.applySystemBarInsets(binding.getRoot());
        StatusBarHelper.updateSystemBars(this, 0xFFFAF8EF);

        PreferencesManager prefs = PreferencesManager.getInstance(this);
        SoundManager soundManager = SoundManager.getInstance();
        soundManager.setEnabled(prefs.isSoundEnabled());
        HapticManager hapticManager = HapticManager.getInstance();
        hapticManager.init(this);
        hapticManager.setEnabled(prefs.isHapticsEnabled());

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

    @Override
    protected void onDestroy() {
        if (progressAnim != null && progressAnim.isRunning()) {
            progressAnim.cancel();
        }
        super.onDestroy();
    }
}
