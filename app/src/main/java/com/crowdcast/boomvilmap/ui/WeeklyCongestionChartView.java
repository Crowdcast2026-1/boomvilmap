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
        float bottomLabelHeight = 24f;
        float chartHeight = height - bottomLabelHeight - 8f;
        float gap = 10f;
        float barWidth = (width - gap * (count + 1)) / count;

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(12f);

        for (int i = 0; i < count; i++) {
            float left = gap + i * (barWidth + gap);
            float top = chartHeight - (chartHeight * values[i] / 100f);
            float right = left + barWidth;
            float bottom = chartHeight;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colors[i]);
            canvas.drawRoundRect(new RectF(left, top, right, bottom), 8f, 8f, paint);

            paint.setColor(Color.parseColor("#94A3B8"));
            canvas.drawText(labels[i], left + barWidth / 2f, height - 6f, paint);
        }
    }
}
