package com.flaremars.classmanagers.model;

/**
 * 相册通用接口
 */
public interface IAlbum {
    String getFirstPhotoThumbnail();

    String getName();

    String getCreateTime();

    int getSizeOfPhotos();

    String getAlbumId();
}
