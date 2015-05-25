package com.flaremars.classmanagers.model;

import java.util.Date;

/**
 * 用于将信息录制对象和通知一起进行比较
 */
public interface NoticeExcelComparetor {
    int NECompareTo(Date time,Boolean newFeedback);
}
