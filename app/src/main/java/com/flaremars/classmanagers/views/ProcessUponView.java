package com.flaremars.classmanagers.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 覆盖在其他View上面的Process控件
 */
public class ProcessUponView extends View {

    private int process = 0;

    private Paint paint;

    private int width;

    private int height;

    private int heightPercent;

    public ProcessUponView(Context context) {
        super(context);
        init();
    }

    public ProcessUponView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setARGB(140,0,0,0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = 100;
        heightPercent = height / 100;
        Log.e("TAG",width + " " + height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画背景
        process = 40;
        canvas.drawRect(0,0,width,height - process * heightPercent,paint);

        invalidate();
    }
}
