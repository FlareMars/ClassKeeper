package com.flaremars.classmanagers.model;

import android.support.annotation.NonNull;

import com.flaremars.classmanagers.views.SlideView;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * 最近消息实体类
 */
public class RecentMessageObject extends DataSupport implements Comparable<RecentMessageObject> {
    public static final int TYPE_NOTICE_GROUP = 0;
    public static final int TYPE_PERSON = 1;
    public static final int TYPE_GROUP = 2;
    public static final int TYPE_NOTICE = 3;
    public static final int TYPE_EXCEL = 4;

    private int id;

    private String imgPath;

    private String name;

    private int type;

    private String content;

    private int noReadNumber;

    private Date time;

    private int targetID;

    private ClassObject inClass;

    private boolean isAlwaysTop;

    private UserObject owner;

    public SlideView slideView;

    public UserObject getOwner() {
        return owner;
    }

    public void setOwner(UserObject owner) {
        this.owner = owner;
    }

    public boolean isAlwaysTop() {
        return isAlwaysTop;
    }

    public void setAlwaysTop(boolean isAlwaysTop) {
        this.isAlwaysTop = isAlwaysTop;
    }

    public ClassObject getInClass() {
        return inClass;
    }

    public void setInClass(ClassObject inClass) {
        this.inClass = inClass;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNoReadNumber() {
        return noReadNumber;
    }

    public void setNoReadNumber(int noReadNumber) {
        this.noReadNumber = noReadNumber;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getTargetID() {
        return targetID;
    }

    public void setTargetID(int targetID) {
        this.targetID = targetID;
    }

    @Override
    public int compareTo(@NonNull RecentMessageObject another) {
        //置顶最大
        if (this.isAlwaysTop && !another.isAlwaysTop) {
            return -1;
        }
        if (!this.isAlwaysTop && another.isAlwaysTop) {
            return 1;
        }
        //未读次之
        if (this.getNoReadNumber() > 0 && another.getNoReadNumber() == 0) {
            return -1;
        }
        if (this.getNoReadNumber() == 0 && another.getNoReadNumber() > 0) {
            return 1;
        }

        //如果未读性相同，则比较时间
        return -this.time.compareTo(another.getTime());
    }
}
