package com.flaremars.classmanagers.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.networkmodel.ICMAlbum;
import com.flaremars.classmanagers.model.AlbumsObject;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.model.LocalFileObject;
import com.flaremars.classmanagers.model.LocalPhotoObject;
import com.flaremars.classmanagers.model.PictureObject;
import com.flaremars.classmanagers.model.UserObject;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 用于控制文件上传的工具类 (普通文件和图片)
 */
public enum UploadUtils {
    INSTANCE;

    private static boolean isUploading = false;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

    public interface UploadListener {
        void onUploadFinished();

        void onUploadProgress(int progress);
    }

    public interface UploadFilesListener {
        void onUploadFilesFinished(List<FileObject> fileObjects);
    }

    /**
     * 上传个人头像
     * @param photo    个人圆图头像
     * @param listener 监听上传状态
     */
    public void uploadPersonalHeaderImg(final Bitmap photo,final AVObject userInfo, final UploadListener listener) {
        //首先要上传
        final SharedPreferences sharedPreferences = CMApplication.GLOBAL_CONTEXT.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        final String userId = sharedPreferences.getString(AppConst.USER_ID, "");

        final String name = String.format("%s_header_%s.png",userId,DATE_FORMAT.format(new Date()));
        final String path = FileUtils.getInstance().saveHeaderBitmap(name, photo);
        try {
            final AVFile avFile = AVFile.withAbsoluteLocalPath(name,path);
            avFile.addMetaData("isThumbnail",true);
            avFile.addMetaData("width", photo.getWidth());
            avFile.addMetaData("height", photo.getHeight());
            avFile.addMetaData("type",0);
            avFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        listener.onUploadFinished();

                        //更新本地数据
                        PictureObject pictureObject = new PictureObject();
                        pictureObject.setTime(new Date().getTime());
                        pictureObject.setHeight(photo.getHeight());
                        pictureObject.setWidth(photo.getWidth());
                        pictureObject.setName(name);
                        pictureObject.setPath(path);
                        pictureObject.setPictureId(avFile.getObjectId());
                        pictureObject.setType(0);
                        pictureObject.setThumbnail(true);
                        pictureObject.save();

                        if (MainActivity.BASE_GLOBAL_DATA != null) {
                            MainActivity.BASE_GLOBAL_DATA.setUserHeaderId(avFile.getObjectId());
                        }
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(AppConst.USER_HEADER_ID, avFile.getObjectId());
                        editor.apply();

                        ContentValues cv = new ContentValues();
                        cv.put("imgId", avFile.getObjectId());
                        DataSupport.updateAll(UserObject.class, cv, "userId=?", userId);

                        //更新网络数据
                        userInfo.put("headerImg", avFile.getObjectId());
                        userInfo.saveInBackground();

                    } else {
                        NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e.getCode(), e.getMessage());
                    }
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer integer) {
                    listener.onUploadProgress(integer);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传设置班级头像
     * @param photo    班级圆图头像
     * @param listener 监听上传状态
     */
    public void uploadClassHeaderImg(final Bitmap photo,final AVObject classObject,final ClassObject targetClassObject, final UploadListener listener) {
        String classId = classObject.getString("classId");
        final String name = String.format("%s_header_%s.png",classId,DATE_FORMAT.format(new Date()));
        final String path = FileUtils.getInstance().saveHeaderBitmap(name, photo);
        try {
            final AVFile avFile = AVFile.withAbsoluteLocalPath(name,path);
            avFile.addMetaData("isThumbnail",true);
            avFile.addMetaData("width", photo.getWidth());
            avFile.addMetaData("height", photo.getHeight());
            avFile.addMetaData("type",0);
            avFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        listener.onUploadFinished();

                        //更新本地数据
                        PictureObject pictureObject = new PictureObject();
                        pictureObject.setTime(new Date().getTime());
                        pictureObject.setHeight(photo.getHeight());
                        pictureObject.setWidth(photo.getWidth());
                        pictureObject.setName(name);
                        pictureObject.setPath(path);
                        pictureObject.setPictureId(avFile.getObjectId());
                        pictureObject.setType(0);
                        pictureObject.setThumbnail(true);
                        pictureObject.save();

                        ContentValues cv = new ContentValues();
                        cv.put("headerPath", avFile.getObjectId());
                        DataSupport.update(ClassObject.class, cv, targetClassObject.getId());

                        //更新网络数据
                        classObject.put("headerImg", avFile.getObjectId());
                        classObject.saveInBackground();

                    } else {
                        NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e.getCode(), e.getMessage());
                    }
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer integer) {
                    listener.onUploadProgress(integer);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UploadHandler handler = new UploadHandler();

    private static int curSize = 0;

    private static int targetSize = 0;

    private static final int START_TO_UPLOAD = 0;

    private static final int PROGRESS_UPLOAD = 1;

    private static class UploadHandler extends Handler {

        private UploadFinishedInternalListener listener;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int what = msg.what;
            switch (what) {
                case START_TO_UPLOAD:
                    curSize = 0;
                    targetSize = (Integer) msg.obj;
                    Log.e("TAG",targetSize + "");
                    isUploading = true;
                    break;
                case PROGRESS_UPLOAD:
                    curSize++;
                    Log.e("TAG",curSize + "");
                    if (curSize == targetSize) {
                        Log.e("TAG","finished~");
                        listener.onUploadFinished();
                        listener = null;

                        Intent uploadFinishedBroadcast = new Intent("cm.action.upload_finished");
                        CMApplication.GLOBAL_CONTEXT.sendBroadcast(uploadFinishedBroadcast);
                    }

                    isUploading = false;
                    break;
                default:
            }
        }

        public void setListener(UploadFinishedInternalListener listener) {
            this.listener = listener;
        }

        private interface UploadFinishedInternalListener {
            void onUploadFinished();
        }
    }

    public boolean getUploadState() {
        return isUploading;
    }

    /**
     * 上传班级文件
     * @param files       待上传文件
     * @param targetClass 目标班级
     * @param listener    上传状态监听器
     */
    public void uploadClassFiles(final List<Integer> files,final ClassObject targetClass,final UploadFilesListener listener) {
        int size = files.size();
        long ids[] = new long[size];
        for (int i = 0;i < size;i++) {
            ids[i] = files.get(i);
        }
        final List<LocalFileObject> fileObjects = DataSupport.findAll(LocalFileObject.class,ids);
        AVQuery<AVObject> query = new AVQuery<>("CMClassObject");
        query.getInBackground(targetClass.getClassID(), new GetCallback<AVObject>() {
            @Override
            public void done(final AVObject cmClassObject, AVException e) {
                if (e == null) {
                    final int size = fileObjects.size();
                    final List<FileObject> result = new ArrayList<>();
                    Message startMsg = new Message();
                    startMsg.what = START_TO_UPLOAD;
                    startMsg.obj = size;
                    handler.sendMessage(startMsg);

                    handler.setListener(new UploadHandler.UploadFinishedInternalListener() {
                        @Override
                        public void onUploadFinished() {
                            if (listener != null) {
                                listener.onUploadFilesFinished(result);
                            }
                        }
                    });

                    for (int i = 0;i < size;i++) {
                        final LocalFileObject file = fileObjects.get(i);
                        try {
                            final AVFile uploadedFile = AVFile.withAbsoluteLocalPath(file.getFileName(),file.getFilePath());
                            uploadedFile.addMetaData("size",file.getSize());
                            uploadedFile.addMetaData("type",file.getType());
                            uploadedFile.addMetaData("source",MainActivity.BASE_GLOBAL_DATA.getUserRealName());
//                            final int finalI = i;
                            uploadedFile.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {

                                        //绑定到指定班级
                                        final AVObject classFile = new AVObject("CMClassFile");
                                        classFile.put("fileData", uploadedFile);
                                        classFile.put("inClass", cmClassObject);
                                        classFile.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(AVException e) {
                                                if (e == null) {
                                                    AVRelation<AVObject> filesRelation = cmClassObject.getRelation("files");
                                                    filesRelation.add(classFile);
                                                    cmClassObject.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(AVException e) {
                                                            if (e == null) {
                                                                //保存班级文件到本地
                                                                FileObject classFile = new FileObject();
                                                                classFile.setUpdateTime(new Date());
                                                                classFile.setPath(file.getFilePath());
                                                                classFile.setInClass(targetClass);
                                                                classFile.setName(file.getFileName());
                                                                classFile.setFileId(uploadedFile.getObjectId());
                                                                classFile.setSize(file.getSize());
                                                                classFile.setType(file.getType());
                                                                classFile.setSource(MainActivity.BASE_GLOBAL_DATA.getUserRealName());
                                                                classFile.save();

                                                                //更新本地文件
                                                                FileUtils.getInstance().updateLocalFile(classFile.getName(), classFile.getPath(), "上传到 " + targetClass.getName(),
                                                                        classFile.getSize(), classFile.getType());

                                                                result.add(classFile);

                                                                Message progressMsg = new Message();
                                                                progressMsg.what = PROGRESS_UPLOAD;
                                                                handler.sendMessage(progressMsg);

//                                                                if (finalI == size - 1) {
//                                                                    listener.onUploadFilesFinished(result);
//                                                                }
                                                            } else {
                                                                isUploading = false;
                                                                Log.e("TAG1", e.getMessage());
                                                                NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e);
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    isUploading = false;
                                                    NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e);
                                                }
                                            }
                                        });
                                    } else {
                                        isUploading = false;
                                        NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT, e);
                                    }
                                }
                            }, new ProgressCallback() {
                                @Override
                                public void done(Integer integer) {

                                }
                            });
                        } catch (IOException e2) {
                            isUploading = false;
                            e2.printStackTrace();
                        }
                    }
                } else {
                    isUploading = false;
                    Log.e("TAG",e.getMessage());
                    NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT,e);
                }
            }
        });
    }

    /**
     * 上传图片到指定班级的相册中
     * @param photoIds     待上传图片
     * @param albumsObject 目的相册
     */
    public void uploadClassPhotos(List<Integer> photoIds, final AlbumsObject albumsObject, final UploadFilesListener listener) {
        int size = photoIds.size();
        long ids[] = new long[size];
        for (int i = 0;i < size;i++) {
            ids[i] = photoIds.get(i);
        }
        final List<LocalPhotoObject> photoObjects = DataSupport.findAll(LocalPhotoObject.class,ids);

        AVQuery<ICMAlbum> query = ICMAlbum.getQuery(ICMAlbum.class);
        query.getInBackground(albumsObject.getNetworkId(), new GetCallback<ICMAlbum>() {

            @Override
            public void done(final ICMAlbum avObject, AVException e) {
                if (e == null) {
                    final int size = photoObjects.size();
                    final List<String> photoObjectIds = new ArrayList<>();

                    Message startMsg = new Message();
                    startMsg.what = START_TO_UPLOAD;
                    startMsg.obj = size;
                    handler.sendMessage(startMsg);
                    handler.setListener(new UploadHandler.UploadFinishedInternalListener() {
                        @Override
                        public void onUploadFinished() {
                            //更新本地相册
                            List<AlbumsObject> temp = DataSupport.where("networkId=?",avObject.getObjectId()).
                                    find(AlbumsObject.class);
                            if (temp.size() > 0) {
                                AlbumsObject targetLocalAlbum = temp.get(0);
                                targetLocalAlbum.setFirstPhotoThumbnail(photoObjectIds.get(size - 1));
                                targetLocalAlbum.setSizeOfPhotos(targetLocalAlbum.getSizeOfPhotos() + size);
                                List<String> cur = avObject.getRealPhotoIds();
                                cur.addAll(photoObjectIds);
                                targetLocalAlbum.setRealPhotoFromList(cur);
                                targetLocalAlbum.update(albumsObject.getId());
                            }

                            //更新相册数据
                            avObject.addAllUnique("realPhotoIds",photoObjectIds);
                            avObject.increment("photoSize",size);
                            avObject.setFirstPhotoThumbnail(photoObjectIds.get(size - 1));
                            avObject.saveInBackground();

                            if (listener != null) {
                                listener.onUploadFilesFinished(new ArrayList<FileObject>());
                            }
                        }
                    });

                    for (int i = 0;i < size;i++) {
                        final LocalPhotoObject photoObject = photoObjects.get(i);
                        try {
                            final AVFile photoFile = AVFile.withAbsoluteLocalPath(photoObject.getName(),photoObject.getPath());
                            photoFile.addMetaData("isThumbnail",true);
                            photoFile.addMetaData("width", photoObject.getWidth());
                            photoFile.addMetaData("height", photoObject.getHeight());
                            photoFile.addMetaData("type",1);
                            photoFile.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        photoObjectIds.add(photoFile.getObjectId());
                                        //更新本地数据
                                        PictureObject pictureObject = new PictureObject();
                                        pictureObject.setTime(new Date().getTime());
                                        pictureObject.setHeight(photoObject.getHeight());
                                        pictureObject.setWidth(photoObject.getWidth());
                                        pictureObject.setName(photoObject.getName());
                                        pictureObject.setPath(photoObject.getPath());
                                        pictureObject.setPictureId(photoFile.getObjectId());
                                        pictureObject.setType(1);
                                        pictureObject.setThumbnail(true);
                                        pictureObject.save();

                                        FileUtils.getInstance().updateLocalFile(photoObject.getName(), photoObject.getPath(), "上传到 " + albumsObject.getName(),
                                                photoObject.getSize(), FileType.PICTURE.ordinal());

                                        Message progressMsg = new Message();
                                        progressMsg.what = PROGRESS_UPLOAD;
                                        handler.sendMessage(progressMsg);
                                    } else {
                                        isUploading = false;
                                        NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT,e);
                                    }
                                }
                            });
                        } catch (IOException e1) {
                            isUploading = false;
                            e1.printStackTrace();
                        }
                    }
                } else {
                    isUploading = false;
                    NormalUtils.INSTANCE.showError(CMApplication.GLOBAL_CONTEXT,e);
                }
            }
        });
    }
}
