package com.flaremars.classmanagers.views;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.uis.ContactsFragment;
import com.flaremars.classmanagers.uis.ContainerActivity;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.litepal.crud.DataSupport;

import java.util.List;

public class ContextMenuDialogForNoticeGroup2 extends DialogFragment {

    private static ContextMenuDialogForNoticeGroup2 instance;

    private int targetGroup = 0;

    private String curName;

    private EditText renameEditText;

    private Context context;

    private String targetClass;

    private ContextMenuDialog.OnDeleteClickListener deleteClickListener;

    //0代表通知组 1代表子群
    private int type = 0;

    public static ContextMenuDialogForNoticeGroup2 getFunctionDialog(ContextMenuDialog.OnDeleteClickListener deleteClickListener,
                                                                    String targetClass, int targetGroup,String curName,int type) {
        if (instance == null) {
            instance = new ContextMenuDialogForNoticeGroup2();
            instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        }
        instance.targetClass = targetClass;
        instance.type = type;
        instance.curName = curName;
        instance.deleteClickListener = deleteClickListener;
        instance.targetGroup = targetGroup;
        return instance;
    }

    public ContextMenuDialogForNoticeGroup2() {
        // Required empty public constructor
    }

    public interface OnDialogHideListener {
        void onDialogHide();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = View.inflate(getActivity(), R.layout.layout_context_menu_dialog_notice_group, null);
        TextView deleteBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_delete);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClickListener.onDeleteClick();
                dismiss();
            }
        });

        renameEditText = (EditText) view.findViewById(R.id.et_context_menu_name);
        renameEditText.setText(curName);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (instance.renameEditText  != null) {
            instance.renameEditText.setText(instance.curName);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        final String name = renameEditText.getText().toString();
        if (!name.equals(instance.curName)) {
            final ContentValues cv = new ContentValues();
            if (type == 0) {
                //直接本地修改通知组名称
                cv.put("name", name);
                cv.put("isCustomName",true);
                DataSupport.update(NoticeGroupObject.class, cv, targetGroup);
                ((ContainerActivity) context).replaceFragment(ContactsFragment.newInstance(instance.targetClass), false);

            } else {
                //修改子群（会话）名称属性
                SubClassObject subClassObject = DataSupport.find(SubClassObject.class,targetGroup);

                AVIMConversationQuery query1 = CMApplication.imClient.getQuery();
                query1.whereEqualTo("objectId",subClassObject.getSubClassID());
                query1.findInBackground(new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> list, AVException e) {
                        if (e == null) {
                            AVIMConversation target = list.get(0);
                            target.setName(name);
                            target.updateInfoInBackground(new AVIMConversationCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        cv.put("subClassName", name);
                                        DataSupport.update(SubClassObject.class, cv, targetGroup);
                                        ((ContainerActivity) context).replaceFragment(ContactsFragment.newInstance(instance.targetClass), false);
                                    } else {
                                        NormalUtils.INSTANCE.showToast(context,"修改子群名称失败 : " + e.getMessage());
                                    }
                                }
                            });
                         } else {
                            NormalUtils.INSTANCE.showToast(context,"修改子群名称失败 : " + e.getMessage());
                        }
                    }
                });
            }
        }
    }
}
