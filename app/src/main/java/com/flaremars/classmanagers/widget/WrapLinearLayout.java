package com.flaremars.classmanagers.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.flaremars.classmanagers.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;


public class WrapLinearLayout extends LinearLayout {

    private int designedGroupWidth = 0;

    private int designedChildHeight = 40;

    private List<LinearLayout> childrenGroupView = new ArrayList<>();

    //当前的行数
    private int curRow = 0;

    //当前所在行的长度
    private int curWidth = 0;

    public WrapLinearLayout(Context context) {
        super(context);

        init(context);
    }

    public WrapLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        designedGroupWidth = dm.widthPixels - DisplayUtils.INSTANCE.dp2px(context,30);
        designedChildHeight = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, designedChildHeight, getResources()
                        .getDisplayMetrics()));
        addGroupView();
    }

    private void addGroupView () {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, designedChildHeight));
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        addView(linearLayout);
        childrenGroupView.add(linearLayout);
        curRow++;
        curWidth = 0;
    }

    public void removeChildView(View view) {
        int targetPosition = 0;
        boolean isFirstRound = true;
        //首先删除被点击的Child
        for (int i = 0; i < childrenGroupView.size();i++) {
            LinearLayout temp = childrenGroupView.get(i);
            if (view.getParent() == temp) {
                temp.removeView(view);
                if (i > 0) {
                    targetPosition = i - 1;
                } else {
                    targetPosition = 0;
                }
                break;
            }
        }

        //向后迭代进行children前推
        for (int i = targetPosition; i < childrenGroupView.size() - 1;i++) {
            LinearLayout tempNextParent = childrenGroupView.get(i + 1);
            LinearLayout tempParent = childrenGroupView.get(i);

            //获取当前行groupview的宽度
            int tempParentWidth = 0;
            for (int j = 0;j < tempParent.getChildCount();j++) {
                tempParentWidth += tempParent.getChildAt(j).getWidth();
            }

            //记录需要移动的子View
            List<View> movedChildren = new ArrayList<>();
            int tempWholeWidth = tempParentWidth;
            for (int k = 0;k < tempNextParent.getChildCount();k++) {
                View temp = tempNextParent.getChildAt(k);
                if (tempWholeWidth + temp.getWidth() <= designedGroupWidth) {
                    movedChildren.add(temp);
                    tempWholeWidth += temp.getWidth();
                } else {
                    break;
                }
            }

            //如果下一行的第一个child太长，就不需要迭代了
            if (!isFirstRound && movedChildren.size() == 0) {
                //                       movedChildWidth = -1;
                break;
            }

            //进入了正常的迭代情况
            if (isFirstRound) {
                isFirstRound = false;
            }

            //进行children前推
            for (View temp : movedChildren) {
                tempNextParent.removeView(temp);
                tempParent.addView(temp);
            }
        }

        //处理parent所有children被删除的情况
        LinearLayout lastParent = childrenGroupView.get(childrenGroupView.size()-1);
        if (childrenGroupView.size() > 1 && lastParent.getChildCount() == 0) {
            WrapLinearLayout.this.removeView(lastParent);
            if (childrenGroupView.size() == 1) {
                return;
            }
            curRow--;
            childrenGroupView.remove(lastParent);
        }

        lastParent = childrenGroupView.get(childrenGroupView.size()-1);
        int tempParentWidth = 0;
        for (int j = 0;j < lastParent.getChildCount();j++) {
            tempParentWidth += lastParent.getChildAt(j).getWidth();
        }

        curWidth = tempParentWidth;
    }

    public void addChildView(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        width = view.getMeasuredWidth();

        if ((width + curWidth) > designedGroupWidth) {
            addGroupView();
        }

        childrenGroupView.get(curRow - 1).addView(view);
        curWidth += width;

    }
}
