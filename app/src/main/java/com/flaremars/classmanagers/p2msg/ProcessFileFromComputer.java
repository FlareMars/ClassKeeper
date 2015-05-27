package com.flaremars.classmanagers.p2msg;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.GetFileCallback;
import com.avos.avoscloud.ProgressCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.uis.DownloadProgressDialog;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.Dialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

public class ProcessFileFromComputer implements IProcessMsg{
    @Override
    public void process(JSONObject content, final Context context, UserObject thisUser) {
        try {
            String fileId = content.getString(MessageConst.CONTENT_TARGET_ID);
            AVFile.withObjectIdInBackground(fileId, new GetFileCallback<AVFile>() {
                @Override
                public void done(final AVFile avFile, AVException e) {
                    if (e == null) {

                        final String fileName = avFile.getOriginalName();
                        final long fileSize = avFile.getSize();
                        final int fileType = FileUtils.getInstance().getFileType(fileName).ordinal();

                        String infoFormat = "文件名称 : %s \n 文件大小 : %s";
                        final Dialog dialog = new Dialog(MainActivity.MainActivityInstance, "待接收文件",
                                String.format(infoFormat, avFile.getOriginalName(), NormalUtils.INSTANCE.sizeToString(avFile.getSize())));
                        dialog.addCancelButton("取消");
                        dialog.setButtonAcceptText("确认下载");
                        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final DownloadProgressDialog downloadProgressDialog = DownloadProgressDialog.getDownloadDialog();
                                if (MainActivity.MainActivityInstance == null) {
                                    NormalUtils.INSTANCE.showToast(context,"显示进度条失败,文件后台下载");
                                } else {
                                    downloadProgressDialog.show(MainActivity.MainActivityInstance.getSupportFragmentManager(), "download_dialog");
                                }
                                avFile.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, AVException e) {
                                        if (e == null) {
                                            String newPath = FileUtils.getInstance().saveNormalFile(bytes, avFile.getOriginalName());

                                            FileUtils.getInstance().updateLocalFile(fileName, newPath, "外部文件源",
                                                    fileSize, fileType);

                                            //为了在“已下载”文件夹中显示
                                            FileObject newFileObject = new FileObject();
                                            newFileObject.setTime(new Date());
                                            newFileObject.setPath(newPath);
                                            newFileObject.setUpdateTime(new Date());
                                            newFileObject.setName(fileName);
                                            newFileObject.setSource("外部文件源");
                                            newFileObject.setType(fileType);
                                            newFileObject.setSize(fileSize);
                                            newFileObject.save();

                                            Toast.makeText(CMApplication.GLOBAL_CONTEXT, "下载成功~", Toast.LENGTH_LONG).show();
                                            FileUtils.getInstance().openFile(new File(newPath), CMApplication.GLOBAL_CONTEXT);
                                        } else {
                                            NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e);
                                        }
                                    }
                                }, new ProgressCallback() {
                                    @Override
                                    public void done(Integer integer) {
                                        downloadProgressDialog.setProgress(integer);
                                    }
                                });
                            }
                        });
                        dialog.show();
                    } else {
                        NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
