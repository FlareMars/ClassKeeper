package com.flaremars.classmanagers.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.GetDataCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.ChattingActivity;
import com.flaremars.classmanagers.model.PictureObject;

import org.litepal.crud.DataSupport;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import libcode.io.DiskLruCache;

/**
 * 加强版图片工具类(配合LeanCloud)
 */
public enum  FlareBitmapUtils {
    INSTANCE;

    //TODO 处理本地图片被删除的情况
    public static final String PICTURE_LOAD_FINISHED = "picture_load_finished";

    private static final String TAG = "FlareBitmapUtil";

    private Context context;

    //图片的默认大小
    private static final int DEFAULT_SIZE = 480;

    //内存缓存
    private static LruCache<String, Bitmap> memoryCache;

    //硬盘缓存
    private static DiskLruCache diskLruCache;

    private Queue<GetBitmapTask> getBitmapTaskQueue;

    private LoadBitmapFinishedListener listener;

    public interface LoadBitmapFinishedListener {
        void onLoadFinished();
    }

    /**
     * 对工具类的缓存操作进行初始化，必须调用
     * @param context context
     */
    public void init(Context context) {
        this.context = context;
        getBitmapTaskQueue = new LinkedBlockingQueue<>();

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

            diskLruCache = DiskLruCache
                    .open(cacheDir, NormalUtils.INSTANCE.getAppVersion(context), 1, 80 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasSomeOneDownloading = false;

    public void setListener(LoadBitmapFinishedListener listener) {
        this.listener = listener;
    }

    public void loadBitmap(ImageView target,String pictureId) {
        List<PictureObject> check = DataSupport.where("pictureId=?",pictureId).find(PictureObject.class);
        if (check.size() == 0) {
            PictureObject object = new PictureObject();
            object.setPictureId(pictureId);
            object.setType(2);
            object.setThumbnail(false);
            object.setTime(new Date().getTime());
            object.setHeight(DEFAULT_SIZE);
            object.setWidth(DEFAULT_SIZE);
            object.save();
            loadBitmap(target, object);
        } else {
            loadBitmap(target, check.get(0));
        }
    }

    /**
     * 将图片显示到指定imageview上
     */
    public void loadBitmap(ImageView target,PictureObject object) {
        /*监控消耗太大*/
        int reqWidth;
        int reqHeight;
        if (target.getWidth() == 0) {
            int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
            int height =View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            target.measure(width, height);

            reqWidth = target.getMeasuredWidth();
            reqHeight = target.getMeasuredWidth();
        } else {
            reqWidth = target.getWidth();
            reqHeight = target.getHeight();
        }

        //一般是聊天界面
        if (reqWidth < 100) {
            reqWidth = ChattingActivity.messageBoxMaxWidth;
            reqHeight = DEFAULT_SIZE;
        }

        //TODO 修改这里的逻辑，为每张图创建多个版本,例如聊天界面版本，相册版本，原图版本
        Bitmap result = getBitmapFromCache(object.getPictureId(), reqWidth, reqHeight);
        if (result != null) {
//            Log.e("TAG",target.getWidth() + " " + target.getHeight());
//            Log.e("TAG",result.getWidth() + " " + result.getHeight());
            target.setImageBitmap(result);
            if (listener != null) {
                listener.onLoadFinished();
                listener = null;
            }
        } else {
            //如果内存中不存在图片数据则开始从后台进行处理获取
            getBitmapTaskQueue.add(new GetBitmapTask(target,object));

            //处理第一个加入的任务
            if (!hasSomeOneDownloading) {
                hasSomeOneDownloading = true;
                GetBitmapTask newOne = getBitmapTaskQueue.poll();
                if (newOne != null && !newOne.getStatus().equals(AsyncTask.Status.RUNNING)) {
                    newOne.execute();
                }
            }
        }
    }

    private class GetBitmapTask extends AsyncTask<Void,Bitmap,Void> {

        private ImageView targetImageView;

        private PictureObject object;

        public GetBitmapTask(ImageView target,PictureObject object) {
            targetImageView = target;
            this.object = object;
        }

        @Override
        protected Void doInBackground(Void... params) {

            int reqWidth;
            int reqHeight;
            if (targetImageView.getWidth() == 0) {
                int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
                int height =View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                targetImageView.measure(width, height);

                reqWidth = targetImageView.getMeasuredWidth();
                reqHeight = targetImageView.getMeasuredWidth();
            } else {
                reqWidth = targetImageView.getWidth();
                reqHeight = targetImageView.getHeight();
            }
            //一般是聊天界面
            if (reqWidth < 100) {
                reqWidth = ChattingActivity.messageBoxMaxWidth;
                reqHeight = DEFAULT_SIZE;
            }
            if (object.getPath() != null && !object.getPath().equals("")) {
                //本地已有数据，直接从本地获取数据
                Bitmap result;
                if (savePictureToCacheFromLocal(object)) {
                    result = getBitmapFromCache(object.getPictureId(),reqWidth,reqHeight);
                } else {
                    result = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_ic_pic_broken);
                }
                publishProgress(result);
            } else {
                //第一次获取的网络图片
                final int finalReqHeight = reqHeight;
                final int finalReqWidth = reqWidth;
                savePictureToCacheFromNetwork(object, new LoadingFinishedListener() {
                    @Override
                    public void onLoadingFinished() {
                        Bitmap result;
//                        if (!object.isThumbnail()) {
//                            result = getBitmapFromCache(object.getPictureId(),-1,-1);
//                        } else {
//                            result = getBitmapFromCache(object.getPictureId(),targetImageView.getWidth(),targetImageView.getHeight());
//                        }
                        result = getBitmapFromCache(object.getPictureId(), finalReqWidth, finalReqHeight);
                        publishProgress(result);
                    }
                });
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {

            Bitmap result = values[0];
            if (targetImageView != null) {
                targetImageView.setImageBitmap(result);

                if (listener != null) {
                    listener.onLoadFinished();
                    listener = null;
                }
            }
            GetBitmapTask nextTask = getBitmapTaskQueue.poll();
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
     * 从内存缓存或者硬盘缓存中获取Bitmap
     * @return 指定id的图片
     */
    public Bitmap getBitmapFromCache(String pictureId,int reqWidth,int reqHeight) {
        String key = NormalUtils.INSTANCE.hashKeyForDisk(pictureId);
        Bitmap result = getBitmapFromMemoryCache(key);
        if (result == null) {
//            Log.e("tag","result == null : width = " + reqWidth + " height = " + reqHeight);
            return getBitmapFromDiskCache(key,reqWidth,reqHeight);
        } else {
            return result;
        }
    }

    /**
     * 将缓存记录同步到journal文件中。
     */
    public void flushCache() {
        if (diskLruCache != null) {
            try {
                diskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 暂停所有正在下载或者等待的下载任务，置空所有任务
     */
    public void cancelAllTask () {
        for (GetBitmapTask task : getBitmapTaskQueue) {
            task.cancel(false);
        }
        if (getBitmapTaskQueue != null) {
            getBitmapTaskQueue.clear();
        }
        hasSomeOneDownloading = false;
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


    private Bitmap getBitmapFromMemoryCache(String key) {
        return memoryCache.get(key);
    }

    private Bitmap getBitmapFromDiskCache(String key,int reqWidth,int reqHeight) {

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
                BitmapFactory.Options options = new BitmapFactory.Options();
//                if (reqHeight != -1 && reqWidth != -1) {
//                }

                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

                float widthScale = (float) options.outWidth / (float) reqWidth;
                float heightScale = (float) options.outHeight / (float) reqHeight;
                float scale = widthScale > heightScale ? widthScale : heightScale;

                options.inSampleSize = Math.round(scale + 0.5f);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);
                if (bitmap != null) {
                    // 将Bitmap对象添加到内存缓存当中
                    addBitmapToMemoryCache(key, bitmap);
                    return bitmap;
                } else {
                    Log.e("FlareBitmapUtils", "getBitmapFromDiskCache -- bitmap == null");
                }
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

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private boolean addBitmapToDiskCache(String key,Bitmap bitmap) {
        DiskLruCache.Snapshot snapShot;
        try {
            // 查找key对应的缓存
            snapShot = diskLruCache.get(key);
            if (snapShot == null) {
                DiskLruCache.Editor editor = diskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (decodeBitmapToStream(bitmap, outputStream)) {
                        editor.commit();
                        return true;
                    } else {
                        editor.abort();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean addBitmapToDiskCache (String key,byte[] data) {
        DiskLruCache.Snapshot snapShot;
        try {
            // 查找key对应的缓存
            snapShot = diskLruCache.get(key);
            if (snapShot == null) {
                DiskLruCache.Editor editor = diskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (decodeByteArrayToStream(data, outputStream)) {
                        editor.commit();
                        return true;
                    } else {
                        editor.abort();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean savePictureToCacheFromLocal(PictureObject pictureObject) {

        String picPath = pictureObject.getPath();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath,options);

        int scaleWidth = (int)(options.outWidth / (float)DEFAULT_SIZE);
        int scaleHeight = (int)(options.outHeight / (float)DEFAULT_SIZE);
        options.inSampleSize = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;

        options.inJustDecodeBounds = false;
        Bitmap result = BitmapFactory.decodeFile(picPath, options);
        if (result != null) {
            return addBitmapToDiskCache(NormalUtils.INSTANCE.hashKeyForDisk(pictureObject.getPictureId()),result);
        } else {
            Log.e(TAG,"savePictureToCacheFromLocal -- result == null");
            return false;
        }
    }

    public interface LoadingFinishedListener {
        void onLoadingFinished();
    }

    public void savePictureToCacheFromNetwork(final PictureObject object, final LoadingFinishedListener listener) {
        try {
            final String pictureId = object.getPictureId();
            final AVFile pictureFile = AVFile.withObjectId(pictureId);
            pictureFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, AVException e) {
                    if (e != null) {
                        NormalUtils.INSTANCE.showError(context,e.getCode(),e.getMessage());
                    } else {
                        if (addBitmapToDiskCache(NormalUtils.INSTANCE.hashKeyForDisk(pictureId),bytes)) {
                            Log.e(TAG,"savePictureToCacheFromNetWork successfully~");
                        } else {
                            Log.e(TAG,"savePictureToCacheFromNetWork fail~");
                        }
                    }
                    int width = DEFAULT_SIZE;
                    int height = DEFAULT_SIZE;
                    Object widthTemp = pictureFile.getMetaData("width");
                    Object heightTemp = pictureFile.getMetaData("height");
                    if (widthTemp != null && heightTemp != null) {
                        width = (Integer) widthTemp;
                        height = (Integer) heightTemp;
                    }
                    object.setHeight(height);
                    object.setWidth(width);
                    object.setName(pictureFile.getOriginalName());
                    listener.onLoadingFinished();
                    switch (object.getType()) {
                        case 0:
                            object.setPath(FileUtils.getInstance().saveHeaderImg(bytes, object.getName()));
                            break;
                        case 1:
                            object.setPath(FileUtils.getInstance().saveThumbnail(bytes, object.getName()));
                            break;
                        case 2:
                            object.setPath(FileUtils.getInstance().savePicture(bytes, object.getName()));
                            break;
                        default:
                    }
                    object.update(object.getId());
                }
            });
        } catch (AVException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean decodeByteArrayToStream(byte[] data,OutputStream outputStream) {
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(outputStream, 8 * 1024);
            out.write(data);
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

    private boolean decodeBitmapToStream(Bitmap res, OutputStream outputStream) {
        BufferedOutputStream out = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
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

    /**
     * 获取一个红色叉叉
     */
    public Drawable getRedXDrawable() {
        final float iconSize = getDimension(com.getbase.floatingactionbutton.R.dimen.fab_icon_size);
        final float iconHalfSize = iconSize / 2.0F;
        float plusSize = this.getDimension(com.getbase.floatingactionbutton.R.dimen.fab_plus_icon_size);
        final float plusHalfStroke = this.getDimension(com.getbase.floatingactionbutton.R.dimen.fab_plus_icon_stroke) / 2.0F;
        final float plusOffset = (iconSize - plusSize) / 2.0F;
        Shape shape = new Shape() {
            public void draw(Canvas canvas, Paint paint) {
                canvas.drawRect(plusOffset, iconHalfSize - plusHalfStroke, iconSize - plusOffset, iconHalfSize + plusHalfStroke, paint);
                canvas.drawRect(iconHalfSize - plusHalfStroke, plusOffset, iconHalfSize + plusHalfStroke, iconSize - plusOffset, paint);
            }
        };
        ShapeDrawable drawable = new ShapeDrawable(shape);
        Paint paint = drawable.getPaint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        RotatingDrawable result = new RotatingDrawable(drawable);
        result.setRotation(135.0F);

        return result;
    }

    private float getDimension(int id) {
        return context.getResources().getDimension(id);
    }

    private class RotatingDrawable extends LayerDrawable {
        private float mRotation;

        public RotatingDrawable(Drawable drawable) {
            super(new Drawable[]{drawable});
        }

        public float getRotation() {
            return this.mRotation;
        }

        public void setRotation(float rotation) {
            this.mRotation = rotation;
            this.invalidateSelf();
        }

        public void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(this.mRotation, (float)this.getBounds().centerX(), (float)this.getBounds().centerY());
            super.draw(canvas);
            canvas.restore();
        }
    }
}
