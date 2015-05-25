package com.flaremars.classmanagers.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.flaremars.classmanagers.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import libcode.io.DiskLruCache;

/**
 * @author FlareMars on 2014-11-20 11:20
 * 负责图片硬盘缓存--辅助类
 */
public class DiskLruCacheUtil {

    private DiskLruCache mDiskLruCache = null;

    private static DiskLruCacheUtil instance = null;

    private DiskLruCacheUtil(){}

    public static DiskLruCacheUtil getInstance(){
        if (instance == null) {
            synchronized (DiskLruCacheUtil.class){
                if (instance == null) {
                    instance = new DiskLruCacheUtil();
                }
            }
        }
        return instance;
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**打开硬盘缓存*/
    public void openDiskLruCache(Context context)  {
        try {
            File cacheDir = getDiskCacheDir(context, "bitmap");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**关闭硬盘缓存*/
    public void closeDiskLruCache(){
        if (mDiskLruCache != null)
            try {
                mDiskLruCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**同步硬盘缓存日志文件*/
    public void flushDiskLruCache()
    {
        try {
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*获取对应图片名字的唯一键值*/
    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 写入到缓存中
     * @param imageUrl 是服务器上保存图片的路径
     * @return 图片唯一缓存码,失败则返回空字符串
     */
    public String writeToDiskCache(String imageUrl) {
        try {
            String key = hashKeyForDisk(imageUrl);
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                if (HttpUtils.downloadPhotoToStream(imageUrl, outputStream)) {
                    editor.commit();
                    return key;
                } else {
                    editor.abort();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 写入到缓存中
     * @param uniqueKey 图片唯一缓存码
     * @return 图片
     */
    public Bitmap getBmpFromDiskCache(String uniqueKey,String imageUrl){
        try {
            DiskLruCache.Snapshot snapShot = mDiskLruCache.get(uniqueKey);
            if (snapShot != null) {
                InputStream is = snapShot.getInputStream(0);
                return BitmapFactory.decodeStream(is);
            }
            /*如果图片不在缓存中，则重新从服务器中获取*/
            else {
                String key = writeToDiskCache(imageUrl);
                if (!key.equals(""))
                    return getBmpFromDiskCache(key, imageUrl);
                else
                    /*如果服务器也没有相关图片，则只使用默认图片*/
                    return BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.ic_launcher);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
