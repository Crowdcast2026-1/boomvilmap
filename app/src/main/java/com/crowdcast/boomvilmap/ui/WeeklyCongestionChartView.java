package com.crowdcast.boomvilmap.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class WeeklyCongestionChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] values = {65, 45, 55, 48, 80, 100, 95};
    private int[] colors = {
            Color.parseColor("#F59E0B"),
            Color.parseColor("#10B981"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#10B981"),
            Color.parseColor("#EF4444"),
            Color.parseColor("#EF4444"),
            Color.parseColor("#EF4444")
    };
    private String[] labels = {"월", "화", "수", "목", "금", "토", "일"};

    public WeeklyCongestionChartView(Context context) {
        super(context);
    }

    public WeeklyCongestionChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WeeklyCongestionChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(int[] values, int[] colors, String[] labels) {
        if (values == null || colors == null || labels == null) return;
        if (values.length != colors.length || values.length != labels.length) return;

        this.values = values;
        this.colors = colors;
        this.labels = labels;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int count = values.length;
        if (count == 0) return;

        float width = getWidth();
        float height = getHeight();
        float bottomLabelHeight = dpToPx(34);
        float chartHeight = height - bottomLabelHeight - dpToPx(8);
        float gap = dpToPx(8);
        float barWidth = (width - gap * (count + 1)) / count;

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(spToPx(11));

        for (int i = 0; i < count; i++) {
            float left = gap + i * (barWidth + gap);
            float top = chartHeight - (chartHeight * values[i] / 100f);
            float right = left + barWidth;
            float bottom = chartHeight;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colors[i]);
            canvas.drawRoundRect(new RectF(left, top, right, bottom), dpToPx(6), dpToPx(6), paint);

            paint.setColor(Color.parseColor("#94A3B8"));
            drawLabel(canvas, labels[i], left + barWidth / 2f, chartHeight + dpToPx(14));
        }
    }

    private void drawLabel(Canvas canvas, String label, float centerX, float firstBaseline) {
        if (label == null) return;
        String[] lines = label.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], centerX, firstBaseline + (i * dpToPx(14)), paint);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
