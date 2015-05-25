package com.flaremars.classmanagers.model;

/**
 * 标识文件类型
 */
public enum FileType {


    DOCUMENT,      //doc,excel
    PICTURE,  //jpg,png
    VIDEO,      //zip,rar
    VOICE,    //mp4,rmvb
    COMPRESSED,    //mp3
    APPLICATION,//apk
    SOURCE_FILE,   //.c .cpp .java
    OTHERS;

    public static FileType valueOfInt(int i) {
        switch (i) {
            case 0:
                return DOCUMENT;
            case 1:
                return PICTURE;
            case 2:
                return VIDEO;
            case 3:
                return VOICE;
            case 4:
                return COMPRESSED;
            case 5:
                return APPLICATION;
            case 6:
                return SOURCE_FILE;
            default:
                return OTHERS;
        }
    }
}
