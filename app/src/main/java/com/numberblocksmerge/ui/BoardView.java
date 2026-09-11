package com.numberblocksmerge.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.Nullable;
import com.numberblocksmerge.audio.HapticManager;
import com.numberblocksmerge.audio.SoundManager;
import com.numberblocksmerge.engine.Direction;
import com.numberblocksmerge.engine.GameEngine;
import com.numberblocksmerge.engine.MoveResult;
import com.numberblocksmerge.engine.Tile;
import com.numberblocksmerge.theme.Theme;
import com.numberblocksmerge.theme.ThemeManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BoardView extends View {
    private GameEngine gameEngine;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final Rect textBounds = new Rect();
    private GestureDetector gestureDetector;

    private float animationProgress = 1.0f;
    private ValueAnimator moveAnimator;
    private final List<Particle> particles = new ArrayList<>();
    private final List<ScoreFloater> scoreFloaters = new ArrayList<>();
    private final Random random = new Random();

    public interface OnMoveListener {
        void onMove(MoveResult result);
    }
    private OnMoveListener moveListener;

    private static class Particle {
        float x, y, vx, vy, alpha, radius;
        int color;
        Particle(float x, float y, float vx, float vy, int color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.alpha = 0.8f;
            this.radius = 5.0f;
        }
        boolean update() {
            x += vx;
            y += vy;
            vy += 0.35f;
            alpha -= 0.045f;
            radius = Math.max(0, radius - 0.15f);
            return alpha > 0;
        }
    }

    private static class ScoreFloater {
        float x, y, vy, alpha;
        String text;
        int color;
        ScoreFloater(float x, float y, String text, int color) {
            this.x = x;
            this.y = y;
            this.vy = -2.8f;
            this.alpha = 1.0f;
            this.text = text;
            this.color = color;
        }
        boolean update() {
            y += vy;
            vy *= 0.92f;
            alpha -= 0.038f;
            return alpha > 0;
        }
    }

    public BoardView(Context context) { super(context); init(); }
    public BoardView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);

        strokePaint.setStyle(Paint.Style.STROKE);

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 50;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) { return true; }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null || gameEngine == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        handleMove(diffX > 0 ? Direction.RIGHT : Direction.LEFT);
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        handleMove(diffY > 0 ? Direction.DOWN : Direction.UP);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        invalidate();
    }

    public void setOnMoveListener(OnMoveListener listener) {
        this.moveListener = listener;
    }

    private long lastMoveTimestamp = 0;

    public void handleMove(Direction direction) {
        long now = System.currentTimeMillis();
        if (now - lastMoveTimestamp < 60) return; // Prevent dual-trigger from overlapping detectors
        lastMoveTimestamp = now;

        if (gameEngine == null) return;
        MoveResult result = gameEngine.move(direction);
        if (result.isMoved()) {
            if (!result.getMergedTiles().isEmpty()) {
                SoundManager.getInstance().playMerge(result.getMergedTiles().get(0).getValue());
                HapticManager.getInstance().heavyClick();
                for (Tile t : result.getMergedTiles()) {
                    spawnParticles(t);
                    spawnScoreFloater(t);
                }
            } else {
                SoundManager.getInstance().playMove();
                HapticManager.getInstance().click();
            }

            startMoveAnimation();

            if (moveListener != null) {
                moveListener.onMove(result);
            }
        }
    }

    private void startMoveAnimation() {
        if (moveAnimator != null && moveAnimator.isRunning()) {
            moveAnimator.cancel();
        }
        animationProgress = 0.0f;
        moveAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        moveAnimator.setDuration(120);
        moveAnimator.setInterpolator(new DecelerateInterpolator());
        moveAnimator.addUpdateListener(anim -> {
            animationProgress = (float) anim.getAnimatedValue();
            invalidate();
        });
        moveAnimator.start();
    }

    private void spawnParticles(Tile tile) {
        int width = getWidth();
        int size = gameEngine != null ? gameEngine.getSize() : 4;
        float padding = width * 0.035f;
        float gap = width * 0.024f;
        float cellSize = (width - padding * 2 - gap * (size - 1)) / size;
        float cx = padding + tile.getCol() * (cellSize + gap) + cellSize / 2;
        float cy = padding + tile.getRow() * (cellSize + gap) + cellSize / 2;

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        int color = theme.getTileColor(tile.getValue());

        for (int i = 0; i < 14; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            float speed = 2.5f + random.nextFloat() * 5.0f;
            particles.add(new Particle(cx, cy, (float)(Math.cos(angle) * speed), (float)(Math.sin(angle) * speed), color));
        }
    }

    private void spawnScoreFloater(Tile tile) {
        int width = getWidth();
        int size = gameEngine != null ? gameEngine.getSize() : 4;
        float padding = width * 0.035f;
        float gap = width * 0.024f;
        float cellSize = (width - padding * 2 - gap * (size - 1)) / size;
        float cx = padding + tile.getCol() * (cellSize + gap) + cellSize / 2f;
        float cy = padding + tile.getRow() * (cellSize + gap) + cellSize / 2f;

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        scoreFloaters.add(new ScoreFloater(cx, cy - cellSize * 0.22f, "+" + tile.getValue(), theme.textPrimaryColor));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (gameEngine == null) return;

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        int width = getWidth();
        int size = gameEngine.getSize();

        float padding = width * 0.035f;
        float gap = width * 0.024f;
        float cellSize = (width - padding * 2 - gap * (size - 1)) / size;
        float cornerRadius = cellSize * 0.18f;
        float shadowOffset = cellSize * 0.03f;

        // 1. Board Well Base (Architectural matte surface)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.boardColor);
        rectF.set(0, 0, width, width);
        canvas.drawRoundRect(rectF, cornerRadius * 1.4f, cornerRadius * 1.4f, paint);

        // 2. Empty Matrix Cells
        paint.setColor(theme.emptyCellColor);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                float left = padding + c * (cellSize + gap);
                float top = padding + r * (cellSize + gap);
                rectF.set(left, top, left + cellSize, top + cellSize);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
            }
        }

        // 3. Active Tactile Stone Tiles
        Tile[][] grid = gameEngine.getGrid();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Tile tile = grid[r][c];
                if (tile == null) continue;

                float currR = tile.getPrevRow() + (tile.getRow() - tile.getPrevRow()) * animationProgress;
                float currC = tile.getPrevCol() + (tile.getCol() - tile.getPrevCol()) * animationProgress;

                float left = padding + currC * (cellSize + gap);
                float top = padding + currR * (cellSize + gap);

                float scale = 1.0f;
                if (tile.isMerged() && animationProgress > 0.5f) {
                    scale = 1.0f + (float) Math.sin((animationProgress - 0.5f) * 2 * Math.PI) * 0.12f;
                } else if (tile.isNew()) {
                    scale = Math.min(1.0f, animationProgress * 1.15f);
                }

                float half = cellSize / 2f;
                float cx = left + half;
                float cy = top + half;

                canvas.save();
                canvas.scale(scale, scale, cx, cy);

                int tileColor = theme.getTileColor(tile.getValue());

                // Subtle Stone Shadow (clean, natural shadow, no glossy colors)
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(25, 0, 0, 0));
                rectF.set(left, top + shadowOffset, left + cellSize, top + cellSize + shadowOffset);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

                // Main Stone Tile Body
                paint.setColor(tileColor);
                rectF.set(left, top, left + cellSize, top + cellSize);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

                // Hairline Ambient Light Rim
                strokePaint.setColor(Color.argb(35, 255, 255, 255));
                strokePaint.setStrokeWidth(Math.max(1.0f, cellSize * 0.018f));
                rectF.set(left + 1f, top + 1f, left + cellSize - 1f, top + cellSize - 1f);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, strokePaint);

                // Architectural Milestone Frame for 2048+
                if (tile.getValue() >= 2048) {
                    strokePaint.setColor(Color.argb(80, 255, 255, 255));
                    strokePaint.setStrokeWidth(Math.max(1.5f, cellSize * 0.025f));
                    canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, strokePaint);
                }

                // Precision Typography
                paint.setColor(theme.getTextColor(tile.getValue()));
                paint.setFakeBoldText(true);

                String text = String.valueOf(tile.getValue());
                if (text.length() <= 2) {
                    paint.setTextSize(cellSize * 0.42f);
                } else if (text.length() == 3) {
                    paint.setTextSize(cellSize * 0.36f);
                } else if (text.length() == 4) {
                    paint.setTextSize(cellSize * 0.28f);
                } else if (text.length() == 5) {
                    paint.setTextSize(cellSize * 0.22f);
                } else if (text.length() == 6) {
                    paint.setTextSize(cellSize * 0.18f);
                } else {
                    paint.setTextSize(cellSize * 0.15f);
                }

                paint.getTextBounds(text, 0, text.length(), textBounds);
                float textY = cy + textBounds.height() / 2f - textBounds.bottom;
                canvas.drawText(text, cx, textY, paint);

                canvas.restore();
            }
        }

        // Particle System
        boolean needsInvalidate = false;
        if (!particles.isEmpty()) {
            paint.setStyle(Paint.Style.FILL);
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                if (!p.update()) {
                    it.remove();
                } else {
                    paint.setColor(p.color);
                    paint.setAlpha((int) (p.alpha * 255));
                    canvas.drawCircle(p.x, p.y, p.radius, paint);
                }
            }
            needsInvalidate = true;
        }

        // Minimalist Floater Score Popups
        if (!scoreFloaters.isEmpty()) {
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            Iterator<ScoreFloater> fit = scoreFloaters.iterator();
            while (fit.hasNext()) {
                ScoreFloater sf = fit.next();
                if (!sf.update()) {
                    fit.remove();
                } else {
                    paint.setColor(sf.color);
                    paint.setAlpha((int) (sf.alpha * 255));
                    paint.setTextSize(cellSize * 0.32f);
                    canvas.drawText(sf.text, sf.x, sf.y, paint);
                }
            }
            needsInvalidate = true;
        }

        if (needsInvalidate) {
            postInvalidateOnAnimation();
        }
    }
}
