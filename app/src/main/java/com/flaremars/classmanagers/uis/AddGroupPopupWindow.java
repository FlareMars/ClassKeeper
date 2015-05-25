package com.flaremars.classmanagers.uis;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.utils.DisplayUtils;

public class AddGroupPopupWindow extends PopupWindow {

    private static AddGroupPopupWindow instance;

    public static final int ACTION_ADD_NOTICE_GROUP = 0;
    public static final int ACTION_ADD_GROUP = 1;

    private int width = 160;

    private int height = 120;

    private LinearLayout addNoticeGroupBtn;

    private View dividerView;

    private static OnActionClickListener listener;

    public AddGroupPopupWindow(Context context) {
        View view = View.inflate(context, R.layout.popup_window_add_group,null);

        width = DisplayUtils.INSTANCE.dp2px(context,width);
        height = DisplayUtils.INSTANCE.dp2px(context,height);
        setContentView(view);
        setWidth(width);
        setHeight(height);

        //以下三个的一，三是必须的，否则不能让window自动消失
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setFocusable(true);
        setOutsideTouchable(true);

        addNoticeGroupBtn = (LinearLayout)view.findViewById(R.id.popup_window_new_notice_group);
        addNoticeGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onActionClickListener(ACTION_ADD_NOTICE_GROUP);
                }
            }
        });

        view.findViewById(R.id.popup_window_new_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onActionClickListener(ACTION_ADD_GROUP);
                }
            }
        });

        dividerView = view.findViewById(R.id.divider);
    }

    public static void setOnActionClickListener(OnActionClickListener listener) {
        AddGroupPopupWindow.listener = listener;
    }

    public interface OnActionClickListener {
        void onActionClickListener(int action);
    }

    public static void showPopupWindow(View parent,boolean isFull) {
        if (instance == null) {
            instance = new AddGroupPopupWindow(parent.getContext());
        }
        if (isFull) {
            instance.setHeight(instance.height);
            instance.addNoticeGroupBtn.setVisibility(View.VISIBLE);
            instance.dividerView.setVisibility(View.VISIBLE);
        } else {
            instance.setHeight(instance.height / 2 );
            instance.addNoticeGroupBtn.setVisibility(View.GONE);
            instance.dividerView.setVisibility(View.GONE);
        }
        instance.showAsDropDown(parent);
    }
}
