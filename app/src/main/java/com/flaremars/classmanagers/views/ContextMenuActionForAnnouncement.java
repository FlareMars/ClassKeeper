package com.flaremars.classmanagers.views;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.flaremars.classmanagers.R;

public class ContextMenuActionForAnnouncement extends DialogFragment {

    private static ContextMenuActionForAnnouncement instance;

    private OnActionClickListener actionClickListener;

    private ContextMenuDialog.OnDeleteClickListener deleteClickListener;

    private String actionName;

    private TextView actionTvBtn;

    //2秒内重复点击删除方可删除
    private long deleteTime = 0;

    public static ContextMenuActionForAnnouncement getFunctionDialog(String actionName,OnActionClickListener actionClickListener,
                                                                     ContextMenuDialog.OnDeleteClickListener deleteClickListener) {
        if (instance == null) {
            instance = new ContextMenuActionForAnnouncement();
            instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        }
        instance.actionName = actionName;
        instance.actionClickListener = actionClickListener;
        instance.deleteClickListener = deleteClickListener;
        return instance;
    }

    public ContextMenuActionForAnnouncement() {
        // Required empty public constructor
    }

    public interface OnActionClickListener {
        void onActionClick();
    }

    @Override
    public void onStart() {
        super.onStart();
        actionTvBtn.setText(actionName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = View.inflate(getActivity(), R.layout.layout_context_menu_one_action, null);

        actionTvBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_action);
        TextView deleteTvBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_action_delete);

        actionTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionClickListener.onActionClick();
                dismiss();
            }
        });

        deleteTvBtn.setText("删除公告");
        deleteTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((System.currentTimeMillis() - deleteTime) > 2000) {
                    Toast.makeText(v.getContext(), "再按一次确认删除", Toast.LENGTH_SHORT).show();
                    deleteTime = System.currentTimeMillis();
                } else {
                    deleteClickListener.onDeleteClick();
                    dismiss();
                }
            }
        });
        return view;
    }


}
