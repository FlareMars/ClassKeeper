package com.flaremars.classmanagers.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.ManagerObject;

import org.litepal.crud.DataSupport;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 用于获取通用数据的工具类
 */
public enum  NormalUtils {
    INSTANCE;

    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 使用MD5算法对传入的key进行加密并返回。
     */
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
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public void showToast(Context context,String content){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show();
    }


    public void showError(Context context,AVException e){
        NormalUtils.INSTANCE.showError(context, e.getCode(), e.getMessage());
    }

    public void showError(Context context,int code,String content){
        NormalUtils.INSTANCE.showToast(context, content + " 错误码为:" + code);
    }

    public void showErrorLog(Context context,AVException e) {
        Log.e("Error",e.getMessage() + " 错误码：" + e.getCode());
    }

    public boolean isNetworkRegularWork(Context context) {
        ConnectivityManager con=(ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        boolean wifi=con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        boolean internet=con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        return wifi | internet;
    }

    public boolean isCreatorOrManager(ClassObject targetClass) {
        boolean isCreatorOrManager = false;
        List<ManagerObject> managers = DataSupport.where("classobject_id=?", String.valueOf(targetClass.getId())).find(ManagerObject.class);
        String creator = targetClass.getCreator();
        if (creator.equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
            isCreatorOrManager = true;
        } else {
            for (ManagerObject temp : managers) {
                if (temp.getManagerID().equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
                    isCreatorOrManager = true;
                    break;
                }
            }
        }
        return isCreatorOrManager;
    }

    /**
     * 字节转KB/MB
     * @param size 字节数
     * @return KB/MB数
     */
    public String sizeToString(long size) {
        int numForM ;
        int numForK;

        numForM = (int)(size / (1024 * 1024));
        numForK = (int)(size / 1024);

        if (numForM > 0) {
            return numForM + "MB";
        } else {
            return numForK + "KB";
        }
    }
}
