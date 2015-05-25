package com.flaremars.classmanagers.networkmodel;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVObject;
import com.flaremars.classmanagers.model.IAlbum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 班级相册对象
 */
@AVClassName("CMAlbum")
public class ICMAlbum extends AVObject implements IAlbum{

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    //相册名字
    private String name;

    //所属班级
    private AVObject inClass;

    //相册图片的数量
    private Integer photoSize;

    //第一张图片的缩略图
    private String firstPhotoThumbnail;

    //一般显示的缩略图列表
//    private List<String> thumbnails;

    //实际图片的id
    private List<String> realPhotoIds;

    //创建时间
    private Date time;

    public ICMAlbum() {

    }

    public Date getTime() {
        return getDate("time");
    }

    public void setTime(Date time) {
        put("time",time);
        this.time = time;
    }

    public String getName() {
        return getString("name");
    }

    @Override
    public String getCreateTime() {
        return DATE_FORMAT.format(getCreatedAt());
    }

    @Override
    public int getSizeOfPhotos() {
        return photoSize;
    }

    @Override
    public String getAlbumId() {
        return getObjectId();
    }

    public void setName(String name) {
        put("name",name);
    }

    public AVObject getInClass() {
        return getAVObject("inClass");
    }

    public void setInClass(AVObject inClass) {
        put("inClass",inClass);
    }

    public Integer getPhotoSize() {
        return photoSize;
    }

    public void setPhotoSize(Integer photoSize) {
        put("photoSize",photoSize);
    }

    public String getFirstPhotoThumbnail() {
        return getString("firstPhotoThumbnail");
    }

    public void setFirstPhotoThumbnail(String firstPhotoThumbnail) {
        put("firstPhotoThumbnail",firstPhotoThumbnail);
    }

    public List<String> getRealPhotoIds() {
        return getList("realPhotoIds");
    }

    public void setRealPhotoIds(List<String> realPhotoIds) {
        put("realPhotoIds",realPhotoIds);
    }

//    public List<String> getThumbnails() {
//        return thumbnails;
//    }

//    public void setThumbnails(List<String> thumbnails) {
//        this.thumbnails = thumbnails;
//    }
}
