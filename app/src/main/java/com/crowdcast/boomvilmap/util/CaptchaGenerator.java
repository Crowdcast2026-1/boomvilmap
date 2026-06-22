package com.crowdcast.boomvilmap.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.Random;

public class CaptchaGenerator {

    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 100;
    private static final int LENGTH = 5; // 캡챠 글자 수

    // 결과값
    public static class Captcha {
        public Bitmap image;
        public String answer;

        public Captcha(Bitmap image, String answer) {
            this.image = image;
            this.answer = answer;
        }
    }

    public static Captcha generate() {
        Random random = new Random();
        StringBuilder answerBuilder = new StringBuilder();

        // 캔버스 불러오기
        Bitmap bitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // 붓 세팅
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(60);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // 무작위 문자열 생성 + 비뚤게 그리기
        int startX = 30;
        for (int i = 0; i < LENGTH; i++) {
            char c = CHARS[random.nextInt(CHARS.length)];
            answerBuilder.append(c);

            // 랜덤 색상 부여
            textPaint.setColor(Color.rgb(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            // 랜덤 기울기
            textPaint.setTextSkewX((random.nextFloat() - 0.5f) * 0.6f);
            // 랜덤 높이
            int startY = 60 + random.nextInt(20);

            canvas.drawText(String.valueOf(c), startX, startY, textPaint);
            startX += 50;
        }

        // 노이즈 추가
        Paint linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(3);
        for (int i = 0; i < 7; i++) {
            linePaint.setColor(Color.rgb(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            canvas.drawLine(
                    random.nextInt(DEFAULT_WIDTH), random.nextInt(DEFAULT_HEIGHT),
                    random.nextInt(DEFAULT_WIDTH), random.nextInt(DEFAULT_HEIGHT),
                    linePaint
            );
        }

        // 점 노이즈
        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.LTGRAY);
        for (int i = 0; i < 100; i++) {
            canvas.drawCircle(random.nextInt(DEFAULT_WIDTH), random.nextInt(DEFAULT_HEIGHT), 2, dotPaint);
        }

        return new Captcha(bitmap, answerBuilder.toString());
    }
}