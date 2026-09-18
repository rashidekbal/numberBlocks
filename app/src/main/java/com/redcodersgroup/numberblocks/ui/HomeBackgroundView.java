package com.redcodersgroup.numberblocks.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;

import androidx.annotation.Nullable;

import com.redcodersgroup.numberblocks.audio.HapticManager;
import com.redcodersgroup.numberblocks.audio.SoundManager;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Interactive Ambient Game Background for the Home Screen.
 * Features 3D floating tactile number blocks, twinkling arcade stars,
 * tumbling game confetti sprinkles, interactive cursor repulsion & tap-to-pop bursts.
 */
public class HomeBackgroundView extends View {

    private final Random random = new Random();
    private final float density;

    private boolean isRunning = false;
    private long lastFrameTimeNanos = 0;
    private float time = 0;

    private Theme currentTheme;

    // Touch / Cursor tracking
    private float cursorX = -9999f;
    private float cursorY = -9999f;
    private float targetCursorX = -9999f;
    private float targetCursorY = -9999f;
    private boolean isCursorActive = false;

    // Entities
    private final List<FloatingBlock> blocks = new ArrayList<>();
    private final List<ArcadeStar> stars = new ArrayList<>();
    private final List<ConfettiSprinkle> sprinkles = new ArrayList<>();
    private final List<BlueprintWatermark> watermarks = new ArrayList<>();
    private final List<SparkBurstParticle> bursts = new ArrayList<>();
    private final List<ShockwaveRing> shockwaves = new ArrayList<>();

    // Reusable drawing objects to avoid allocations in onDraw
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint auraPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint watermarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sprinklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shockwavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF tempRectF = new RectF();
    private final Path tempPath = new Path();
    private final Rect textBounds = new Rect();

    private final DashPathEffect orbitDash1;
    private final DashPathEffect orbitDash2;

    private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (!isRunning) return;

            if (lastFrameTimeNanos > 0) {
                float dt = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000.0f;
                if (dt > 0.05f) dt = 0.05f; // Cap delta time
                time += dt;
                update(dt);
                invalidate();
            }
            lastFrameTimeNanos = frameTimeNanos;
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    public HomeBackgroundView(Context context) {
        this(context, null);
    }

    public HomeBackgroundView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;

        orbitDash1 = new DashPathEffect(new float[]{4 * density, 10 * density}, 0);
        orbitDash2 = new DashPathEffect(new float[]{2 * density, 14 * density}, 0);

