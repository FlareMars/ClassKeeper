package com.flaremars.classmanagers.views;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.GetFileCallback;
import com.avos.avoscloud.SaveCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.uis.ChangeClassDialog;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.litepal.crud.DataSupport;

public class ContextMenuDialogForFile extends DialogFragment {

    private static ContextMenuDialogForFile instance;

    private int targetId = 0;

    private ContextMenuDialog.OnDeleteClickListener deleteClickListener;

    private Context context;

    public static ContextMenuDialogForFile getDialog(ContextMenuDialog.OnDeleteClickListener deleteClickListener,int targetId) {
        if (instance == null) {
            instance = new ContextMenuDialogForFile();
            instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        }
        instance.targetId = targetId;
        instance.deleteClickListener = deleteClickListener;

        return instance;
    }

    public ContextMenuDialogForFile() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
//        Bmob.initialize(context, AppConst.APP_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = View.inflate(context, R.layout.layout_context_menu_dialog_file, null);
        TextView deleteBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_delete);
        TextView saveToBtn = (TextView) view.findViewById(R.id.tv_btn_context_menu_saveto);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteClickListener.onDeleteClick();
                dismiss();
            }
        });

        saveToBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ChangeClassDialog dialog = ChangeClassDialog.getClassDialog("转存到...");
                dialog.setOnConfirmClickListener(new ChangeClassDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(ClassObject item) {
                        final FileObject source = DataSupport.find(FileObject.class, targetId);

                        AVQuery<AVObject> classQuery = new AVQuery<>("CMClassObject");
                        classQuery.getInBackground(item.getClassID(), new GetCallback<AVObject>() {
                            @Override
                            public void done(final AVObject avObject, AVException e) {
                                if (e == null) {
                                    AVFile.withObjectIdInBackground(source.getFileId(), new GetFileCallback<AVFile>() {
                                        @Override
                                        public void done(AVFile avFile, AVException e) {
                                            if (e == null) {
                                                final AVObject classFile = new AVObject("CMClassFile");
                                                classFile.put("fileData", avFile);
                                                classFile.put("inClass", avObject);
                                                classFile.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(AVException e) {
                                                        if (e == null) {
                                                            AVRelation<AVObject> filesRelation = avObject.getRelation("files");
                                                            filesRelation.add(classFile);
                                                            avObject.saveInBackground(new SaveCallback() {
                                                                @Override
                                                                public void done(AVException e) {
                                                                    if (e == null) {
                                                                        NormalUtils.INSTANCE.showToast(context, "转发文件成功~");
                                                                    } else {
                                                                        NormalUtils.INSTANCE.showError(context, e);
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            NormalUtils.INSTANCE.showError(context, e);
                                                        }
                                                    }
                                                });
                                            } else {
                                                NormalUtils.INSTANCE.showError(context,e);
                                            }
                                        }
                                    });

                                } else {
                                    NormalUtils.INSTANCE.showError(context, e);
                                }
                            }
                        });
                    }
                });
                dialog.show(getFragmentManager(), "tag_change_class_dialog");
            }
        });
        return view;
    }
}
