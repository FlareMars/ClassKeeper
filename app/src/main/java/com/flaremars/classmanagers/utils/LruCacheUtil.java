package com.flaremars.classmanagers.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.flaremars.classmanagers.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author FlareMars on 2014-11-21 00:02
 * 主要用于加载超多图片的缓存,防止OOM(OutOfMemory)异常
 */
public class LruCacheUtil {

    private LruCache<String, Bitmap> mMemoryCache;

    private static LruCacheUtil instance = null;

    private LruCacheUtil(){

    }

    /*为了防止多线程的调用，导致产生多个不同的实例，并且只有在第一次使用时进行初始化*/
    /*双重锁定(Double-Check Locking)*/
    public static LruCacheUtil getInstance(){
        if (instance == null) {
            synchronized (LruCacheUtil.class) {
                if (instance == null)
                    instance = new LruCacheUtil();
            }
        }
        return instance;
    }

    /**查看最大内存*/
    private int checkMaxMemory()
    {
        return (int) (Runtime.getRuntime().maxMemory() / 1024);
    }

    /**
     * 预估一下加载整张图片所需占用的内存。
     * @param options 图片的参数。
     * @param reqWidth 根据控件大小，屏幕分辨率等等定出的期望宽度
     * @param reqHeight 根据控件大小，屏幕分辨率等等定出的期望高度
     */
    private int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 压缩图片为指定的像素
     * @param res 图片对象
     * @param reqWidth 指定宽像素
     * @param reqHeight 指定高像素
     * @return
     */
    public Bitmap decodeSampledBitmapFromResource(Bitmap res, int reqWidth, int reqHeight) {

        /*获取相关压缩参数*/
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = res.getHeight();
        options.outWidth = res.getWidth();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        /*bitmap 转 byte[]*/
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        res.compress(Bitmap.CompressFormat.PNG,100,baos);
        Bitmap result = BitmapFactory.decodeByteArray(baos.toByteArray(),0,baos.size(),options);
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**使用内存缓存*/
    public void createLruCache()
    {
        // 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
        // LruCache通过构造函数传入缓存值，以KB为单位。
        int maxMemory = checkMaxMemory();
        // 使用最大可用内存值的1/8作为缓存的大小。
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**从内存中加载图片*/
    public void loadBitmap(String imageUrl, ImageView imageView) {
        String uniqueKey = DiskLruCacheUtil.getInstance().hashKeyForDisk(imageUrl);
        final Bitmap bitmap = getBitmapFromMemCache(uniqueKey);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher);//设置为加载中的默认图片
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            task.execute(uniqueKey,imageUrl);
        }
    }

    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView mImageView;
        public BitmapWorkerTask(ImageView imageView)
        {
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = DiskLruCacheUtil.getInstance().getBmpFromDiskCache(params[0],params[1]);
            addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
        }
    }
}
