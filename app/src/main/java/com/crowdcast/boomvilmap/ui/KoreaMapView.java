package com.crowdcast.boomvilmap.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class KoreaMapView extends View {
    public interface OnSpotClickListener {
        void onSpotClick(int spotId);
    }

    private static class Pin {
        final int id;
        final String name;
        final double lat;
        final double lng;
        final int color;

        Pin(int id, String name, double lat, double lng, int color) {
            this.id = id;
            this.name = name;
            this.lat = lat;
            this.lng = lng;
            this.color = color;
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Pin> pins = new ArrayList<>();
    private OnSpotClickListener listener;

    public KoreaMapView(Context context) {
        super(context);
        init();
    }

    public KoreaMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KoreaMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        pins.add(new Pin(1, "경복궁", 37.58, 126.98, Color.parseColor("#EF4444")));
        pins.add(new Pin(2, "해운대", 35.16, 129.16, Color.parseColor("#F59E0B")));
        pins.add(new Pin(3, "성산일출봉", 33.46, 126.94, Color.parseColor("#10B981")));
        pins.add(new Pin(4, "불국사", 35.79, 129.33, Color.parseColor("#10B981")));
        pins.add(new Pin(5, "남이섬", 37.79, 127.52, Color.parseColor("#F59E0B")));
    }

    public void setOnSpotClickListener(OnSpotClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float sx = w / 390f;
        float sy = h / 360f;

        canvas.drawColor(Color.parseColor("#DBEAFE"));

        canvas.save();
        canvas.scale(sx, sy);
        drawKoreaShape(canvas);
        drawGrid(canvas);
        drawPins(canvas);
        drawMyLocation(canvas);
        canvas.restore();
    }

    private void drawKoreaShape(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#C7D7F0"));

        Path path = new Path();
        path.moveTo(160, 45);
        path.lineTo(185, 42);
        path.lineTo(210, 48);
        path.lineTo(230, 55);
        path.lineTo(245, 70);
        path.lineTo(250, 90);
        path.lineTo(248, 115);
        path.lineTo(242, 135);
        path.lineTo(250, 155);
        path.lineTo(255, 175);
        path.lineTo(258, 200);
        path.lineTo(252, 225);
        path.lineTo(240, 248);
        path.lineTo(225, 268);
        path.lineTo(215, 288);
        path.lineTo(208, 308);
        path.lineTo(200, 318);
        path.lineTo(195, 308);
        path.lineTo(185, 295);
        path.lineTo(175, 278);
        path.lineTo(168, 260);
        path.lineTo(162, 240);
        path.lineTo(155, 218);
        path.lineTo(148, 198);
        path.lineTo(145, 175);
        path.lineTo(142, 152);
        path.lineTo(138, 130);
        path.lineTo(135, 108);
        path.lineTo(138, 88);
        path.lineTo(145, 68);
        path.close();
        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.parseColor("#A5B4CF"));
        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#C7D7F0"));
        canvas.drawOval(new RectF(167, 326, 223, 350), paint);
    }

    private void drawGrid(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.5f);
        paint.setColor(Color.argb(90, 255, 255, 255));

        for (int y : new int[]{80, 130, 180, 230, 280}) {
            canvas.drawLine(120, y, 270, y, paint);
        }
        for (int x : new int[]{150, 180, 210, 240}) {
            canvas.drawLine(x, 40, x, 330, paint);
        }
    }

    private void drawPins(Canvas canvas) {
        for (Pin pin : pins) {
            float[] point = toMapPoint(pin.lat, pin.lng);
            float x = point[0];
            float y = point[1];

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(pin.color);
            paint.setAlpha(55);
            canvas.drawOval(new RectF(x - 28, y - 20, x + 28, y + 20), paint);
            paint.setAlpha(255);

            paint.setColor(pin.color);
            canvas.drawCircle(x, y, 9, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, 9, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, 4, paint);

            paint.setColor(Color.argb(235, 255, 255, 255));
            canvas.drawRoundRect(new RectF(x - 26, y + 13, x + 26, y + 29), 8, 8, paint);

            paint.setColor(Color.parseColor("#1A1F36"));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(8f);
            paint.setFakeBoldText(true);
            canvas.drawText(pin.name, x, y + 24, paint);
            paint.setFakeBoldText(false);
        }
    }

    private void drawMyLocation(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#3B82F6"));
        paint.setAlpha(45);
        canvas.drawCircle(192, 148, 14, paint);
        paint.setAlpha(255);
        canvas.drawCircle(192, 148, 7, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(192, 148, 7, paint);
    }

    private float[] toMapPoint(double lat, double lng) {
        float x = (float) (((lng - 125.5) / 4.5) * 280 + 55);
        float y = (float) (((38.9 - lat) / 6.0) * 340 + 30);
        return new float[]{x, y};
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        float sx = getWidth() / 390f;
        float sy = getHeight() / 360f;
        float touchX = event.getX() / sx;
        float touchY = event.getY() / sy;

        for (Pin pin : pins) {
            float[] point = toMapPoint(pin.lat, pin.lng);
            float dx = touchX - point[0];
            float dy = touchY - point[1];
            if (Math.sqrt(dx * dx + dy * dy) <= 24f) {
                if (listener != null) {
                    listener.onSpotClick(pin.id);
                }
                return true;
            }
        }
        return true;
    }
}
