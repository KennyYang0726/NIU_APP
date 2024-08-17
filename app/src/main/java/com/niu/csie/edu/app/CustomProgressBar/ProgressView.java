package com.niu.csie.edu.app.CustomProgressBar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import com.niu.csie.edu.app.R;

public class ProgressView extends View {
    private Paint paint;
    private float progress; // 0.0 - 1.0
    private Bitmap redBitmap;
    private Bitmap yellowBitmap;
    private Bitmap greenBitmap;
    private Bitmap scaledRedBitmap;
    private Bitmap scaledYellowBitmap;
    private Bitmap scaledGreenBitmap;
    private int width;
    private int height;

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        // 載入不同顏色的圖片
        redBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.progressbar_red);
        yellowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.progressbar_yellow);
        greenBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.progressbar_green);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate(); // 觸發重新繪製
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        // 調整圖片寬度以適應新的寬度，但保持高度不變
        int bitmapHeight = redBitmap.getHeight();
        // 調整圖片大小以適應新的寬度和高度
        scaledRedBitmap = Bitmap.createScaledBitmap(redBitmap, width, bitmapHeight, true);
        scaledYellowBitmap = Bitmap.createScaledBitmap(yellowBitmap, width, bitmapHeight, true);
        scaledGreenBitmap = Bitmap.createScaledBitmap(greenBitmap, width, bitmapHeight, true);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (progress <= 0) {
            return;
        }

        // 計算斜線的長度
        float drawWidth = width * progress;

        // 調整斜線終點位置，使切線更陡峭
        float steepFactor = 2.0f; // 調整這個值，使切線更陡峭或更平緩
        float endY = height / steepFactor;

        // 繪製進度條的路徑
        @SuppressLint("DrawAllocation") Path path = new Path();
        path.moveTo(0, 0); // 左上角
        path.lineTo(drawWidth, 0); // 右上角
        path.lineTo(drawWidth - endY + 10, height); // 右下角
        path.lineTo(0, height); // 左下角
        path.close();

        // 根據進度選擇不同的顏色
        Bitmap progressBitmap;
        if (progress <= 0.2) {
            progressBitmap = scaledRedBitmap;
        } else if (progress <= 0.53) {
            progressBitmap = scaledYellowBitmap;
        } else {
            progressBitmap = scaledGreenBitmap;
        }

        // 裁切並繪製進度條圖片
        canvas.save();
        canvas.clipPath(path);
        canvas.drawBitmap(progressBitmap, 0, 0, paint);
        canvas.restore();
    }
}
