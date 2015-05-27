package com.flaremars.classmanagers.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.flaremars.classmanagers.mainui.SelectFilesActivity;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.model.LocalFileObject;

import org.litepal.crud.DataSupport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author FlareMars on 2014-11-20 21:32
 * 负责各种文件操作--辅助类
 */
public class FileUtils {

    private final  String CM_ROOT = "CMROOT";

    private static final  String MINE_FILES_DIR = "CM_Files";

    private static final  String PHOTOS_DIR = "CM_Images";

    private static final String THUMBNAIL_DIR = "CM_Thumbnails";

    private static final String HEADER_DIR = "CM_Headers";


    private  final String[][] MIME_MapTable = {
            //{后缀名，MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".amr", "audio/amr"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    private static final String [] APPLICATION = {".apk"};
    private static final String [] DOCUMENT = {".doc",".docx",".xls",".xlsx",".ppt",".pptx",".pdf",".txt"};
    private static final String [] PICTURE = {".png",".jpg",".jpeg"};
    private static final String [] VIDEO = {".mp4",".flv",".rmvb",".swf"};
    private static final String [] VOICE = {".amr",".ogg",".mp3"};
    private static final String [] COMPRESSED = {".rar",".zip"};
    private static final String [] SOURCE_FILE = {".cpp",".c",".java",".html"};

    private static final List<String> FILE_END_LIST = new ArrayList<>();

    static {

        Collections.addAll(FILE_END_LIST, DOCUMENT);
        Collections.addAll(FILE_END_LIST, PICTURE);
        Collections.addAll(FILE_END_LIST, VIDEO);
        Collections.addAll(FILE_END_LIST, VOICE);
        Collections.addAll(FILE_END_LIST, COMPRESSED);
        Collections.addAll(FILE_END_LIST, APPLICATION);
        Collections.addAll(FILE_END_LIST, SOURCE_FILE);
    }

    private static final String rootDir = "/storage/";

    private static final String [] TARGET_DIR = {
            "微云保存的文件",
            "tencent/QQ_Collection",
            "tencent/QQ_Favorite",
            "tencent/QQ_Images",
            "tencent/QQfile_recv",
            "CMROOT_CM_Files"
    };

    private static final FileFilter CMFileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String fileName = pathname.getName();

            //如果不可写不可读不可见的，直接跳过
            if (!pathname.canRead() || !pathname.canWrite() || pathname.isHidden()) {
                return false;
            }

            //无后缀名的文件，跳过
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex == -1) {
                return false;
            }

