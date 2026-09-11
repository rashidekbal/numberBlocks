package com.numberblocksmerge;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;

import com.numberblocksmerge.storage.PreferencesManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final View tile = findViewById(R.id.splash_tile);
        final View title = findViewById(R.id.splash_title);
        final View subtitle = findViewById(R.id.splash_subtitle);

        // Initial invisible & scaled down
        tile.setScaleX(0.3f);
        tile.setScaleY(0.3f);
        tile.setAlpha(0.0f);

        title.setAlpha(0.0f);
        title.setTranslationY(30f);

        subtitle.setAlpha(0.0f);
        subtitle.setTranslationY(20f);

        // 1. Pop tile
        tile.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        // 2. Fade in title
        title.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(180)
                .setDuration(350)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 3. Fade in subtitle
        subtitle.animate()
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            finish();
        }, 1100);
    }
}
