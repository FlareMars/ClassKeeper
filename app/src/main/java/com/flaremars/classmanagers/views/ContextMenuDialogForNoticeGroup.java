package com.flaremars.classmanagers.views;


import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.NoticeGroupObject;

import org.litepal.crud.DataSupport;

public class ContextMenuDialogForNoticeGroup extends DialogFragment {

    private static ContextMenuDialogForNoticeGroup instance;

    private int targetId = 0;

    private int targetNoticeGroup = 0;

    private String curName;

    private EditText renameEditText;


    private ContextMenuDialog.OnDeleteClickListener deleteClickListener;

    private ContextMenuDialog.OnUpClickListener upClickListener;

    /**
     * @param targetNoticeGroup NoticeGroup Item Id
     */
    public static ContextMenuDialogForNoticeGroup getFunctionDialog(ContextMenuDialog.OnDeleteClickListener deleteClickListener,
            ContextMenuDialog.OnUpClickListener upClickListener,int targetNoticeGroup,String curName) {
        if (instance == null) {
            instance = new ContextMenuDialogForNoticeGroup();
            instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        }
        instance.curName = curName;
        instance.deleteClickListener = deleteClickListener;
        instance.upClickListener = upClickListener;
        instance.targetNoticeGroup = targetNoticeGroup;
        if (instance.renameEditText  != null) {
            instance.renameEditText.setText(instance.curName);
        }
        return instance;
    }

    public ContextMenuDialogForNoticeGroup() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = View.inflate(getActivity(), R.layout.layout_context_menu_dialog, null);
        TextView deleteBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_delete);
        TextView upBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_up);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClickListener.onDeleteClick();
                dismiss();
            }
        });

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upClickListener.onUpClick();
                dismiss();
            }
        });

        renameEditText = (EditText) view.findViewById(R.id.et_context_menu_name);
        renameEditText.setText(curName);
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        String name = renameEditText.getText().toString();
        if (!name.equals(instance.curName)) {
            ContentValues cv = new ContentValues();
            cv.put("name",name);
            DataSupport.update(NoticeGroupObject.class,cv,targetNoticeGroup);
        }
    }

}
