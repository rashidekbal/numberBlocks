package com.redcodersgroup.numberblocks.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.redcodersgroup.numberblocks.theme.Theme;
import com.redcodersgroup.numberblocks.theme.ThemeManager;

public class BoardPreviewView extends View {
    private int gridSize = 4;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final Rect textBounds = new Rect();

    private static final int[][][] PREVIEW_MATRICES = {
        // 4x4
        {
            {0, 2, 4, 0},
            {8, 16, 0, 0},
            {0, 32, 64, 128},
            {0, 256, 1024, 2048}
        },
        // 5x5
        {
            {0, 2, 0, 4, 0},
            {8, 16, 32, 0, 0},
            {0, 0, 64, 128, 0},
            {0, 256, 512, 0, 0},
            {1024, 2048, 4096, 8192, 0}
        },
        // 6x6
        {
            {0, 4, 0, 0, 8, 0},
            {16, 32, 0, 0, 64, 0},
            {0, 0, 128, 256, 0, 0},
            {0, 512, 1024, 0, 0, 2048},
            {4096, 0, 0, 8192, 0, 0},
            {16384, 0, 0, 0, 0, 0}
        }
    };

    public BoardPreviewView(Context context) {
        super(context);
        init();
    }

    public BoardPreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardPreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public void setGridSize(int size) {
        if (size < 4 || size > 6) size = 4;
        if (this.gridSize != size) {
            this.gridSize = size;
            invalidate();
        }
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setTheme(Theme theme) {
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size;
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            size = Math.min(widthSize, heightSize);
        } else if (widthMode == MeasureSpec.EXACTLY) {
            size = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY) {
            size = heightSize;
        } else {
            size = Math.min(widthSize, heightSize);
            if (size == 0) size = (int) (220 * getResources().getDisplayMetrics().density);
        }
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        if (size <= 0) return;

        int gridDim = gridSize;
        int matrixIdx = gridDim - 4;
        if (matrixIdx < 0 || matrixIdx >= PREVIEW_MATRICES.length) matrixIdx = 0;
        int[][] matrix = PREVIEW_MATRICES[matrixIdx];

        float padding = size * (10f / 220f);
        float gap = size * (gridDim == 4 ? (7f / 220f) : gridDim == 5 ? (5f / 220f) : (4f / 220f));
        float cellSize = (size - padding * 2 - gap * (gridDim - 1)) / gridDim;
        float cornerRadius = size * (6f / 220f);
        float boardRadius = size * (20f / 220f);

        // 1. Board Well Base
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.boardColor);
        rectF.set(0, 0, size, size);
        canvas.drawRoundRect(rectF, boardRadius, boardRadius, paint);

        // 2. Empty Matrix Cells
        paint.setColor(theme.emptyCellColor);
        for (int r = 0; r < gridDim; r++) {
            for (int c = 0; c < gridDim; c++) {
                float left = padding + c * (cellSize + gap);
                float top = padding + r * (cellSize + gap);
                rectF.set(left, top, left + cellSize, top + cellSize);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
            }
        }

        // 3. Active Tactile Preview Tiles
        float shadowOffsetY = size * (1.5f / 220f);
        for (int r = 0; r < gridDim; r++) {
            for (int c = 0; c < gridDim; c++) {
                int val = matrix[r][c];
                if (val <= 0) continue;

                float left = padding + c * (cellSize + gap);
                float top = padding + r * (cellSize + gap);

                // Subtle Tile Shadow
                paint.setColor(Color.argb(40, 0, 0, 0));
                rectF.set(left, top + shadowOffsetY, left + cellSize, top + cellSize + shadowOffsetY);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

                // Tile Background
                paint.setColor(theme.getTileColor(val));
                rectF.set(left, top, left + cellSize, top + cellSize);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

                // Tile Text
                String text = String.valueOf(val);
                float textSize;
                if (text.length() >= 5) {
                    textSize = cellSize * 0.22f;
                } else if (text.length() >= 4) {
                    textSize = cellSize * 0.28f;
                } else if (text.length() >= 3) {
                    textSize = cellSize * 0.36f;
                } else {
                    textSize = cellSize * 0.44f;
                }
                textPaint.setTextSize(textSize);
                textPaint.setColor(theme.getTextColor(val));

                textPaint.getTextBounds(text, 0, text.length(), textBounds);
                float textY = rectF.centerY() + (textBounds.height() / 2f) - textBounds.bottom;
                canvas.drawText(text, rectF.centerX(), textY, textPaint);
            }
        }
    }
}