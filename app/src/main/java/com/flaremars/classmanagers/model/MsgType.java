package com.flaremars.classmanagers.model;


public enum MsgType {
    NORMAL_MINE,
    NORMAL_OTHERS,
    EXCEL_MINE,
    EXCEL_OTHERS,
    FILE_MINE,
    FILE_OTHERS;

    public static MsgType valueOf(int i) {
        MsgType result = NORMAL_MINE;
        switch (i) {
            case 0:
                result = NORMAL_MINE;
                break;
            case 1:
                result = NORMAL_OTHERS;
                break;
            case 2:
                result = EXCEL_MINE;
                break;
            case 3:
                result = EXCEL_OTHERS;
                break;
            case 4:
                result = FILE_MINE;
                break;
            case 5:
                result = FILE_OTHERS;
                break;
            default:
        }
        return result;
    }
}
