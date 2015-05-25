package com.flaremars.classmanagers.uis;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.networkmodel.ICMAlbum;
import com.flaremars.classmanagers.model.AlbumsObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.ManagerObject;
import com.flaremars.classmanagers.model.PictureObject;
import com.flaremars.classmanagers.utils.DisplayUtils;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.CircleLoadingView;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.Collections;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class BrowsePhotoActivity extends Activity {

    private static int headerHeight = 0;

    private CircleLoadingView loadingView;

    private PhotoViewAttacher mAttacher;

    private String targetAlbumId;

    private ImageView imageView;

    private String targetPhotoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_photo);


        imageView = (ImageView) findViewById(R.id.iv_browse_photo_content);
        loadingView = (CircleLoadingView) findViewById(R.id.loading_picture_view);
        mAttacher = new PhotoViewAttacher(imageView);
        ImageView deleteBtn = (ImageView) findViewById(R.id.iv_browse_photo_delete);

        if (headerHeight == 0) {
            headerHeight = DisplayUtils.INSTANCE.dp2px(this,50);
        }

        Intent data  = getIntent();
        targetPhotoId = data.getStringExtra("targetPhoto");
        boolean isLocal = data.getBooleanExtra("is_local_picture", false);
        int targetClass = data.getIntExtra("targetClass", -1);
        targetAlbumId = data.getStringExtra("targetAlbum");

        if (isLocal) {
            loadingView.show(true);
            new loadBitmapFromLocal().execute(targetPhotoId);
        } else {
            List<PictureObject> temp = DataSupport.where("pictureId=?", targetPhotoId).find(PictureObject.class);
            PictureObject img;
            if (temp.size() > 0) {
                img = temp.get(0);
                loadingView.show(true);

                new loadBitmapFromLocal().execute(img.getPath());

                //判断是否有资格删除图片
                if (targetClass != -1) {
                    ClassObject classObject = DataSupport.find(ClassObject.class, targetClass);
                    List<ManagerObject> managers = DataSupport.where("classobject_id=?", String.valueOf(classObject.getId())).find(ManagerObject.class);
                    String creator = classObject.getCreator();
                    boolean isCreatorOrManager = false;
                    if (creator.equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
                        isCreatorOrManager = true;
                    } else {
                        for (ManagerObject manager : managers) {
                            if (manager.getManagerID().equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
                                isCreatorOrManager = true;
                                break;
                            }
                        }
                    }

                    if (isCreatorOrManager) {
                        deleteBtn.setVisibility(View.VISIBLE);
                    } else {
                        if (MainActivity.BASE_GLOBAL_DATA.getUserID().equals(img.getOwner())) {
                            deleteBtn.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } else {
                imageView.setImageResource(R.drawable.default_ic_contact);
            }

        }
        findViewById(R.id.iv_browse_photo_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowsePhotoActivity.this.finish();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先完成网络端的数据更新
                AVQuery<ICMAlbum> query = ICMAlbum.getQuery(ICMAlbum.class);
                query.getInBackground(targetAlbumId, new GetCallback<ICMAlbum>() {
                    @Override
                    public void done(final ICMAlbum icmAlbum, AVException e) {
                        if (e == null) {
                            icmAlbum.increment("photoSize", -1);
                            final List<String> curPhotos = icmAlbum.getRealPhotoIds();
                            curPhotos.remove(targetPhotoId);
                            icmAlbum.removeAll("realPhotoIds", Collections.singleton(targetPhotoId));
                            if (curPhotos.size() == 0) {
                                icmAlbum.setFirstPhotoThumbnail("");
                            } else {
                                icmAlbum.setFirstPhotoThumbnail(curPhotos.get(curPhotos.size() - 1));
                            }
                            icmAlbum.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        //网络数据处理成功，可以处理本地
                                        List<AlbumsObject> albumsObjects = DataSupport.where("networkId=?", targetAlbumId).
                                                find(AlbumsObject.class);
                                        AlbumsObject albumsObject = albumsObjects.get(0);
                                        albumsObject.setFirstPhotoThumbnail(icmAlbum.getFirstPhotoThumbnail());
                                        albumsObject.setSizeOfPhotos(albumsObject.getSizeOfPhotos() - 1);
                                        albumsObject.setRealPhotoFromList(curPhotos);
                                        albumsObject.update(albumsObject.getId());
                                        NormalUtils.INSTANCE.showToast(BrowsePhotoActivity.this, "删除图片成功");
                                        BrowsePhotoActivity.this.finish();
                                    } else {
                                        NormalUtils.INSTANCE.showToast(BrowsePhotoActivity.this,"删除图片失败");
                                        Log.e("TAG", e.getCode() + " " + e.getMessage());
                                    }
                                }
                            });
                        } else {
                            NormalUtils.INSTANCE.showToast(BrowsePhotoActivity.this, "删除图片失败");
                            Log.e("TAG",e.getCode() + " " + e.getMessage());
                        }
                    }
                });
            }
        });
    }

    private class loadBitmapFromLocal extends AsyncTask<String,Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            File file = new File(params[0]);
            Bitmap result;
            if (file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(params[0],options);
                float scale = (float)options.outWidth / 1080.0f;
                if (scale < 0) {
                    scale = 1.0f;
                }
                options.inSampleSize = (int)Math.ceil(scale);
                options.inJustDecodeBounds = false;
                result = BitmapFactory.decodeFile(params[0],options);
            } else {
                result = BitmapFactory.decodeResource(BrowsePhotoActivity.this.getResources(),R.drawable.default_ic_pic_broken);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            loadingView.show(false);
            mAttacher.update();
        }
    }

}
