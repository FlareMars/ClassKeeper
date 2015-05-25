package com.flaremars.classmanagers.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.LruCache;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bmob.BmobProFile;
import com.bmob.btp.callback.DownloadListener;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.ChattingActivity;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.model.LocalPhotoObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

import libcode.io.DiskLruCache;

/**
 * 与图片操作相关的工具类
 */
public enum  BitmapUtils {
    INSTANCE;

    //缩略图的最大大小
    private static final int REQ_SIZE = 200;

    public static final int MSG_DOWNLOAD_DONE_FOR_IMAGEVIEW = 0;

    public static final int MSG_DOWNLOAD_DONE_FOR_TEXTVIEW = 1;

    //缓存相关
    private static LruCache<String, Bitmap> memoryCache;

    private static DiskLruCache diskLruCache;

    //图片最大的尺寸
    private int bitmapMaxWidth = 180;

    private Context context;

    private Map<String,ImageView> bitmapTasks;

    private Map<String,TextView> bitmapTasksForTV;

    private LoadingFinishListener listener;

    private int screenWidth = 0;

    private Handler BITMAP_HANDLER = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DOWNLOAD_DONE_FOR_IMAGEVIEW:
                    String tag = (String)msg.obj;
                    ImageView target = bitmapTasks.get(tag);
                    loadBitmap(target,tag);
                    bitmapTasks.remove(tag);
                    break;
                case MSG_DOWNLOAD_DONE_FOR_TEXTVIEW:
                    String tag2 = (String)msg.obj;
                    TextView target2 = bitmapTasksForTV.get(tag2);
                    loadBitmap(target2,tag2);
                    bitmapTasksForTV.remove(tag2);
                    break;
                default:
            }
            if (listener != null) {
                listener.onLoadingFinishListener();
            }
        }
    };

    /**
     * 对工具类的缓存操作进行初始化，必须调用
     * @param context context
     */
    public void initBitmapUtils(Context context) {

        this.context = context;

        bitmapMaxWidth = DisplayUtils.INSTANCE.dp2px(context,bitmapMaxWidth);
        screenWidth = DisplayUtils.INSTANCE.getSystemInfo(context).getScreenWidth();

        bitmapTasks = new HashMap<>();
        bitmapTasksForTV = new HashMap<>();

        if (memoryCache != null && diskLruCache != null) {
            memoryCache.evictAll();
            try {
                if (!diskLruCache.isClosed()) {
                    diskLruCache.flush();
                    diskLruCache.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        // 设置图片缓存大小为程序最大可用内存的1/8
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        try {

            // 获取图片缓存路径
            File cacheDir = FileUtils.getInstance().getDiskCacheDir(context, "thumb");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            //初始化Bmob文件下载的目录(如果手机无sd卡的时候)
//            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//   //             Toast.makeText(context, cacheDir.getPath(), Toast.LENGTH_LONG).show();
//                String photosCachePath = FileUtils.getInstance().getDiskCacheDir(context,"PHOTOSCACHE").getPath();
//                BmobConfiguration config = new BmobConfiguration.Builder(context).customExternalCacheDir(photosCachePath).build();
//                BmobPro.getInstance(context).initConfig(config);
//            }
            // 创建DiskLruCache实例，初始化缓存数据
            diskLruCache = DiskLruCache
                    .open(cacheDir, NormalUtils.INSTANCE.getAppVersion(context), 1, 80 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取图片
     * @param idFromServer 文件或图片上传到服务器时获得的id
     * @return 图片
     */
    public Bitmap getBitmap(String idFromServer) {
        if (idFromServer.equals("")) {
            return BitmapFactory.decodeResource(context.getResources(),R.drawable.default_ic_contact);
        }
        String key = NormalUtils.INSTANCE.hashKeyForDisk(idFromServer);
        Bitmap result = getBitmapFromMemoryCache(key);
        if (result == null) {
            result = getBitmapFromDiskCache(key);
        }
        return result;
    }


    private Queue<DownloadBitmapTask> downloadBitmapTaskSet;

    private GridView targetGridView;

    /**
     * 设置高并发下载任务的图片容易
     * @param gridView 目标父容器
     */
    public void initConcurrencyLoad(GridView gridView) {
        targetGridView = gridView;
        downloadBitmapTaskSet = new LinkedBlockingQueue<>();
    }

    private boolean hasSomeOneDownloading = false;

    /**
     * 为了应付高并发而存在
     * @param thumnailId  目标图片
     *  @param target
     */
    public void loadBitmapForGridView(String thumnailId,ImageView target) {
        if (target == null) {
            Log.e("TAG","target == null");
            return;
        }
        final String key = NormalUtils.INSTANCE.hashKeyForDisk(thumnailId);

        Bitmap result = getBitmapFromMemoryCache(key);
        if (result == null) {
            result = getBitmapFromDiskCache(key);
        } else {
            target.setImageBitmap(result);
            return;
        }

        if (result == null) {
            if (targetGridView != null && downloadBitmapTaskSet != null) {
                downloadBitmapTaskSet.add(new DownloadBitmapTask(target,thumnailId));

                //处理第一个加入的任务
                if (!hasSomeOneDownloading) {
                    hasSomeOneDownloading = true;
                    DownloadBitmapTask newOne = downloadBitmapTaskSet.poll();
                    if (newOne != null && !newOne.getStatus().equals(AsyncTask.Status.RUNNING)) {
                        newOne.execute();
                    }
                }
            }
        } else {
            target.setImageBitmap(result);
        }

    }

    private class DownloadBitmapTask extends AsyncTask<Void,Bitmap,Void> {

        private String targetThumnail;

        private ImageView target;


        public DownloadBitmapTask(ImageView target,String targetThumnail) {
            this.target = target;
            this.targetThumnail = targetThumnail;
        }

        @Override
        protected Void doInBackground(Void... params) {
//            Log.e("TAG","TASK#" + targetThumnail + " start~");
            BmobProFile.getInstance(context).download(targetThumnail, new DownloadListener() {
                @Override
                public void onSuccess(String s) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(s,options);

                    int scaleWidth = (int)(options.outWidth / (float)REQ_SIZE);
                    int scaleHeight = (int)(options.outHeight / (float)REQ_SIZE);
                    options.inSampleSize = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;

                    options.inJustDecodeBounds = false;
                    Bitmap bitmap = BitmapFactory.decodeFile(s,options);
                    if (bitmap == null) {
                        FileUtils.getInstance().deleteFile(new File(s));
                        return;
                    }

                    FileDescriptor fileDescriptor = null;
                    FileInputStream fileInputStream = null;
                    DiskLruCache.Snapshot snapShot;
                    try {
                        final String key = NormalUtils.INSTANCE.hashKeyForDisk(targetThumnail);
                        snapShot = diskLruCache.get(key);
                        if (snapShot == null) {
                            DiskLruCache.Editor editor = diskLruCache.edit(key);
                            if (editor != null) {
                                OutputStream outputStream = editor.newOutputStream(0);
                                if (decodeBitmapToStream(bitmap, outputStream)) {
                                    editor.commit();
                                } else {
                                    editor.abort();
                                }
                            }
                            snapShot = diskLruCache.get(key);
                        }
                        if (snapShot != null) {
                            fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                            fileDescriptor = fileInputStream.getFD();
                        }
                        if (fileDescriptor != null) {
                            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        }
                        if (bitmap != null) {
                            addBitmapToMemoryCache(key, bitmap);
                        }
                        publishProgress(bitmap);
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fileDescriptor == null && fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    publishProgress(new Bitmap[1]);
                }

                @Override
                public void onProgress(String s, int i) {

                }

                @Override
                public void onError(int i, String s) {
                    NormalUtils.INSTANCE.showError(context,i,s);
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            Bitmap result = values[0];
            if (result == null) {
                result = BitmapFactory.decodeResource(context.getResources(),R.drawable.default_ic_pic_broken);
            }
            target.setImageBitmap(result);
//            Log.e("TAG", "TASK#" + targetThumnail + " finished~");
            DownloadBitmapTask nextTask = downloadBitmapTaskSet.poll();
            if (nextTask != null) {
                if (!nextTask.getStatus().equals(Status.RUNNING)) {
                    nextTask.execute();
                }
            } else {
                hasSomeOneDownloading = false;
            }
        }
    }

    /**
     * 暂停所有正在下载或者等待的下载任务，置空所有任务
     */
    public void cancelAllTask () {
        for (DownloadBitmapTask task : downloadBitmapTaskSet) {
            task.cancel(false);
        }
        if (downloadBitmapTaskSet != null) {
            downloadBitmapTaskSet.clear();
        }
        targetGridView = null;
        hasSomeOneDownloading = false;
    }

    public interface LoadingFinishListener {
        void onLoadingFinishListener();
    }

    /**
     * 为本地产生的图片以缩略图的方式加载到界面
     * @param path 需要制成缩略图的路径
     */
    public void loadLocalThumbnail(ImageView target,String path) {

        if (!new File(path).exists()) {
            target.setImageResource(R.drawable.default_ic_pic_broken);
            return;
        }

        String key = NormalUtils.INSTANCE.hashKeyForDisk(path);
        Bitmap result = getBitmapFromMemoryCache(key);

        if (result == null) {
            result = getBitmapFromDiskCache(key);
        } else {
            target.setImageBitmap(result);
            return;
        }

        if (result == null) {
            if (!taskSet.contains(path)) {
                new LocalThumbnailTask(target).execute(path);
            }
        } else {
            target.setImageBitmap(result);
        }
    }

    /**
     * 为本地产生的图片以缩略图的方式加载到界面
     * @param path 需要制成缩略图的路径
     */
    public void loadLocalThumbnail(TextView target,String path) {
        if (target == null) {
            return;
        }

        int size = ChattingActivity.padding2dp;
        target.setPadding(2 * size,2 * size,6 * size,2 * size);

        if (!new File(path).exists()) {
            Bitmap invalidPic = BitmapFactory.decodeResource(context.getResources(),R.drawable.default_ic_pic_broken);
            SpannableString spanString = new SpannableString("pic");
            ImageSpan imgSpan = new ImageSpan(context, invalidPic);
            spanString.setSpan(imgSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            target.setText(spanString);
            return;
        }

        String key = NormalUtils.INSTANCE.hashKeyForDisk(path);
        Bitmap result = getBitmapFromMemoryCache(key);
        SpannableString spanString = new SpannableString(path);

        if (result == null) {
            result = getBitmapFromDiskCache(key);
        } else {
            ImageSpan imgSpan = new ImageSpan(context, result);
            spanString.setSpan(imgSpan, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            target.setText(spanString);
            return;
        }

        if (result == null) {
            new LocalThumbnailTask(target).execute(path);
        } else {
            ImageSpan imgSpan = new ImageSpan(context, result);
            spanString.setSpan(imgSpan, 0, path.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            target.setText(spanString);
        }
    }

    /**
     * 为BrowsePhotoActivity加载原图
     * @param target 目标容器
     * @param idFromServer 图片ID
     * @param listener 图片加载完成后执行的操作容器
     */
    public void loadBitmap(ImageView target,String idFromServer,LoadingFinishListener listener) {
        if (!NormalUtils.INSTANCE.isNetworkRegularWork(context)) {
            target.setImageResource(R.drawable.default_ic_pic_broken);
            return;
        }
        String key = NormalUtils.INSTANCE.hashKeyForDisk(idFromServer);
        Bitmap result = getBitmapFromMemoryCache(key);
        this.listener = listener;
        if (result == null) {
            result = getBitmapFromDiskCache(key);
        } else {
            target.setImageBitmap(result);
            listener.onLoadingFinishListener();
            return;
        }

        if (result == null) {
            bitmapTasks.put(idFromServer,target);
            savePictureToCache(idFromServer, false, MSG_DOWNLOAD_DONE_FOR_IMAGEVIEW);
        } else {
            target.setImageBitmap(result);
            listener.onLoadingFinishListener();
        }
    }

    /**
     * LeanCloud版本
     * 以缓存的方式为ImageView填充图片
     * @param target 需要显示图片的ImageView
     * @param idFromServer 目标图片
     */
    public void loadBitmapL(ImageView target,String idFromServer) {
        String key = NormalUtils.INSTANCE.hashKeyForDisk(idFromServer);
        Bitmap result = getBitmapFromMemoryCache(key);
        if (target == null) {
            return;
        }
        if (result == null) {
            result = getBitmapFromDiskCache(key);
        } else {
            target.setImageBitmap(result);
            return;
        }

        if (result == null) {
            bitmapTasks.put(idFromServer,target);
            savePictureToCache(idFromServer,true,MSG_DOWNLOAD_DONE_FOR_IMAGEVIEW);
        } else {
            target.setImageBitmap(result);
        }
    }

    /**
     * 以缓存的方式为ImageView填充图片
     * @param target 需要显示图片的ImageView
     * @param idFromServer 目标图片
     */
    public void loadBitmap(ImageView target,String idFromServer) {
        String key = NormalUtils.INSTANCE.hashKeyForDisk(idFromServer);
        Bitmap result = getBitmapFromMemoryCache(key);
        if (target == null) {
            return;
        }
        if (result == null) {
            result = getBitmapFromDiskCache(key);
        } else {
            target.setImageBitmap(result);
            return;
        }

        if (result == null) {
            bitmapTasks.put(idFromServer,target);
            savePictureToCache(idFromServer,true,MSG_DOWNLOAD_DONE_FOR_IMAGEVIEW);
        } else {
            target.setImageBitmap(result);
        }
    }

    /**
     * 以缓存的方式为TextView填充图片
     * @param target 需要显示图片的TextVuew
     * @param idFromServer 目标图片
     */
    public void loadBitmap(TextView target,String idFromServer) {
        if (!NormalUtils.INSTANCE.isNetworkRegularWork(context)) {
            Bitmap invalidPic = BitmapFactory.decodeResource(context.getResources(),R.drawable.default_ic_pic_broken);
            SpannableString spanString = new SpannableString(idFromServer);
            ImageSpan imgSpan = new ImageSpan(context, invalidPic);
            spanString.setSpan(imgSpan, 0, idFromServer.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            target.setText(spanString);
            return;
        }
        if (target == null) {
            return;
        }
        String key = NormalUtils.INSTANCE.hashKeyForDisk(idFromServer);
        Bitmap result = getBitmapFromMemoryCache(key);
        SpannableString spanString = new SpannableString(idFromServer);

        if (result == null) {
            result = getBitmapFromDiskCache(key);
        } else {
            ImageSpan imgSpan = new ImageSpan(context, result);
            spanString.setSpan(imgSpan, 0, idFromServer.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            target.setText(spanString);
            return;
        }

        if (result == null) {
            bitmapTasksForTV.put(idFromServer,target);
            savePictureToCache(idFromServer,true,MSG_DOWNLOAD_DONE_FOR_TEXTVIEW);
        } else {
            ImageSpan imgSpan = new ImageSpan(context, result);
            spanString.setSpan(imgSpan, 0, idFromServer.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            target.setText(spanString);
        }
    }

    private ConcurrentSkipListSet<String> taskSet = new ConcurrentSkipListSet<>();

    private class LocalThumbnailTask extends AsyncTask<String,Bitmap,Void> {

        private TextView target;

        private ImageView targetIv;

        private LocalThumbnailTask(ImageView target) {targetIv = target;}

        public LocalThumbnailTask(TextView target) {
            this.target = target;
        }

        @Override
        protected Void doInBackground(String... params) {
            String imgPath = params[0];

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgPath,options);

            int scaleWidth = (int)(options.outWidth / (float)REQ_SIZE);
            int scaleHeight = (int)(options.outHeight / (float)REQ_SIZE);
            options.inSampleSize = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;

            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath,options);
            if (bitmap == null) {
                Log.e("TAG",imgPath);
                FileUtils.getInstance().deleteFile(new File(imgPath));

                bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.default_ic_pic_broken);
                publishProgress(bitmap);
                return null;
            }
            publishProgress(bitmap);
            FileDescriptor fileDescriptor = null;
            FileInputStream fileInputStream = null;
            DiskLruCache.Snapshot snapShot;
            try {
                final String key = NormalUtils.INSTANCE.hashKeyForDisk(imgPath);
                snapShot = diskLruCache.get(key);
                if (snapShot == null) {
                    DiskLruCache.Editor editor = diskLruCache.edit(key);
                    if (editor != null) {
                        OutputStream outputStream = editor.newOutputStream(0);
                        if (decodeBitmapToStream(bitmap, outputStream)) {
                            editor.commit();
                        } else {
                            editor.abort();
                        }
                    }
                    snapShot = diskLruCache.get(key);
                }
                if (snapShot != null) {
                    fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                    fileDescriptor = fileInputStream.getFD();
                }
                if (fileDescriptor != null) {
                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                }
                if (bitmap != null) {
                    addBitmapToMemoryCache(key, bitmap);
                    taskSet.remove(imgPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileDescriptor == null && fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            Bitmap bitmap = values[0];
            if (bitmap == null) {
                return;
            }
            if (target != null) {
                SpannableString spanString = new SpannableString("img");
                ImageSpan imgSpan = new ImageSpan(context, bitmap);
                spanString.setSpan(imgSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                target.setText(spanString);
            } else {
                if (targetIv != null) {
                    targetIv.setImageBitmap(bitmap);
                }
            }
        }
    }

    private boolean decodeUrlToStream(String urlString, OutputStream outputStream) {
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(new FileInputStream(urlString), 8 * 1024);
            out = new BufferedOutputStream(outputStream, 8 * 1024);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;

        } catch (IOException e2) {
            e2.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * LeanCloud版本
     *  对图片首先从网络上获取数据，然后根据参数确定输入的最大size
     * @param idFromServer 图片ID
     * @param isThumbnail 是否需要为缩略图
     * @param type 目标容器是MSG_DOWNLOAD_DONE_FOR_IMAGEVIEW 或者 MSG_DOWNLOAD_DONE_FOR_TEXTVIEW
     */
    private void savePictureToCacheL(final String idFromServer, final boolean isThumbnail,final int type) {

        BmobProFile.getInstance(context).download(idFromServer, new DownloadListener() {
            @Override
            public void onSuccess(String bitmapPath) {
                Log.e("TAG",bitmapPath);

                if (!isThumbnail) {
                    //首先进行图片本地化
                    String newPath = FileUtils.getInstance().getPhotosDir().getPath() + File.separator + idFromServer;
                    FileUtils.getInstance().copyFile(bitmapPath, newPath);
                    File file = new File(bitmapPath);

                    //更新本地文件
                    FileUtils.getInstance().updateLocalFile(idFromServer,newPath,"图片下载",
                            file.length(),FileType.PICTURE.ordinal());

                    LocalPhotoObject newLocalObject = new LocalPhotoObject();
                    newLocalObject.setName(file.getName());
                    newLocalObject.setPath(bitmapPath);
                    newLocalObject.setSize(file.length());
                    newLocalObject.setModifiedTime(new Date().getTime());
                    newLocalObject.save();
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(bitmapPath,options);
                int maxSize = isThumbnail ? bitmapMaxWidth : screenWidth;
                if (options.outWidth > maxSize) {
                    float scale = (float) maxSize / (float) options.outWidth;
                    options.inSampleSize = (int)Math.ceil(1.0 / scale);
                }

                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath,options);
                if (bitmap == null) {
                    Log.e("TAG","bitmap == null");
                    //文件有问题，直接将文件删除
                    FileUtils.getInstance().deleteFile(new File(bitmapPath));
                    //TODO 这里可能会很危险
                    savePictureToCache(idFromServer,isThumbnail,type);
                    return;
                }
                DiskLruCache.Snapshot snapShot;
                FileDescriptor fileDescriptor = null;
                FileInputStream fileInputStream = null;

                try {
                    // 生成图片id对应的key
                    final String key = NormalUtils.INSTANCE.hashKeyForDisk(idFromServer);

                    // 查找key对应的缓存
                    snapShot = diskLruCache.get(key);
                    if (snapShot == null) {
                        DiskLruCache.Editor editor = diskLruCache.edit(key);
                        if (editor != null) {
                            OutputStream outputStream = editor.newOutputStream(0);
                            if (decodeBitmapToStream(bitmap, outputStream)) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                        }

                        // 缓存被写入后，再次查找key对应的缓存
                        snapShot = diskLruCache.get(key);
                    }
                    if (snapShot != null) {
                        fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                        fileDescriptor = fileInputStream.getFD();
                    }
                    // 将缓存数据解析成Bitmap对象
                    if (fileDescriptor != null) {
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    }
                    if (bitmap != null) {
                        // 将Bitmap对象添加到内存缓存当中
                        addBitmapToMemoryCache(key, bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileDescriptor == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Message msg = new Message();
                msg.what = type;
                msg.obj = idFromServer;
                BITMAP_HANDLER.sendMessage(msg);
            }

            @Override
            public void onProgress(String s, int i) {

            }

            @Override
            public void onError(int i, String s) {
                Log.e("LOAD_BITMAP_ERROR",s + "错误码为：" + i);
            }
        });
    }

    /**
     *  对图片首先从网络上获取数据，然后根据参数确定输入的最大size
     * @param idFromServer 图片ID
     * @param isThumbnail 是否需要为缩略图
     * @param type 目标容器是MSG_DOWNLOAD_DONE_FOR_IMAGEVIEW 或者 MSG_DOWNLOAD_DONE_FOR_TEXTVIEW
     */
    private void savePictureToCache(final String idFromServer, final boolean isThumbnail,final int type) {
        BmobProFile.getInstance(context).download(idFromServer, new DownloadListener() {
            @Override
            public void onSuccess(String bitmapPath) {
                Log.e("TAG",bitmapPath);

                if (!isThumbnail) {
                    //首先进行图片本地化
                    String newPath = FileUtils.getInstance().getPhotosDir().getPath() + File.separator + idFromServer;
                    FileUtils.getInstance().copyFile(bitmapPath, newPath);
                    File file = new File(bitmapPath);

                    //更新本地文件
                    FileUtils.getInstance().updateLocalFile(idFromServer,newPath,"图片下载",
                            file.length(),FileType.PICTURE.ordinal());

                    LocalPhotoObject newLocalObject = new LocalPhotoObject();
                    newLocalObject.setName(file.getName());
                    newLocalObject.setPath(bitmapPath);
                    newLocalObject.setSize(file.length());
                    newLocalObject.setModifiedTime(new Date().getTime());
                    newLocalObject.save();
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(bitmapPath,options);
                int maxSize = isThumbnail ? bitmapMaxWidth : screenWidth;
                if (options.outWidth > maxSize) {
                    float scale = (float) maxSize / (float) options.outWidth;
                    options.inSampleSize = (int)Math.ceil(1.0 / scale);
                }

                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath,options);
                if (bitmap == null) {
                    Log.e("TAG","bitmap == null");
                    //文件有问题，直接将文件删除
                    FileUtils.getInstance().deleteFile(new File(bitmapPath));
                    //TODO 这里可能会很危险
                    savePictureToCache(idFromServer,isThumbnail,type);
                    return;
                }
                DiskLruCache.Snapshot snapShot;
                FileDescriptor fileDescriptor = null;
                FileInputStream fileInputStream = null;

                try {
                    // 生成图片id对应的key
                    final String key = NormalUtils.INSTANCE.hashKeyForDisk(idFromServer);

                    // 查找key对应的缓存
                    snapShot = diskLruCache.get(key);
                    if (snapShot == null) {
                        DiskLruCache.Editor editor = diskLruCache.edit(key);
                        if (editor != null) {
                            OutputStream outputStream = editor.newOutputStream(0);
                            if (decodeBitmapToStream(bitmap, outputStream)) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                        }

                        // 缓存被写入后，再次查找key对应的缓存
                        snapShot = diskLruCache.get(key);
                    }
                    if (snapShot != null) {
                        fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                        fileDescriptor = fileInputStream.getFD();
                    }
                    // 将缓存数据解析成Bitmap对象
                    if (fileDescriptor != null) {
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    }
                    if (bitmap != null) {
                        // 将Bitmap对象添加到内存缓存当中
                        addBitmapToMemoryCache(key, bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileDescriptor == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Message msg = new Message();
                msg.what = type;
                msg.obj = idFromServer;
                BITMAP_HANDLER.sendMessage(msg);
            }

            @Override
            public void onProgress(String s, int i) {

            }

            @Override
            public void onError(int i, String s) {
                Log.e("LOAD_BITMAP_ERROR",s + "错误码为：" + i);
            }
        });
    }


    /**
     * 生成缩略图
     * @param source 源图片路径，保证为图片格式文件
     * @return 生成的缩略图路径
     */
    public String getLocalThumnail(String source) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(source,options);

        int scaleWidth = (int)(options.outWidth / (float)(REQ_SIZE * 2));
        int scaleHeight = (int)(options.outHeight / (float)(REQ_SIZE * 2));
        options.inSampleSize = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;

        options.inJustDecodeBounds = false;
        Bitmap result = BitmapFactory.decodeFile(source,options);
        try {
            String targetPath = FileUtils.getInstance().getThumnailPath() +
                    FileUtils.getInstance().getFileName(source) + "_thumnail";
            Log.e("TAG",targetPath);
            FileOutputStream fos = new FileOutputStream(targetPath);
            if (decodeBitmapToStream(result,fos)) {
                return targetPath;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean decodeBitmapToStream(Bitmap res, OutputStream outputStream) {
        BufferedOutputStream out = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (res == null) {
            return false;
        }
        res.compress(Bitmap.CompressFormat.PNG, 100, baos);


        try {
            out = new BufferedOutputStream(outputStream, 8 * 1024);
            out.write(baos.toByteArray());
            return true;
        } catch (IOException e2) {
            e2.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return memoryCache.get(key);
    }

    private Bitmap getBitmapFromDiskCache(String key) {
        DiskLruCache.Snapshot snapShot;
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;

        try {
            // 查找key对应的缓存
            snapShot = diskLruCache.get(key);
            if (snapShot == null) {
                return null;
            }

            //有硬盘缓存无内存缓存的处理
            fileInputStream = (FileInputStream) snapShot.getInputStream(0);
            fileDescriptor = fileInputStream.getFD();
            // 将缓存数据解析成Bitmap对象
            Bitmap bitmap = null;
            if (fileDescriptor != null) {
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            }
            if (bitmap != null) {
                // 将Bitmap对象添加到内存缓存当中
                addBitmapToMemoryCache(key, bitmap);
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileDescriptor == null && fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    /**
     * 将缓存记录同步到journal文件中。
     */
    public void fluchCache() {
        if (diskLruCache != null) {
            try {
                diskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return 切割完成的圆形Bitmap
     */
    public Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }


}
