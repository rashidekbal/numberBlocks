package com.redcodersgroup.numberblocks.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import com.redcodersgroup.numberblocks.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Explosive arcade-style combo celebration overlay view.
 * Features 360-degree radial burst sprinkles (tumbling pills, star sparks, circular beads)
 * and unboxed high-impact gradient typography with outer neon glow and a bonus pill badge.
 */
public class ComboBurstView extends View {

    private enum ParticleShape {
        PILL,
        STAR,
        CIRCLE
    }

    private static class Particle {
        float x, y;
        float vx, vy;
        float gravity;
        float rotation, rotSpeed;
        int color;
        ParticleShape shape;
        float width, height;
        float alpha = 1.0f;
        float scale = 1.0f;

        void update() {
            x += vx;
            y += vy;
            vy += gravity;
            rotation += rotSpeed;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    // Typography & Drawing
    private Typeface titleTypeface;
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bonusBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bonusStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bonusTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect textBounds = new Rect();
    private final RectF bonusPillRect = new RectF();
    private final Path starPath = new Path();

    // State
    private String comboTitle = "2× COMBO!";
    private String bonusText = "+24 BONUS";
    private int themeColor = 0xFFF59E0B;
    private int themeDark = 0xFFD97706;
    private int themeGlow = 0xE6F59E0B;
    private boolean drawTypography = true;

    private float animProgress = 0f;
    private ValueAnimator burstAnimator;
    private final float density;

    public ComboBurstView(Context context) {
        this(context, null);
    }

    public ComboBurstView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComboBurstView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        density = dm.density;
        init();
    }

