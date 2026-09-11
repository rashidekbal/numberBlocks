package com.numberblocksmerge;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;

import com.numberblocksmerge.databinding.ActivitySplashBinding;
import com.numberblocksmerge.storage.PreferencesManager;
import com.numberblocksmerge.ui.StatusBarHelper;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        StatusBarHelper.applySystemBarInsets(binding.getRoot());
        StatusBarHelper.updateSystemBars(this, 0xFFFAF9F6);

        // Initial invisible & scaled down
        binding.splashTile.setScaleX(0.3f);
        binding.splashTile.setScaleY(0.3f);
        binding.splashTile.setAlpha(0.0f);

        binding.splashTitle.setAlpha(0.0f);
        binding.splashTitle.setTranslationY(30f);

        binding.splashSubtitle.setAlpha(0.0f);
        binding.splashSubtitle.setTranslationY(20f);

        // 1. Pop tile
        binding.splashTile.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        // 2. Fade in title
        binding.splashTitle.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(180)
                .setDuration(350)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 3. Fade in subtitle
        binding.splashSubtitle.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(280)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Navigate after animation: first launch goes directly to 4x4 with tutorial
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PreferencesManager prefs = PreferencesManager.getInstance(SplashActivity.this);
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
        }, 1100);
    }
}
