package com.flaremars.classmanagers.views;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.utils.NormalUtils;

public class ContextMenuDialog extends DialogFragment {

    private static ContextMenuDialog instance;

    private OnDeleteClickListener deleteClickListener;

    private OnUpClickListener upClickListener;

    private boolean isTargetTop;

    public static ContextMenuDialog getFunctionDialog(OnDeleteClickListener deleteClickListener,OnUpClickListener upClickListener,
                                                      boolean isAlwaysTop) {
        if (instance == null) {
            instance = new ContextMenuDialog();
            instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        }
        instance.isTargetTop = isAlwaysTop;
        instance.deleteClickListener = deleteClickListener;
        instance.upClickListener = upClickListener;
        return instance;
    }

    public ContextMenuDialog() {
        // Required empty public constructor
    }

    public interface OnDeleteClickListener {
        void onDeleteClick();
    }

    public interface OnUpClickListener {
        void onUpClick();
    }

    @Override
    public void onStart() {
        super.onStart();
        upBtn.setText(isTargetTop?"取消置顶":"置顶");
    }

    private TextView upBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = View.inflate(getActivity(), R.layout.layout_context_menu_dialog_simple, null);

        TextView deleteBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_delete);
        upBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_up);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NormalUtils.INSTANCE.showToast(upBtn.getContext(),"暂不支持删除子群功能");
//                deleteClickListener.onDeleteClick();
//                dismiss();
            }
        });

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upClickListener.onUpClick();
                dismiss();
            }
        });
        return view;
    }


}
