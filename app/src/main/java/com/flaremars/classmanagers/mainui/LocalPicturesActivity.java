package com.flaremars.classmanagers.mainui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.model.LocalPhotoObject;
import com.flaremars.classmanagers.utils.BitmapUtils;
import com.flaremars.classmanagers.utils.DisplayUtils;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.views.ButtonRectangle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocalPicturesActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int GET_PHOTO_ACTION = 1;

    private static final String[] STORE_IMAGES = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
    };

    private List<ItemObject> photoObjects;

    private PhotoAdapter adapter;

    private static int itemWidth = 0;

    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_local_pictures);
        BitmapUtils.INSTANCE.initBitmapUtils(this);

        if (itemWidth == 0) {
            int screenWidth = DisplayUtils.INSTANCE.getSystemInfo(this).getScreenWidth();
            int blankFieldWidth = DisplayUtils.INSTANCE.dp2px(this, 22);
            itemWidth = (screenWidth - blankFieldWidth) / 3;
        }

        calendar = Calendar.getInstance(Locale.CHINA);
        calendar.add(Calendar.WEEK_OF_MONTH, -1);

        GridView contentGridView = (GridView) findViewById(R.id.gv_fragment_photos_content);
        ButtonRectangle btn = (ButtonRectangle) findViewById(R.id.btn_to_local_store);
        btn.setRippleSpeed(24.0F);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, GET_PHOTO_ACTION);
            }
        });

        photoObjects = new ArrayList<>();
        List<LocalPhotoObject> photos = DataSupport.where("modifiedTime > ?",String.valueOf(calendar.getTimeInMillis() / 1000)).
                order("modifiedTime desc").limit(30).find(LocalPhotoObject.class);
        for (LocalPhotoObject object : photos) {
            ItemObject temp = new ItemObject(object);
            photoObjects.add(temp);
        }
        adapter = new PhotoAdapter(this,photoObjects);
        contentGridView.setAdapter(adapter);

        findViewById(R.id.iv_btn_select_photos_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, null);
                LocalPicturesActivity.this.finish();
            }
        });

        findViewById(R.id.tv_btn_select_photos_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<LocalPhotoObject> photoObjectList = new ArrayList<>();
                for (ItemObject itemObject : photoObjects) {
                    if (itemObject.isSelected()) {
                        LocalPhotoObject fileObject = itemObject.getPhotoObject();
                        photoObjectList.add(fileObject);
                    }
                }


                if (photoObjectList.size() == 0) {
                    setResult(RESULT_CANCELED, null);
                } else {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject object;
                    for (LocalPhotoObject item : photoObjectList) {
                        object = new JSONObject();
                        try {
                            object.put("file_name", item.getName());
                            object.put("file_path", item.getPath());
                            object.put("file_size", item.getSize());
                            object.put("file_type", FileType.PICTURE.ordinal());
                            object.put("file_id", item.getId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArray.put(object);
                    }

                    Intent data = new Intent();
                    data.putExtra("data", jsonArray.toString());
                    setResult(RESULT_OK, data);
                    LocalPicturesActivity.this.finish();
                }
            }

        });

        contentGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemObject item = photoObjects.get(position);
                boolean newState = !item.isSelected;
                if (newState) {
                    if (curSize == 5) {
                        return;
                    } else {
                        curSize++;
                    }
                } else {
                    curSize--;
                }
                item.setIsSelected(newState);
                adapter.notifyDataSetChanged();
            }
        });

        getSupportLoaderManager().initLoader(0, null, LocalPicturesActivity.this);
    }

    private int curSize = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_PHOTO_ACTION && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String imgPath = FileUtils.getInstance().getPath(this, uri);
            File file = new File(imgPath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgPath, options);
            LocalPhotoObject temp = new LocalPhotoObject();
            temp.setName(file.getName());
            temp.setPath(file.getPath());
            temp.setSize(file.length());
            temp.setWidth(options.outWidth);
            temp.setHeight(options.outHeight);
            temp.setModifiedTime(new Date().getTime());
            temp.save();
            ItemObject newObject = new ItemObject(temp);
            newObject.setIsSelected(true);
            photoObjects.add(0, newObject);
            adapter.notifyDataSetChanged();
        }
    }

    private class ItemObject {
        private LocalPhotoObject photoObject;

        private boolean isSelected;

        public ItemObject(LocalPhotoObject photoObject) {
            this.photoObject = photoObject;
            isSelected = false;
        }

        public LocalPhotoObject getPhotoObject() {
            return photoObject;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setIsSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }
    }

    private class PhotoAdapter extends BaseAdapter {

        private List<ItemObject> photos;

        private Context context;

        public PhotoAdapter(Context context,List<ItemObject> photos) {
            this.context = context;
            this.photos = photos;
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public ItemObject getItem(int position) {
            return photos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        private class ViewHolder {
            public ImageView photoImageView;

            public ImageView isSelectedImageView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemObject photoObject = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_photo_view,null);
                viewHolder = new ViewHolder();
                viewHolder.photoImageView = (ImageView) convertView.findViewById(R.id.tv_photos_content);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(itemWidth,itemWidth);
                viewHolder.photoImageView.setLayoutParams(layoutParams);
                viewHolder.isSelectedImageView = (ImageView) convertView.findViewById(R.id.iv_photos_selected);
                layoutParams = new FrameLayout.LayoutParams(itemWidth,itemWidth);
                viewHolder.isSelectedImageView.setLayoutParams(layoutParams);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.photoImageView.setImageResource(R.drawable.default_ic_pic);
            LocalPhotoObject object = photoObject.getPhotoObject();
            if (!new File(object.getPath()).exists()) {
                DataSupport.delete(LocalPhotoObject.class,object.getId());
            }
            BitmapUtils.INSTANCE.loadLocalThumbnail(viewHolder.photoImageView, object.getPath());

            if (photoObject.isSelected()) {
                viewHolder.isSelectedImageView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.isSelectedImageView.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.e("TAG","onCreateLoader");

        SharedPreferences sharedPreferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
        Long lastUpdateTime = sharedPreferences.getLong("last_update_local_pictures",0);
        if (lastUpdateTime == 0) {
            lastUpdateTime = calendar.getTimeInMillis();
        }
        return new CursorLoader(
                this,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                STORE_IMAGES,
                "date_modified>?",
                new String[]{String.valueOf(lastUpdateTime / 1000)},
                "date_modified desc");
    }

    private class DataObject {
        private int id;

        private long size;

        private long dateModefied;

        private String displayName;

        private int width;

        private int height;

        public DataObject(int id, long size, long dateModefied, String displayName,int width,int height) {
            this.id = id;
            this.size = size;
            this.dateModefied = dateModefied;
            this.displayName = displayName;
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getId() {
            return id;
        }

        public long getSize() {
            return size;
        }

        public long getDateModefied() {
            return dateModefied;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private class ShowPictureTask extends AsyncTask<Cursor,ItemObject,Void> {

        @Override
        protected Void doInBackground(Cursor... params) {
            Log.e("TAG","doInBackground");
            Cursor data = params[0];
            if (data != null) {
                int curSize = 0;
                while (!data.isClosed() && data.moveToNext() && curSize <= 30) {
                    DataObject temp = new DataObject(data.getInt(0),data.getLong(2),data.getLong(1),data.getString(3),
                            data.getInt(4),data.getInt(5));
                    if (FileUtils.getInstance().getFileType(temp.getDisplayName()) != FileType.PICTURE) {
                        continue;
                    }

                    List<LocalPhotoObject> objects = DataSupport.where("name = ? and size = ?",
                            temp.getDisplayName(),String.valueOf(temp.getSize())).
                            find(LocalPhotoObject.class);
                    if (objects.size() == 0) {
                        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().
                                appendPath(Long.toString(temp.getId())).build();
                        String path = FileUtils.getInstance().getPath(LocalPicturesActivity.this, uri);
                        if (new File(path).exists()) {
                            LocalPhotoObject newLocalObject = new LocalPhotoObject();
                            newLocalObject.setName(temp.getDisplayName());
                            newLocalObject.setPath(path);
                            newLocalObject.setSize(temp.getSize());
                            newLocalObject.setHeight(temp.getHeight());
                            newLocalObject.setWidth(temp.getWidth());
                            newLocalObject.setModifiedTime(temp.getDateModefied());
                            newLocalObject.save();

                            publishProgress(new ItemObject(newLocalObject));
                            curSize++;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(ItemObject... values) {
            ItemObject object = values[0];
            photoObjects.add(object);
            adapter.notifyDataSetChanged();
        }

//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            if (photoObjects.size() == 0) {
//                List<LocalPhotoObject> photos = DataSupport.where("modifiedTime > ?",String.valueOf(calendar.getTimeInMillis() / 1000)).
//                        order("modifiedTime desc").limit(100).find(LocalPhotoObject.class);
//                for (LocalPhotoObject object : photos) {
//                    ItemObject temp = new ItemObject(object);
//                    photoObjects.add(temp);
//                }
//            } else {
//                photoObjects.addAll(newPhotoObject);
//            }
//            adapter.notifyDataSetChanged();
//        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.e("TAG", "onLoadFinished");
        new ShowPictureTask().execute(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.e("TAG","onLoaderReset");
        SharedPreferences sharedPreferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("last_update_local_pictures",new Date().getTime());
        editor.apply();
        BitmapUtils.INSTANCE.fluchCache();
    }
}
