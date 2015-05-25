package com.flaremars.classmanagers.views;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.flaremars.classmanagers.R;

/**
 * l用于网络加载时显示的转转圈
 */
public class CircleLoadingView extends ImageView {

    private Animation rotateAnimation;

    public CircleLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setImageResource(R.drawable.circle_circle);
        rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_360);
    }

    public void show (boolean show) {
        if (show) {
            setVisibility(VISIBLE);
            startAnimation(rotateAnimation);
        } else {
            setVisibility(GONE);
            clearAnimation();
        }
    }
}
