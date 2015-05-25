package com.flaremars.classmanagers.model;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册实体类
 */
public class AlbumsObject extends DataSupport implements IAlbum{
    private int id;

    private String networkId;

    private String name;

    private ClassObject inClass;

    private String createTime;

    private int sizeOfPhotos;

    private String firstPhotoThumbnail;

    private String thumbnails;

    private String realPhotoIds;

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public void setThumbnails(String thumbnails) {
        this.thumbnails = thumbnails;
    }

    public void setRealPhotoIds(String realPhotoIds) {
        this.realPhotoIds = realPhotoIds;
    }

    public void setThumbnailFromList(List<String> thumbnails) {
        StringBuilder sb = new StringBuilder();
        for (String temp : thumbnails) {
            sb.append(temp);
            sb.append("_");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        this.thumbnails = sb.toString();
    }

    public void setRealPhotoFromList(List<String> realPhotoIds) {
        StringBuilder sb = new StringBuilder();
        for (String temp : realPhotoIds) {
            sb.append(temp);
            sb.append("_");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        this.realPhotoIds = sb.toString();
    }

    public String getThumbnails() {
        return thumbnails;
    }

    public String getRealPhotoIds() {
        return realPhotoIds;
    }

    public String[] getRealPhotoIdWithArray() {
        return realPhotoIds == null ? new String[0] :realPhotoIds.split("_");
    }

    public String[] getThumbnailWithArray() {
        return thumbnails == null ? new String[0] :thumbnails.split("_");
    }


    private List<PictureObject> pictureObjectList = new ArrayList<>();

    public String getFirstPhotoThumbnail() {
        return firstPhotoThumbnail;
    }

    public void setFirstPhotoThumbnail(String firstPhotoThumbnail) {
        this.firstPhotoThumbnail = firstPhotoThumbnail;
    }

    public ClassObject getInClass() {
        return inClass;
    }

    public void setInClass(ClassObject inClass) {
        this.inClass = inClass;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getSizeOfPhotos() {
        return sizeOfPhotos;
    }

    @Override
    public String getAlbumId() {
        return networkId;
    }

    public void setSizeOfPhotos(int sizeOfPhotos) {
        this.sizeOfPhotos = sizeOfPhotos;
    }

    public List<PictureObject> getPictureObjectList() {
        return pictureObjectList;
    }

    public void setPictureObjectList(List<PictureObject> pictureObjectList) {
        this.pictureObjectList = pictureObjectList;
    }
}
