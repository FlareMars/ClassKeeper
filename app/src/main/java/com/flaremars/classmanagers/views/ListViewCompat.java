package com.flaremars.classmanagers.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.flaremars.classmanagers.model.RecentMessageObject;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;


public class ListViewCompat extends ObservableListView {

    private static final String TAG = "ListViewCompat";

    private SlideView mFocusedItemView;

    /*需要达到的最小距离*/
    private int mMinDistance;

    /*标记用户是否正在滑动*/
    private boolean mSwipping;

    private float mDownX;
    private float mDownY;

    private float lastDownX;


    public ListViewCompat(Context context) {
        super(context);
        ViewConfiguration vc = ViewConfiguration.get(context);
        mMinDistance = vc.getScaledTouchSlop();
    }

    public ListViewCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration vc = ViewConfiguration.get(context);
        mMinDistance = vc.getScaledTouchSlop();
    }

    public ListViewCompat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ViewConfiguration vc = ViewConfiguration.get(context);
        mMinDistance = vc.getScaledTouchSlop();
    }

    public void shrinkListItem(int position) {
        View item = getChildAt(position);

        if (item != null) {
            try {
                ((SlideView) item).shrink();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = event.getX();
                mDownY = event.getY();
                int position = pointToPosition(x, y);
                if (position != INVALID_POSITION) {
                    RecentMessageObject data = (RecentMessageObject) getItemAtPosition(position);
                    if (data != null) {
                        mFocusedItemView = data.slideView;
                    }
                }

            }
            break;
            case MotionEvent.ACTION_MOVE:{
               // 获取X方向滑动的距离
                float deltaX = x - mDownX;
                // 获取Y方向滑动的距离
                float deltaY = y - mDownY;

                // 获取X方向的位移距离
                int distance = x - (int)lastDownX;

                // X方向滑动的距离大于mMinDistance并且Y方向滑动的距离小于mMinDistance，则可进行滑动
                if (Math.abs(deltaX) > mMinDistance && Math.abs(deltaY) < mMinDistance) {
                    mSwipping = true;
                    //当手指滑动item,取消item的点击事件，否额滑动Item也伴随着item点击事件的发生
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (event.getActionIndex()<< MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    onTouchEvent(cancelEvent);
                }

                if (mSwipping) {
                    if (mFocusedItemView == null) {
                        return super.onTouchEvent(event);
                    }
                    int newScroll = mFocusedItemView.getScrollX() - distance;
                    if (newScroll < 0) {
                        newScroll = 0;
                    } else if (newScroll > SlideView.getHolderWidth()) {
                        newScroll = SlideView.getHolderWidth();
                    }

                    mFocusedItemView.scrollTo(newScroll,0);
                }
            }
            break;
            case MotionEvent.ACTION_UP:{
                mSwipping = false;

                if (mFocusedItemView != null) {
                    mFocusedItemView.onRequireTouchEvent(event);
                }
            }
            default:
            break;
        }
        lastDownX = x;
        return super.onTouchEvent(event);
    }

}