        initPaints();
        setupEntities();
        currentTheme = ThemeManager.getInstance().getCurrentTheme();
    }

    private void initPaints() {
        bgPaint.setStyle(Paint.Style.FILL);
        auraPaint.setStyle(Paint.Style.FILL);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(1.2f * density);

        watermarkPaint.setStyle(Paint.Style.FILL);
        watermarkPaint.setTextAlign(Paint.Align.CENTER);
        watermarkPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        starPaint.setStyle(Paint.Style.FILL);
        starCorePaint.setStyle(Paint.Style.FILL);
        starCorePaint.setColor(Color.WHITE);

        sprinklePaint.setStyle(Paint.Style.FILL);

        shadowPaint.setStyle(Paint.Style.FILL);
        tilePaint.setStyle(Paint.Style.FILL);
        bevelPaint.setStyle(Paint.Style.FILL);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(1.0f * density);

        sheenPaint.setStyle(Paint.Style.FILL);
        sheenPaint.setColor(Color.argb(40, 255, 255, 255));

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        badgePaint.setStyle(Paint.Style.FILL);
        badgePaint.setColor(Color.WHITE);

        particlePaint.setStyle(Paint.Style.FILL);

        shockwavePaint.setStyle(Paint.Style.STROKE);
        shockwavePaint.setStrokeWidth(2.0f * density);
    }

    private void setupEntities() {
        blocks.clear();
        // Configuration mirroring flagship web ambient layout:
        // Top area (around title & header)
        blocks.add(new FloatingBlock(2, 0.12f, 0.08f, 36, 1, -0.12f, 0));
        blocks.add(new FloatingBlock(8, 0.88f, 0.09f, 42, 2, 0.15f, 1));
        blocks.add(new FloatingBlock(4, 0.28f, 0.14f, 28, 0, 0.08f, 2));

        // Left & Right sides of Active Run / Segmented Tabs
        blocks.add(new FloatingBlock(16, 0.06f, 0.25f, 44, 2, 0.20f, 3));
        blocks.add(new FloatingBlock(64, 0.94f, 0.26f, 40, 1, -0.16f, 4));
        blocks.add(new FloatingBlock(32, 0.14f, 0.38f, 32, 0, -0.09f, 5));
        blocks.add(new FloatingBlock(128, 0.86f, 0.40f, 46, 2, 0.14f, 6));

        // Beside the Hero Board Showcase
        blocks.add(new FloatingBlock(256, 0.08f, 0.56f, 48, 1, -0.22f, 7));
        blocks.add(new FloatingBlock(2048, 0.92f, 0.58f, 54, 2, 0.18f, 8));
        blocks.add(new FloatingBlock(512, 0.16f, 0.72f, 38, 0, 0.10f, 9));
        blocks.add(new FloatingBlock(1024, 0.85f, 0.74f, 44, 1, -0.12f, 10));

        // Bottom Area (above navigation)
        blocks.add(new FloatingBlock(4, 0.22f, 0.88f, 34, 0, -0.15f, 11));
        blocks.add(new FloatingBlock(16, 0.78f, 0.89f, 40, 1, 0.16f, 12));
        blocks.add(new FloatingBlock(2, 0.50f, 0.94f, 30, 0, 0.05f, 13));

        // Sort blocks by depth for rendering order
        Collections.sort(blocks, Comparator.comparingInt(b -> b.depth));

        // Twinkling Geometric Arcade Stars
        stars.clear();
        int starCount = 22;
        for (int i = 0; i < starCount; i++) {
            stars.add(new ArcadeStar(
                    0.04f + random.nextFloat() * 0.92f,
                    0.03f + random.nextFloat() * 0.94f,
                    6f + random.nextFloat() * 8f,
                    random.nextFloat() > 0.4f ? 4 : 8,
                    1.5f + random.nextFloat() * 2.5f,
                    random.nextFloat() * (float) (Math.PI * 2),
                    (random.nextFloat() - 0.5f) * 0.8f,
                    random.nextFloat() * (float) Math.PI,
                    0.25f + random.nextFloat() * 0.45f,
                    0.04f + random.nextFloat() * 0.08f,
                    random.nextFloat()
            ));
        }

        // Floating Confetti Sprinkles (Pills & Beads)
        sprinkles.clear();
        int sprinkleCount = 18;
        for (int i = 0; i < sprinkleCount; i++) {
            boolean isBead = (i % 3 == 0);
            sprinkles.add(new ConfettiSprinkle(
                    0.03f + random.nextFloat() * 0.94f,
                    0.05f + random.nextFloat() * 0.90f,
                    !isBead,
                    isBead ? 6f : 5f,
                    isBead ? 6f : 13f,
                    random.nextFloat() * (float) (Math.PI * 2),
                    random.nextFloat() * (float) (Math.PI * 2),
                    0.6f + random.nextFloat() * 1.2f,
                    (random.nextFloat() - 0.5f) * 0.8f,
                    6f + random.nextFloat() * 10f,
                    i % 6,
                    0.35f + random.nextFloat() * 0.4f
            ));
        }

        // Faint Math Blueprint Watermarks
        watermarks.clear();
        String[] glyphs = new String[]{"2⁰", "2²", "2⁴", "2⁸", "2¹¹", "×", "+", "∞"};
        for (int i = 0; i < glyphs.length; i++) {
            watermarks.add(new BlueprintWatermark(
                    glyphs[i],
                    0.08f + (i * 0.12f) % 0.85f,
                    0.12f + (i * 0.11f) % 0.82f,
                    i == 4 ? 22f : 14f + (i % 3) * 4f,
                    i * 0.8f,
                    0.3f + (i % 3) * 0.1f
            ));
        }
    }

    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        invalidate();
    }

    public void resumeAnimation() {
        if (!isRunning) {
            isRunning = true;
            lastFrameTimeNanos = 0;
            Choreographer.getInstance().postFrameCallback(frameCallback);
        }
    }

    public void pauseAnimation() {
        if (isRunning) {
            isRunning = false;
            Choreographer.getInstance().removeFrameCallback(frameCallback);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        resumeAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        pauseAnimation();
        super.onDetachedFromWindow();
    }

    public void setCursorPosition(float x, float y) {
        this.targetCursorX = x;
        this.targetCursorY = y;
        this.isCursorActive = true;
    }

    public void clearCursor() {
        this.isCursorActive = false;
        this.targetCursorX = -9999f;
        this.targetCursorY = -9999f;
    }

    /**
     * Hit test on floating blocks. If a block is tapped, it bounces and spawns sparks.
     * Returns true if a block was hit, false if ambient space was tapped.
     */
    public boolean handleTap(float tapX, float tapY) {
        FloatingBlock hitBlock = null;

        // Hit test from top to bottom
        for (int i = blocks.size() - 1; i >= 0; i--) {
            FloatingBlock b = blocks.get(i);
            float half = (b.sizePx * b.popScale) / 2.0f + 14 * density;
            if (tapX >= b.x - half && tapX <= b.x + half &&
                    tapY >= b.y - half && tapY <= b.y + half) {
                hitBlock = b;
                break;
            }
        }

        if (hitBlock != null) {
            // Pop the block!
            hitBlock.popScale = 1.45f;
            hitBlock.popVelocity = 0.35f;
            hitBlock.wobble = (random.nextBoolean() ? 1f : -1f) * 0.30f;

            Theme theme = getEffectiveTheme();
            int tileColor = theme.getTileColor(hitBlock.val);
            spawnSparkBurst(hitBlock.x, hitBlock.y, tileColor, 12);

            SoundManager.getInstance().playMerge(hitBlock.val);
            SoundManager.getInstance().playMove();
            HapticManager.getInstance().click();
            return true;
        } else {
            // Ambient tap shockwave & twinkles
            Theme theme = getEffectiveTheme();
            int ringColor = theme.isDark ? Color.WHITE : theme.textPrimaryColor;
            shockwaves.add(new ShockwaveRing(tapX, tapY, 10 * density, 75 * density, 0.5f, ringColor));

            spawnSparkBurst(tapX, tapY, theme.isDark ? Color.parseColor("#F59E0B") : Color.parseColor("#EDC22E"), 6);
            HapticManager.getInstance().click();
            return false;
        }
    }

    private void spawnSparkBurst(float x, float y, int baseColor, int count) {
        Theme theme = getEffectiveTheme();
        int[] colors = new int[]{
                baseColor,
                Color.WHITE,
                theme.isDark ? Color.parseColor("#F59E0B") : Color.parseColor("#EDC22E"),
                Color.parseColor("#38BDF8"),
                Color.parseColor("#EC4899"),
                Color.parseColor("#10B981")
        };

        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.PI * 2 * i / count + (random.nextFloat() - 0.5f) * 0.5f);
            float speed = (2.0f + random.nextFloat() * 4.5f) * density;
            bursts.add(new SparkBurstParticle(
                    x, y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed - 1.2f * density,
                    colors[i % colors.length],
                    (3f + random.nextFloat() * 5f) * density,
                    i % 3 == 0 ? 0 : (i % 2 == 0 ? 1 : 2),
                    random.nextFloat() * (float) Math.PI,
                    (random.nextFloat() - 0.5f) * 8.0f,
                    28 + random.nextInt(16)
            ));
        }
    }

    private void update(float dt) {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;

        // Smooth cursor interpolation
        if (isCursorActive) {
            cursorX += (targetCursorX - cursorX) * 0.18f;
            cursorY += (targetCursorY - cursorY) * 0.18f;
        } else {
            cursorX = -9999f;
            cursorY = -9999f;
        }

        float repulsionRadius = 110f * density;
        float repulsionForce = 180f * density;

        // Update Floating Blocks
        for (int i = 0; i < blocks.size(); i++) {
            FloatingBlock b = blocks.get(i);
            b.sizePx = b.sizeDp * density;

            float targetBaseX = b.relX * width;
            float targetBaseY = b.relY * height;

            float oscX = (float) Math.sin(time * b.speedX + b.phaseX) * b.ampX * density;
            float oscY = (float) Math.cos(time * b.speedY + b.phaseY) * b.ampY * density;

            // Cursor magnetic repulsion
            if (isCursorActive) {
                float currentApproxX = targetBaseX + oscX + b.pushX;
                float currentApproxY = targetBaseY + oscY + b.pushY;
                float dx = currentApproxX - cursorX;
                float dy = currentApproxY - cursorY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < repulsionRadius && dist > 1f) {
                    float factor = (1f - dist / repulsionRadius);
                    b.pushX += (dx / dist) * repulsionForce * factor * dt;
                    b.pushY += (dy / dist) * repulsionForce * factor * dt;
                }
            }

            // Spring return for push offset
            b.pushX *= 0.91f;
            b.pushY *= 0.91f;

            b.x = targetBaseX + oscX + b.pushX;
            b.y = targetBaseY + oscY + b.pushY;

            // Pop spring physics
            if (b.popScale > 1.0f || Math.abs(b.popVelocity) > 0.01f) {
                float springForce = (1.0f - b.popScale) * 18.0f;
                b.popVelocity += springForce * dt;
                b.popVelocity *= 0.86f;
                b.popScale += b.popVelocity;

                if (Math.abs(b.popScale - 1.0f) < 0.01f && Math.abs(b.popVelocity) < 0.01f) {
                    b.popScale = 1.0f;
                    b.popVelocity = 0;
                }
            }

            // Wobble damping
            b.wobble *= 0.92f;
        }

        // Update Floating Sprinkles
        for (int i = 0; i < sprinkles.size(); i++) {
            ConfettiSprinkle s = sprinkles.get(i);
            s.angleX += s.rotSpeedX * dt;
            s.angleZ += s.rotSpeedZ * dt;

            // Slow upward drift
            s.relY -= (s.speedYDp / (height / density)) * dt;
            if (s.relY < -0.05f) {
                s.relY = 1.05f;
                s.relX = random.nextFloat();
            }
        }

        // Update Twinkling Stars
        for (int i = 0; i < stars.size(); i++) {
            ArcadeStar st = stars.get(i);
            st.rot += st.rotSpeed * dt;
            st.relY -= (st.driftY / 100f) * dt;
            if (st.relY < -0.05f) {
                st.relY = 1.05f;
                st.relX = random.nextFloat();
            }
        }

        // Update Spark Bursts
        for (int i = bursts.size() - 1; i >= 0; i--) {
            SparkBurstParticle p = bursts.get(i);
            p.life++;
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.12f * density; // gentle gravity
            p.vx *= 0.96f;
            p.vy *= 0.96f;
            p.rot += p.rotSpeed * dt;
            p.alpha = Math.max(0f, 1.0f - (float) p.life / p.maxLife);

            if (p.life >= p.maxLife) {
                bursts.remove(i);
            }
        }

        // Update Shockwaves
        for (int i = shockwaves.size() - 1; i >= 0; i--) {
            ShockwaveRing sw = shockwaves.get(i);
            sw.radius += (sw.maxRadius - sw.radius) * 0.14f;
            sw.alpha *= 0.91f;
            if (sw.alpha < 0.02f || sw.radius >= sw.maxRadius - 2f * density) {
                shockwaves.remove(i);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;

        Theme theme = getEffectiveTheme();
        boolean isDark = theme.isDark;

        // 1. Background color fill
        bgPaint.setColor(theme.backgroundColor);
        canvas.drawRect(0, 0, width, height, bgPaint);

        // 2. Ambient Energy Atmosphere & Radial Aura
        renderAmbientAtmosphere(canvas, width, height, theme, isDark);

        // 3. Faint Math Blueprint Watermarks
        renderWatermarks(canvas, width, height, theme, isDark);

        // 4. Twinkling Arcade Stars
        renderStars(canvas, width, height, theme, isDark);

        // 5. Floating Confetti Sprinkles
        renderSprinkles(canvas, width, height, theme, isDark);

        // 6. Floating Number Blocks (depth-sorted)
        for (int i = 0; i < blocks.size(); i++) {
            renderFloatingBlock(canvas, blocks.get(i), theme, isDark);
        }

        // 7. Click Shockwave Rings
        for (int i = 0; i < shockwaves.size(); i++) {
            ShockwaveRing sw = shockwaves.get(i);
            shockwavePaint.setColor(sw.color);
            shockwavePaint.setAlpha((int) (sw.alpha * 255));
            canvas.drawCircle(sw.x, sw.y, sw.radius, shockwavePaint);
        }

        // 8. Click Particle Spark Bursts
        for (int i = 0; i < bursts.size(); i++) {
            renderBurstParticle(canvas, bursts.get(i));
        }
    }

    private void renderAmbientAtmosphere(Canvas canvas, float width, float height, Theme theme, boolean isDark) {
        float haloX = width * 0.5f;
        float haloY = height * 0.52f;
        float haloRadius = Math.min(width, 420 * density) * 0.85f;

        float pulse = 0.5f + 0.5f * (float) Math.sin(time * 1.2f);
        float haloAlpha = isDark ? (0.12f + pulse * 0.08f) : (0.09f + pulse * 0.06f);

        int auraColor;
        if ("titanium".equalsIgnoreCase(theme.id)) auraColor = Color.parseColor("#38BDF8");
        else if ("nordic".equalsIgnoreCase(theme.id)) auraColor = Color.parseColor("#E5DDD0");
        else if ("graphite".equalsIgnoreCase(theme.id)) auraColor = Color.parseColor("#A855F7");
        else if ("prism".equalsIgnoreCase(theme.id)) auraColor = Color.parseColor("#FF007F");
        else auraColor = Color.parseColor("#EDC22E");

        int centerAura = Color.argb((int) (haloAlpha * 255), Color.red(auraColor), Color.green(auraColor), Color.blue(auraColor));
        int midAura = Color.argb((int) (haloAlpha * 0.35f * 255), Color.red(auraColor), Color.green(auraColor), Color.blue(auraColor));
        int edgeAura = Color.TRANSPARENT;

        RadialGradient auraGrad = new RadialGradient(haloX, haloY, haloRadius,
                new int[]{centerAura, midAura, edgeAura},
                new float[]{0.0f, 0.5f, 1.0f},
                Shader.TileMode.CLAMP);
        auraPaint.setShader(auraGrad);
        canvas.drawCircle(haloX, haloY, haloRadius, auraPaint);

        // Celestial Architectural Compass / Orbital Rings
        int ringColor = isDark ? Color.argb(16, 255, 255, 255) : Color.argb(20, 119, 110, 101);
        ringPaint.setColor(ringColor);

        // Orbit ring 1 (clockwise)
        canvas.save();
        canvas.translate(haloX, haloY);
        canvas.rotate(time * 2.8f);
        ringPaint.setPathEffect(orbitDash1);
        canvas.drawCircle(0, 0, 160 * density, ringPaint);
        canvas.restore();

        // Orbit ring 2 (counter-clockwise)
        canvas.save();
        canvas.translate(haloX, haloY);
        canvas.rotate(-time * 2.0f);
        ringPaint.setPathEffect(orbitDash2);
        canvas.drawCircle(0, 0, 240 * density, ringPaint);
        canvas.restore();
    }

    private void renderWatermarks(Canvas canvas, float width, float height, Theme theme, boolean isDark) {
        int baseColor = isDark ? Color.WHITE : theme.textPrimaryColor;
        watermarkPaint.setColor(baseColor);
        int alpha = isDark ? (int) (0.08f * 255) : (int) (0.09f * 255);
        watermarkPaint.setAlpha(alpha);

        for (int i = 0; i < watermarks.size(); i++) {
            BlueprintWatermark wm = watermarks.get(i);
            float x = wm.relX * width;
            float y = wm.relY * height + (float) Math.sin(time * wm.speed + wm.phase) * 8f * density;
            watermarkPaint.setTextSize(wm.fontSizeSp * getResources().getDisplayMetrics().scaledDensity);
            canvas.drawText(wm.text, x, y, watermarkPaint);
        }
    }

    private void renderStars(Canvas canvas, float width, float height, Theme theme, boolean isDark) {
        for (int i = 0; i < stars.size(); i++) {
            ArcadeStar st = stars.get(i);
            float x = st.relX * width;
            float y = st.relY * height;

            float twinkle = 0.5f + 0.5f * (float) Math.sin(time * st.twinkleSpeed + st.twinklePhase);
            float alpha = st.baseAlpha * (0.4f + 0.6f * twinkle);

            canvas.save();
            canvas.translate(x, y);
            canvas.rotate((float) Math.toDegrees(st.rot));

            int starColor = isDark
                    ? (st.hueOffset > 0.5f ? Color.WHITE : Color.parseColor("#FFD54F"))
                    : (st.hueOffset > 0.5f ? Color.parseColor("#EDC22E") : Color.parseColor("#B8860B"));

            starPaint.setColor(starColor);
            starPaint.setAlpha((int) (alpha * 255));

            float outerRadius = st.sizeDp * density;
            float innerRadius = outerRadius * 0.32f;
            drawStarPath(tempPath, 0, 0, st.points, outerRadius, innerRadius);
            canvas.drawPath(tempPath, starPaint);

            // Glowing center core
            starCorePaint.setAlpha((int) (alpha * 255));
            canvas.drawCircle(0, 0, outerRadius * 0.18f, starCorePaint);

            canvas.restore();
        }
    }

    private void renderSprinkles(Canvas canvas, float width, float height, Theme theme, boolean isDark) {
        int[] palette = new int[]{
                Color.parseColor("#F2B179"), Color.parseColor("#EDCF72"),
                Color.parseColor("#F67C5F"), Color.parseColor("#EDC22E"),
                isDark ? Color.parseColor("#38BDF8") : Color.parseColor("#8B5CF6"),
                isDark ? Color.parseColor("#34D399") : Color.parseColor("#10B981")
        };

        for (int i = 0; i < sprinkles.size(); i++) {
            ConfettiSprinkle s = sprinkles.get(i);
            float x = s.relX * width;
            float y = s.relY * height;

            canvas.save();
            canvas.translate(x, y);
            canvas.rotate((float) Math.toDegrees(s.angleZ));
            canvas.scale(1.0f, (float) Math.cos(s.angleX)); // 3D tumbling perspective

            sprinklePaint.setColor(palette[s.colorIndex % palette.length]);
            sprinklePaint.setAlpha((int) (s.alpha * 255));

            float w = s.wDp * density;
            float h = s.hDp * density;

            if (s.isPill) {
                tempRectF.set(-w / 2f, -h / 2f, w / 2f, h / 2f);
                canvas.drawRoundRect(tempRectF, w / 2f, w / 2f, sprinklePaint);
            } else {
                canvas.drawCircle(0, 0, w / 2f, sprinklePaint);
            }

            canvas.restore();
        }
    }

    private void renderFloatingBlock(Canvas canvas, FloatingBlock b, Theme theme, boolean isDark) {
        canvas.save();

        float currentRot = b.baseRot + (float) Math.sin(time * b.rotSpeed) * b.rotAmp + b.wobble;
        float scale = b.popScale;
        float size = b.sizePx * scale;
        float radius = size * 0.24f;

        canvas.translate(b.x, b.y);
        canvas.rotate((float) Math.toDegrees(currentRot));

        float baseAlpha = b.depth == 2 ? 0.90f : (b.depth == 1 ? 0.72f : 0.45f);

        // 1. Tactile Drop Shadow
        int shadowColor = isDark ? Color.argb((int) (baseAlpha * 140), 0, 0, 0)
                : Color.argb((int) (baseAlpha * 60), 90, 75, 60);
        shadowPaint.setColor(shadowColor);
        float shadowOffset = (4f + b.depth * 4f) * density * scale;
        tempRectF.set(-size / 2f, -size / 2f + shadowOffset, size / 2f, size / 2f + shadowOffset);
        canvas.drawRoundRect(tempRectF, radius, radius, shadowPaint);

        // 2. Tile Face Body
        int tileColor = theme.getTileColor(b.val);
        tilePaint.setColor(tileColor);
        tilePaint.setAlpha((int) (baseAlpha * 255));
        tempRectF.set(-size / 2f, -size / 2f, size / 2f, size / 2f);
        canvas.drawRoundRect(tempRectF, radius, radius, tilePaint);

        // 3. Tactile 3D Bevel & Specular Highlights
        LinearGradient bevelGrad = new LinearGradient(-size / 2f, -size / 2f, size / 2f, size / 2f,
                new int[]{
                        Color.argb((int) (baseAlpha * 100), 255, 255, 255),
                        Color.argb((int) (baseAlpha * 15), 255, 255, 255),
                        Color.argb((int) (baseAlpha * 15), 0, 0, 0),
                        Color.argb((int) (baseAlpha * 60), 0, 0, 0)
                },
                new float[]{0.0f, 0.4f, 0.7f, 1.0f},
                Shader.TileMode.CLAMP);
        bevelPaint.setShader(bevelGrad);
        canvas.drawRoundRect(tempRectF, radius, radius, bevelPaint);

        // Crisp border stroke
        int strokeColor = isDark ? Color.argb((int) (baseAlpha * 35), 255, 255, 255)
                : Color.argb((int) (baseAlpha * 25), 0, 0, 0);
        strokePaint.setColor(strokeColor);
        canvas.drawRoundRect(tempRectF, radius, radius, strokePaint);

        // 4. Diagonal Gloss Sheen
        canvas.save();
        tempPath.reset();
        tempPath.addRoundRect(tempRectF, radius, radius, Path.Direction.CW);
        canvas.clipPath(tempPath);

        tempPath.reset();
        tempPath.moveTo(-size / 2f, -size / 2f);
        tempPath.lineTo(size / 2f, -size / 2f);
        tempPath.lineTo(-size / 2f, size / 2f);
        tempPath.close();

        sheenPaint.setAlpha((int) (baseAlpha * 42));
        canvas.drawPath(tempPath, sheenPaint);
        canvas.restore();

        // 5. Crisp Centered Number Value
        int textColor = theme.getTextColor(b.val);
        textPaint.setColor(textColor);
        textPaint.setAlpha((int) (baseAlpha * 255));

        String valStr = String.valueOf(b.val);
        float fontSize = size * 0.44f;
        if (valStr.length() >= 4) fontSize = size * 0.30f;
        else if (valStr.length() == 3) fontSize = size * 0.36f;
        textPaint.setTextSize(fontSize);

        textPaint.getTextBounds(valStr, 0, valStr.length(), textBounds);
        float textY = (textBounds.height() / 2f) - textBounds.bottom;
        canvas.drawText(valStr, 0, textY, textPaint);

        // 2048 crown star spark badge
        if (b.val >= 2048) {
            canvas.save();
            canvas.translate(size * 0.36f, -size * 0.36f);
            badgePaint.setAlpha((int) (baseAlpha * 255));
            drawStarPath(tempPath, 0, 0, 4, size * 0.16f, size * 0.05f);
            canvas.drawPath(tempPath, badgePaint);
            canvas.restore();
        }

        canvas.restore();
    }

    private void renderBurstParticle(Canvas canvas, SparkBurstParticle p) {
        canvas.save();
        canvas.translate(p.x, p.y);
        canvas.rotate((float) Math.toDegrees(p.rot));

        particlePaint.setColor(p.color);
        particlePaint.setAlpha((int) (p.alpha * 255));

        if (p.type == 0) { // Star
            drawStarPath(tempPath, 0, 0, 4, p.size, p.size * 0.30f);
            canvas.drawPath(tempPath, particlePaint);
        } else if (p.type == 1) { // Pill
            tempRectF.set(-p.size / 2f, -p.size, p.size / 2f, p.size);
            canvas.drawRoundRect(tempRectF, p.size / 2f, p.size / 2f, particlePaint);
        } else { // Circle
            canvas.drawCircle(0, 0, p.size / 2f, particlePaint);
        }

        canvas.restore();
    }

    private void drawStarPath(Path path, float cx, float cy, int spikes, float outerRadius, float innerRadius) {
        path.reset();
        double rot = (Math.PI / 2.0) * 3.0;
        double step = Math.PI / spikes;

        float x = cx + (float) (Math.cos(rot) * outerRadius);
        float y = cy + (float) (Math.sin(rot) * outerRadius);
        path.moveTo(x, y);

        for (int i = 0; i < spikes; i++) {
            x = cx + (float) (Math.cos(rot) * outerRadius);
            y = cy + (float) (Math.sin(rot) * outerRadius);
            path.lineTo(x, y);
            rot += step;

            x = cx + (float) (Math.cos(rot) * innerRadius);
            y = cy + (float) (Math.sin(rot) * innerRadius);
            path.lineTo(x, y);
            rot += step;
        }
        path.close();
    }

    private Theme getEffectiveTheme() {
        if (currentTheme != null) return currentTheme;
        return ThemeManager.getInstance().getCurrentTheme();
    }

    // --- Entity Data Structures ---

    private static class FloatingBlock {
        final int val;
        final float relX, relY;
        final float sizeDp;
        final int depth;
        final float baseRot;
        final float rotSpeed, rotAmp;
        final float phaseX, phaseY, speedX, speedY, ampX, ampY;

        float sizePx;
        float x, y;
        float pushX, pushY;
        float popScale = 1.0f;
        float popVelocity = 0;
        float wobble = 0;

        FloatingBlock(int val, float relX, float relY, float sizeDp, int depth, float baseRot, int index) {
            this.val = val;
            this.relX = relX;
            this.relY = relY;
            this.sizeDp = sizeDp;
            this.depth = depth;
            this.baseRot = baseRot;

            this.rotSpeed = 0.4f + (index % 5) * 0.15f;
            this.rotAmp = 0.08f + (index % 3) * 0.04f;

            this.phaseX = (float) ((index * 1.618) % (Math.PI * 2));
            this.phaseY = (float) ((index * 2.718) % (Math.PI * 2));
            this.speedX = 0.35f + (index % 4) * 0.15f;
            this.speedY = 0.45f + ((index + 2) % 4) * 0.15f;
            this.ampX = 10f + (depth * 5f);
            this.ampY = 14f + (depth * 7f);
        }
    }

    private static class ArcadeStar {
        float relX, relY;
        final float sizeDp;
        final int points;
        final float twinkleSpeed, twinklePhase;
        final float rotSpeed;
        float rot;
        final float baseAlpha;
        final float driftY;
        final float hueOffset;

        ArcadeStar(float relX, float relY, float sizeDp, int points, float twinkleSpeed, float twinklePhase,
                   float rotSpeed, float rot, float baseAlpha, float driftY, float hueOffset) {
            this.relX = relX;
            this.relY = relY;
            this.sizeDp = sizeDp;
            this.points = points;
            this.twinkleSpeed = twinkleSpeed;
            this.twinklePhase = twinklePhase;
            this.rotSpeed = rotSpeed;
            this.rot = rot;
            this.baseAlpha = baseAlpha;
            this.driftY = driftY;
            this.hueOffset = hueOffset;
        }
    }

    private static class ConfettiSprinkle {
        float relX, relY;
        final boolean isPill;
        final float wDp, hDp;
        float angleX, angleZ;
        final float rotSpeedX, rotSpeedZ;
        final float speedYDp;
        final int colorIndex;
        final float alpha;

        ConfettiSprinkle(float relX, float relY, boolean isPill, float wDp, float hDp,
                         float angleX, float angleZ, float rotSpeedX, float rotSpeedZ,
                         float speedYDp, int colorIndex, float alpha) {
            this.relX = relX;
            this.relY = relY;
            this.isPill = isPill;
            this.wDp = wDp;
            this.hDp = hDp;
            this.angleX = angleX;
            this.angleZ = angleZ;
            this.rotSpeedX = rotSpeedX;
            this.rotSpeedZ = rotSpeedZ;
            this.speedYDp = speedYDp;
            this.colorIndex = colorIndex;
            this.alpha = alpha;
        }
    }

    private static class BlueprintWatermark {
        final String text;
        final float relX, relY;
        final float fontSizeSp;
        final float phase, speed;

        BlueprintWatermark(String text, float relX, float relY, float fontSizeSp, float phase, float speed) {
            this.text = text;
            this.relX = relX;
            this.relY = relY;
            this.fontSizeSp = fontSizeSp;
            this.phase = phase;
            this.speed = speed;
        }
    }

    private static class SparkBurstParticle {
        float x, y;
        float vx, vy;
        final int color;
        final float size;
        final int type; // 0=star, 1=pill, 2=circle
        float rot;
        final float rotSpeed;
        float alpha = 1.0f;
        int life = 0;
        final int maxLife;

        SparkBurstParticle(float x, float y, float vx, float vy, int color, float size,
                           int type, float rot, float rotSpeed, int maxLife) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = size;
            this.type = type;
            this.rot = rot;
            this.rotSpeed = rotSpeed;
            this.maxLife = maxLife;
        }
    }

    private static class ShockwaveRing {
        final float x, y;
        float radius;
        final float maxRadius;
        float alpha;
        final int color;

        ShockwaveRing(float x, float y, float radius, float maxRadius, float alpha, int color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.maxRadius = maxRadius;
            this.alpha = alpha;
            this.color = color;
        }
    }
}