            String end = fileName
                    .substring(lastDotIndex, fileName.length())
                    .toLowerCase();
            return FILE_END_LIST.contains(end);
        }
    };

    private static FileUtils instance = null;

    public static FileUtils getInstance() {
        if (instance == null) {
            synchronized (FileUtils.class){
                if (instance == null)
                    instance = new FileUtils();
            }
        }
        return instance;
    }


    /**
     * 更新本地文件库
     * @param fileName 文件名称
     * @param pathPath 文件路径
     * @param source   更新的来源
     * @param size     文件的大小
     * @param type     文件的类型
     */
    public void updateLocalFile(String fileName,String pathPath,String source,long size,int type) {
        List<LocalFileObject> localFiles = DataSupport.where("fileName=? and size=?",
                fileName,String.valueOf(size)).
                find(LocalFileObject.class);
        LocalFileObject newestLocalFile;

        if (localFiles.size() == 0) {
            newestLocalFile = new LocalFileObject();
            newestLocalFile.setFilePath(pathPath);
            newestLocalFile.setSize(size);
            newestLocalFile.setSource(source);
            newestLocalFile.setFileName(fileName);
            newestLocalFile.setType(type);
            newestLocalFile.setTime(new Date().getTime());
            newestLocalFile.save();

        } else {
            newestLocalFile = localFiles.get(0);
            newestLocalFile.setSource(source);
            newestLocalFile.setTime(new Date().getTime());
            newestLocalFile.update(newestLocalFile.getId());
        }
    }

    private String saveFileToPath(byte[] data,String rootDir,String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        File file = null;
        try {
            File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator +
                    CM_ROOT + File.separator + rootDir);
            if(!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(dir.getPath() + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (file != null && file.exists()) {
            return file.getPath();
        } else {
            return "";
        }
    }

    /**
     * 专门用于save缩略图文件
     * @param data     图片数据
     * @param fileName 图片名称
     */
    public String saveThumbnail(byte[] data,String fileName) {
        return saveFileToPath(data,THUMBNAIL_DIR,fileName);
    }

    /**
     * 专门用于save头像文件
     * @param data     图片数据
     * @param fileName 图片名称
     */
    public String saveHeaderImg(byte[] data,String fileName) {
        return saveFileToPath(data,HEADER_DIR,fileName);
    }

    /**
     * 专门用于save原图文件
     * @param data     图片数据
     * @param fileName 图片名称
     */
    public String savePicture(byte[] data,String fileName) {
        return saveFileToPath(data,PHOTOS_DIR,fileName);
    }

    /**
     * 专门用于save一般文件
     * @param data     图片数据
     * @param fileName 图片名称
     */
    public String saveNormalFile(byte[] data,String fileName) {
        return saveFileToPath(data,MINE_FILES_DIR,fileName);
    }

    /**
     * 遍历系统中所有的文件
     */
    public void listAllFile() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            File rootDir = Environment.getExternalStorageDirectory();
            for (String targetStr : TARGET_DIR) {
                File targetDir = new File(rootDir, targetStr);
                if (targetDir.exists()) {
                    printAllFiles(targetDir);
                }
            }
        }
    }

    private void printAllFiles(File dir) {
        if (!dir.exists()) {
            return;
        }

        File [] files = dir.listFiles(CMFileFilter);
        if (files == null) {
            return;
        }

        for (File fileObject : files) {
            if (fileObject.isFile()) {
                List<LocalFileObject> temp = DataSupport.where("fileName=? and size=?",fileObject.getName(),
                        String.valueOf(fileObject.length())).find(LocalFileObject.class);
                if (temp.size() == 0) {
                    LocalFileObject localFileObject = new LocalFileObject();
                    localFileObject.setSource("本机");
                    if (fileObject.length() > SelectFilesActivity.LIMIT_FILE_SIZE) {
                        continue;
                    }
                    localFileObject.setSize(fileObject.length());
                    localFileObject.setFileName(fileObject.getName());
                    localFileObject.setFilePath(fileObject.getPath());
                    localFileObject.setTime(fileObject.lastModified());
                    localFileObject.setType(getFileType(localFileObject.getFileName()).ordinal());
                    localFileObject.save();
                }
            } else if (fileObject.isDirectory()) {
                printAllFiles(fileObject);
            }
        }
    }

    /**
     * 复制文件
     * @param oldPath 源目录
     * @param newPath 目标目录
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ( (byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();

        }

    }


    /**
     * 获取缩略图的路径
     * @return 软件存放缩略图的路径
     */
    public String getThumnailPath() {
        String path;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            path = Environment.getExternalStorageDirectory().getPath();
            path = path + File.separator + CM_ROOT + File.separator;
        } else {
            path = Environment.getRootDirectory().getPath();
            path = path + File.separator + CM_ROOT + File.separator;
        }
        File dir = new File(path + THUMBNAIL_DIR + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getPath() + File.separator;
    }

    /**
     * 根据全路径返回名字
     */
    public String getFileName(String path) {
        int index = path.lastIndexOf(File.separator);
        return path.substring(index + 1);
    }

    /**
     * 返回文件的MimeType
     */
    private  String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        /* 取得扩展名 */
        String end = fName
                .substring(fName.lastIndexOf("."), fName.length())
                .toLowerCase();

        /* 依扩展名的类型决定MimeType */
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    public FileType getFileType(String fileName) {
        if (fileName == null) {
            return FileType.OTHERS;
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        String end = "others";
        if (lastDotIndex != -1) {
            end = fileName.substring(fileName.lastIndexOf("."), fileName.length()).toLowerCase();
        }
        int index = FILE_END_LIST.indexOf(end);

        if (index == -1) {
            return FileType.OTHERS;
        }

        if (index >= 0 && index <= 7) {
            return FileType.DOCUMENT;
        } else if (index >= 8 && index <= 10) {
            return FileType.PICTURE;
        } else if (index >= 11 && index <= 14) {
            return FileType.VIDEO;
        } else if (index >= 15 && index <= 17) {
            return FileType.VOICE;
        } else if (index >= 18 && index <= 19) {
            return FileType.COMPRESSED;
        } else if (index == 20) {
            return FileType.APPLICATION;
        } else if (index >= 21 && index <= 24) {
            return FileType.SOURCE_FILE;
        } else {
            return FileType.OTHERS;
        }

    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() == null) {
                cachePath = context.getCacheDir().getPath();
            } else {
                cachePath = context.getExternalCacheDir().getPath();
            }
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 删除指定文件
     *
     * @param file 打算删除的文件
     * @return is successful
     */
    public  boolean deleteFile(File file) {
        return file.delete();
    }

    /**
     * 打开文件
     *
     * @param file 文件对象
     */
    public  void openFile(File file, Context context) {
        if (!file.exists()) {
            //有可能是以FileId的形式存在
            List<FileObject> temp = DataSupport.where("fileId=?",file.getPath()).find(FileObject.class);
            if (temp.size() == 0) {
                Toast.makeText(context, "文件不存在！", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            file = new File(temp.get(0).getPath());
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        /* 调用getMIMEType()来取得MimeType */
        String type = getMIMEType(file);
        /* 设置intent的file与MimeType */
        intent.setDataAndType(Uri.fromFile(file), type);

        try {
            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo == null) {
                NormalUtils.INSTANCE.showToast(context,"没有程序能够打开此文件~");
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回“我的文件”文件夹路径
     */
    public  String getMineFilesDir() {
        String path;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            path = Environment.getExternalStorageDirectory().getPath();
            path = path + File.separator + CM_ROOT + File.separator;
        } else {
            path = Environment.getRootDirectory().getPath();
            path = path + File.separator + CM_ROOT + File.separator;
        }
        File dir = new File(path + MINE_FILES_DIR + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getPath() + File.separator;
    }

    /**
     * 返回“图片文件”文件夹路径
     *
     */
    public  File getPhotosDir() {
        String path;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            path = Environment.getExternalStorageDirectory().getPath();
            path = path + File.separator + CM_ROOT + File.separator + PHOTOS_DIR + File.separator;
        } else {
            path = Environment.getRootDirectory().getPath();
            path = path + File.separator + CM_ROOT + File.separator + PHOTOS_DIR + File.separator;
        }
        File temp = new File(path);
        if (!temp.exists()) {
            temp.mkdirs();
        }
        return temp;
    }

    /**
     * 将文件写入“我的文件”文件夹,作用于添加本地文件时
     *
     * @param file 文件对象
     */
    public  boolean saveFileToMineDir(File file){

        String targetPath = getMineFilesDir() + file.getName();

        try {
            FileInputStream fis = new FileInputStream(file);

            File target = new File(targetPath);
            FileOutputStream fos = new FileOutputStream(target);

            byte[] buffer = new byte[1024];
            int bytes = 0;
            while ((bytes = fis.read(buffer,0,buffer.length))!= -1)
            {
                fos.write(buffer,0,bytes);
            }

            fis.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
        return true;
    }

    public  File getHeaderPhotosDir() {
        String path;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            path = Environment.getExternalStorageDirectory().getPath();
            path = path + File.separator + CM_ROOT + File.separator + HEADER_DIR + File.separator;
        } else {
            path = Environment.getRootDirectory().getPath();
            path = path + File.separator + CM_ROOT + File.separator + HEADER_DIR + File.separator;
        }
        File temp = new File(path);
        if (!temp.exists()) {
            temp.mkdirs();
        }
        return temp;
    }

    public String saveHeaderBitmap(String name,Bitmap bitmap) {
        File headerPhotosDir = getHeaderPhotosDir();

        File f = new File(headerPhotosDir,name);
        FileOutputStream fOut = null;
        if (f.exists()) {
            f.delete();
        }
        try {
            if (!f.createNewFile()) {
                return "";
            }
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return f.getPath();
    }

    public  boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);

        Log.e("TAG", "Begin to delete files : " + path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 閸忓牆鍨归梽銈嗘瀮娴犺泛銇欓柌宀勬桨閻ㄥ嫭鏋冩禒锟?
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 根据Uri返回文件路径
     */
    public String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }

        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * 将文件写入“我的文件”文件夹,作用于添加网络文件时
     *
     * @param fileData 文件数据
     * @param fileName 文件名
     */
    public  boolean saveFileToMineDir(byte[] fileData,String fileName){

        String targetPath = getMineFilesDir() + fileName;

        try {
            File target = new File(targetPath);
            FileOutputStream fos = new FileOutputStream(target);
            fos.write(fileData);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
        return true;
    }
}