    private void init() {
        setClickable(false);
        setFocusable(false);

        try {
            titleTypeface = ResourcesCompat.getFont(getContext(), R.font.plus_jakarta_sans_extrabold);
        } catch (Exception e) {
            titleTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        }

        if (titleTypeface != null) {
            titleTypeface = Typeface.create(titleTypeface, Typeface.BOLD_ITALIC);
        } else {
            titleTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(titleTypeface);

        bonusTextPaint.setTextAlign(Paint.Align.CENTER);
        bonusTextPaint.setTypeface(titleTypeface);

        bonusBgPaint.setStyle(Paint.Style.FILL);
        bonusBgPaint.setColor(0xEE121418); // 88% dark background

        bonusStrokePaint.setStyle(Paint.Style.STROKE);
        bonusStrokePaint.setStrokeWidth(2f * density);

        particlePaint.setStyle(Paint.Style.FILL);

        // Pre-construct canonical 8-point star spark path (normalized to 10dp)
        buildStarPath(10f * density);
    }

    private void buildStarPath(float size) {
        starPath.reset();
        float half = size / 2f;
        float inner = size * 0.22f;
        int points = 8;
        for (int i = 0; i < points * 2; i++) {
            float r = (i % 2 == 0) ? half : inner;
            double angle = i * Math.PI / points - Math.PI / 2.0;
            float px = (float) (Math.cos(angle) * r);
            float py = (float) (Math.sin(angle) * r);
            if (i == 0) {
                starPath.moveTo(px, py);
            } else {
                starPath.lineTo(px, py);
            }
        }
        starPath.close();
    }

    public void showCombo(int combo, int bonusPts) {
        if (combo < 3) {
            setVisibility(View.GONE);
            return;
        }

        if (burstAnimator != null && burstAnimator.isRunning()) {
            burstAnimator.cancel();
        }

        drawTypography = true;

        // Configure tier metadata
        configureTier(combo);
        bonusText = bonusPts > 0 ? "+" + String.format(java.util.Locale.US, "%,d", bonusPts) + " BONUS" : "+" + (combo * 32) + " BONUS";

        // Seed 42 to 56 burst sprinkle particles
        spawnSprinkles(combo);

        setVisibility(View.VISIBLE);
        animProgress = 0f;

        burstAnimator = ValueAnimator.ofFloat(0f, 1f);
        burstAnimator.setDuration(1400);
        burstAnimator.setInterpolator(new DecelerateInterpolator(0.9f));
        burstAnimator.addUpdateListener(anim -> {
            animProgress = (float) anim.getAnimatedValue();
            for (Particle p : particles) {
                p.update();
            }
            invalidate();
        });
        burstAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
                particles.clear();
            }
        });
        burstAnimator.start();
    }

    public void showMilestoneBurst(int primaryColor) {
        if (burstAnimator != null && burstAnimator.isRunning()) {
            burstAnimator.cancel();
        }

        drawTypography = false;
        themeColor = primaryColor;

        particles.clear();
        int count = 60;
        int[] confettiColors = new int[]{
                primaryColor,
                0xFFFFFFFF,
                0xFFFFD54F, // Radiant Gold
                0xFFFDE047, // Bright Sun Gold
                0xFFF59E0B, // Warm Amber
                0xFF38BDF8, // Sky Cyan
                0xFFF43F5E, // Hot Rose
                0xFFC084FC  // Electric Lavender
        };

        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = 0f;
            p.y = 0f;

            float shapeRoll = random.nextFloat();
            if (shapeRoll < 0.35f) {
                p.shape = ParticleShape.STAR; // Stars for milestone celebrations
                p.width = (10f + random.nextInt(4)) * density;
                p.height = p.width;
            } else if (shapeRoll < 0.60f) {
                p.shape = ParticleShape.CIRCLE;
                p.width = (7f + random.nextInt(4)) * density;
                p.height = p.width;
            } else {
                p.shape = ParticleShape.PILL;
                p.width = (5f + random.nextInt(4)) * density;
                p.height = (12f + random.nextInt(8)) * density;
            }

            double baseAngle = (i / (double) count) * 2.0 * Math.PI;
            double angle = baseAngle + (random.nextDouble() - 0.5) * 0.45;
            float speed = (8f + random.nextFloat() * 13f) * density * 0.65f;

            p.vx = (float) (Math.cos(angle) * speed);
            p.vy = (float) (Math.sin(angle) * speed * 0.85f - 3.0f * density);
            p.gravity = (0.22f + random.nextFloat() * 0.12f) * density;

            p.rotation = random.nextFloat() * 360f;
            p.rotSpeed = (random.nextFloat() - 0.5f) * 24f;

            float colorRoll = random.nextFloat();
            if (colorRoll < 0.45f) {
                p.color = primaryColor;
            } else if (colorRoll < 0.70f) {
                p.color = 0xFFFFFFFF; // Brilliant white star sparks
            } else {
                p.color = confettiColors[random.nextInt(confettiColors.length)];
            }

            particles.add(p);
        }

        setVisibility(View.VISIBLE);
        animProgress = 0f;

        burstAnimator = ValueAnimator.ofFloat(0f, 1f);
        burstAnimator.setDuration(1600);
        burstAnimator.setInterpolator(new DecelerateInterpolator(0.9f));
        burstAnimator.addUpdateListener(anim -> {
            animProgress = (float) anim.getAnimatedValue();
            for (Particle p : particles) {
                p.update();
            }
            invalidate();
        });
        burstAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
                particles.clear();
            }
        });
        burstAnimator.start();
    }

    private void configureTier(int combo) {
        if (combo == 3) {
            comboTitle = "3× COMBO!";
            themeColor = 0xFFFB923C; // Fiery Orange
            themeDark = 0xFFEA580C;
            themeGlow = 0xEEFB923C;
        } else if (combo == 4) {
            comboTitle = "4× SUPER COMBO!";
            themeColor = 0xFFEF4444; // Crimson Red
            themeDark = 0xFFB91C1C;
            themeGlow = 0xF0EF4444;
        } else if (combo == 5) {
            comboTitle = "5× MEGA COMBO!";
            themeColor = 0xFFEC4899; // Vivid Magenta
            themeDark = 0xFFBE185D;
            themeGlow = 0xF0EC4899;
        } else if (combo >= 6) {
            comboTitle = combo + "× ULTRA COMBO!";
            themeColor = 0xFF8B5CF6; // Electric Violet
            themeDark = 0xFF6D28D9;
            themeGlow = 0xFF8B5CF6;
        } else {
            comboTitle = "2× COMBO!";
            themeColor = 0xFFF59E0B; // Warm Amber Gold
            themeDark = 0xFFD97706;
            themeGlow = 0xE6F59E0B;
        }
    }

    private void spawnSprinkles(int combo) {
        particles.clear();
        int count = 40 + Math.min(combo * 3, 16);

        int[] confettiColors = new int[]{
                themeColor,
                0xFFFFFFFF,
                0xFFFDE047, // Bright Gold
                0xFF38BDF8, // Sky Cyan
                0xFF4ADE80, // Neon Mint
                0xFFF43F5E, // Hot Rose
                0xFFC084FC, // Electric Lavender
                0xFFFB923C  // Orange Spark
        };

        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = 0f;
            p.y = 0f;

            // Shape distribution: 55% pills, 25% stars, 20% circles
            float shapeRoll = random.nextFloat();
            if (shapeRoll < 0.25f) {
                p.shape = ParticleShape.STAR;
                p.width = 10f * density;
                p.height = 10f * density;
            } else if (shapeRoll < 0.45f) {
                p.shape = ParticleShape.CIRCLE;
                p.width = 8f * density;
                p.height = 8f * density;
            } else {
                p.shape = ParticleShape.PILL;
                p.width = (5f + random.nextInt(4)) * density;
                p.height = (11f + random.nextInt(7)) * density;
            }

            // Radial distribution in 360 degrees with jitter
            double baseAngle = (i / (double) count) * 2.0 * Math.PI;
            double angle = baseAngle + (random.nextDouble() - 0.5) * 0.45;
            float speed = (7f + random.nextFloat() * 11f) * density * 0.65f;

            p.vx = (float) (Math.cos(angle) * speed);
            // Slight upwards bias to counter initial gravity fall
            p.vy = (float) (Math.sin(angle) * speed * 0.82f - 2.5f * density);
            p.gravity = (0.28f + random.nextFloat() * 0.15f) * density;

            p.rotation = random.nextFloat() * 360f;
            p.rotSpeed = (random.nextFloat() - 0.5f) * 22f;

            // Color: 40% theme, 20% sparkling white, 40% multicolor confetti
            float colorRoll = random.nextFloat();
            if (colorRoll < 0.4f) {
                p.color = themeColor;
            } else if (colorRoll < 0.6f) {
                p.color = 0xFFFFFFFF;
            } else {
                p.color = confettiColors[random.nextInt(confettiColors.length)];
            }

            particles.add(p);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (animProgress >= 1.0f) return;

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // 1. Draw 360-degree Radial Burst Sprinkles
        drawSprinkles(canvas, cx, cy);

        // 2. Draw Explosive Typography & Bonus Badge
        if (drawTypography) {
            drawArcadeTypography(canvas, cx, cy);
        }
    }

    private void drawSprinkles(Canvas canvas, float cx, float cy) {
        // Sprinkles fade out during the last 35% of the animation
        float sprinkleAlpha = 1.0f;
        if (animProgress > 0.65f) {
            sprinkleAlpha = Math.max(0f, 1.0f - (animProgress - 0.65f) / 0.35f);
        }

        for (Particle p : particles) {
            int alpha = (int) (sprinkleAlpha * 255);
            if (alpha <= 0) continue;

            particlePaint.setColor(p.color);
            particlePaint.setAlpha(alpha);

            canvas.save();
            canvas.translate(cx + p.x, cy + p.y);
            canvas.rotate(p.rotation);

            if (p.shape == ParticleShape.STAR) {
                canvas.drawPath(starPath, particlePaint);
            } else if (p.shape == ParticleShape.CIRCLE) {
                canvas.drawCircle(0, 0, p.width / 2f, particlePaint);
            } else {
                // Pill / capsule
                float hw = p.width / 2f;
                float hh = p.height / 2f;
                canvas.drawRoundRect(-hw, -hh, hw, hh, hw, hw, particlePaint);
            }
            canvas.restore();
        }
    }

    private void drawArcadeTypography(Canvas canvas, float cx, float cy) {
        // Spring Scale curve replicating the web mockup's keyframe:
        // 0% -> scale 0.1, rot -12deg
        // 22% -> scale 1.45, rot +4deg
        // 40% -> scale 0.92, rot -2deg
        // 60% -> scale 1.10, rot +1deg
        // 78% -> scale 1.00, rot 0deg
        // 100% -> scale 0.85, translateY -30dp, alpha 0
        float textScale;
        float textRot;
        float textAlpha = 1.0f;
        float translateY = 0f;

        if (animProgress < 0.22f) {
            float t = animProgress / 0.22f;
            textScale = 0.1f + t * (1.45f - 0.1f);
            textRot = -12f + t * 16f;
        } else if (animProgress < 0.40f) {
            float t = (animProgress - 0.22f) / 0.18f;
            textScale = 1.45f - t * (1.45f - 0.92f);
            textRot = 4f - t * 6f;
        } else if (animProgress < 0.60f) {
            float t = (animProgress - 0.40f) / 0.20f;
            textScale = 0.92f + t * (1.10f - 0.92f);
            textRot = -2f + t * 3f;
        } else if (animProgress < 0.78f) {
            float t = (animProgress - 0.60f) / 0.18f;
            textScale = 1.10f - t * 0.10f;
            textRot = 1f - t * 1f;
        } else {
            float t = (animProgress - 0.78f) / 0.22f;
            textScale = 1.0f - t * 0.15f;
            textRot = 0f;
            textAlpha = Math.max(0f, 1.0f - t);
            translateY = -t * 32f * density;
        }

        if (textAlpha <= 0f) return;

        canvas.save();
        canvas.translate(cx, cy + translateY - 12f * density);
        canvas.rotate(textRot);
        canvas.scale(textScale, textScale);

        // Setup Text Size & Paint
        float textSize = 38f * density;
        textPaint.setTextSize(textSize);

        // Calculate text bounds for gradient
        textPaint.getTextBounds(comboTitle, 0, comboTitle.length(), textBounds);
        float textH = textBounds.height();

        // Vertical Linear Gradient (Pure White top to saturated tier bottom)
        LinearGradient gradient = new LinearGradient(
                0, -textH, 0, 4f * density,
                new int[]{0xFFFFFFFF, themeColor, themeDark},
                new float[]{0.0f, 0.65f, 1.0f},
                Shader.TileMode.CLAMP
        );
        textPaint.setShader(gradient);
        textPaint.setAlpha((int) (textAlpha * 255));
        textPaint.setShadowLayer(14f * density, 0, 0, themeGlow);

        // Draw Combo Title
        canvas.drawText(comboTitle, 0, 0, textPaint);

        // Draw Bonus Pill Badge underneath
        drawBonusPill(canvas, textH, textAlpha);

        canvas.restore();
    }

    private void drawBonusPill(Canvas canvas, float titleHeight, float parentAlpha) {
        // Bonus badge pops up slightly delayed
        float pillScale = 1.0f;
        if (animProgress < 0.10f) {
            pillScale = animProgress / 0.10f * 0.5f;
        } else if (animProgress < 0.32f) {
            float t = (animProgress - 0.10f) / 0.22f;
            pillScale = 0.5f + t * (1.20f - 0.5f);
        } else if (animProgress < 0.55f) {
            float t = (animProgress - 0.32f) / 0.23f;
            pillScale = 1.20f - t * 0.20f;
        }

        canvas.save();
        float pillY = 18f * density;
        canvas.translate(0, pillY);
        canvas.scale(pillScale, pillScale);

        float bTextSize = 13f * density;
        bonusTextPaint.setTextSize(bTextSize);
        bonusTextPaint.getTextBounds(bonusText, 0, bonusText.length(), textBounds);

        float textW = bonusTextPaint.measureText(bonusText);
        float textH = textBounds.height();

        float padH = 14f * density;
        float padV = 5f * density;
        float pillW = textW + padH * 2f;
        float pillH = textH + padV * 2f;

        bonusPillRect.set(-pillW / 2f, -pillH / 2f, pillW / 2f, pillH / 2f);
        float radius = pillH / 2f;

        int alpha = (int) (parentAlpha * 255);

        // 1. Dark Pill Background
        bonusBgPaint.setAlpha((int) (parentAlpha * 230));
        bonusBgPaint.setShadowLayer(8f * density, 0, 2f * density, 0x88000000);
        canvas.drawRoundRect(bonusPillRect, radius, radius, bonusBgPaint);

        // 2. Glowing Colored Border
        bonusStrokePaint.setColor(themeColor);
        bonusStrokePaint.setAlpha(alpha);
        bonusStrokePaint.setShadowLayer(8f * density, 0, 0, themeGlow);
        canvas.drawRoundRect(bonusPillRect, radius, radius, bonusStrokePaint);

        // 3. Colored Bonus Text
        bonusTextPaint.setColor(themeColor);
        bonusTextPaint.setAlpha(alpha);
        bonusTextPaint.setShadowLayer(6f * density, 0, 0, themeGlow);
        canvas.drawText(bonusText, 0, textH / 2f - 1f * density, bonusTextPaint);

        canvas.restore();
    }
}
