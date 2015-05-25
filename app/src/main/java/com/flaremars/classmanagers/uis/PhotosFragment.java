package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.AlbumsObject;
import com.flaremars.classmanagers.model.PictureObject;
import com.flaremars.classmanagers.utils.DisplayUtils;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class PhotosFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";

    private static final String ARG_PARAM2 = "param2";

    public static final int ACTION_UPLOAD_PHOTO = 6;

    private List<PhotoObject> photoObjectList;

    private AlbumsObject albumsObject;

    private String targetAlbumId;

    private PhotoAdapter adapter;

    private int targetClassId;

    private static int itemWidth = 0;

    public static PhotosFragment newInstance(String targetID,int targetClassId) {
        PhotosFragment fragment = new PhotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, targetID);
        args.putInt(ARG_PARAM2,targetClassId);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotosFragment() {
        // Required empty public constructor
    }

    private class PhotoObject {
        private String raelId;

        private String thumnailId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PhotoObject that = (PhotoObject) o;

            if (raelId != null ? !raelId.equals(that.raelId) : that.raelId != null) return false;
            return !(thumnailId != null ? !thumnailId.equals(that.thumnailId) : that.thumnailId != null);

        }

        @Override
        public int hashCode() {
            int result = raelId != null ? raelId.hashCode() : 0;
            result = 31 * result + (thumnailId != null ? thumnailId.hashCode() : 0);
            return result;
        }

        public PhotoObject(String raelId, String thumnailId) {
            this.raelId = raelId;
            this.thumnailId = thumnailId;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (itemWidth == 0) {
            int screenWidth = DisplayUtils.INSTANCE.getSystemInfo(getContainerActivity()).getScreenWidth();
            int blankFieldWidth = DisplayUtils.INSTANCE.dp2px(getContainerActivity(), 22);
            itemWidth = (screenWidth - blankFieldWidth) / 3;
        }

        if (getArguments() != null) {
            targetAlbumId = getArguments().getString(ARG_PARAM1);
            targetClassId = getArguments().getInt(ARG_PARAM2);
            List<AlbumsObject> albumsObjects = DataSupport.where("networkId=?", targetAlbumId).
                    find(AlbumsObject.class);
            albumsObject = albumsObjects.get(0);
        }
    }

    @Override
    void backAction() {
        getContainerActivity().finish();
    }

    @Override
    String fragmentTitle() {
        return "班级相册" + "-" + albumsObject.getName();
    }

    @Override
    List<Integer> setActionViews() {
        List<Integer> action = new ArrayList<>();
        action.add(R.id.main_container_upload_photos);
        return action;
    }

    private class PhotoAdapter extends BaseAdapter {

        private List<PhotoObject> photoObjects;

        private Context context;

        public PhotoAdapter(Context context,List<PhotoObject> photoObjects) {
            this.context = context;
            this.photoObjects = photoObjects;
        }

        @Override
        public int getCount() {
            return photoObjects.size();
        }

        @Override
        public PhotoObject getItem(int position) {
            return photoObjects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        private class ViewHolder {
            public ImageView photoImageView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PhotoObject photoObject = getItem(position);
            ViewHolder viewHolder;
            View view;

            if (convertView == null) {
                view = View.inflate(context,R.layout.item_photo_simple_view,null);
            } else {
                view = convertView;
            }
            final ImageView imageView = (ImageView) view.findViewById(R.id.tv_photos_content);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(itemWidth,itemWidth);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageResource(R.drawable.default_ic_pic);
//            BitmapUtils.INSTANCE.loadBitmapForGridView(photoObject.thumnailId,imageView);
            PictureObject img = DataSupport.where("pictureId=?",photoObject.raelId).find(PictureObject.class).get(0);
            FlareBitmapUtils.INSTANCE.loadBitmap(imageView,img);
            return view;
        }
    }

    private GridView gridView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        gridView = (GridView) view.findViewById(R.id.gv_fragment_photos_content);

        photoObjectList = new ArrayList<>();

        adapter = new PhotoAdapter(getContainerActivity(),photoObjectList);
        gridView.setAdapter(adapter);

//        new LoadPhotosTask().execute();
//        BitmapUtils.INSTANCE.initConcurrencyLoad(gridView);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContainerActivity(),BrowsePhotoActivity.class);
                intent.putExtra("targetPhoto",photoObjectList.get(position).raelId);
                intent.putExtra("targetClass", targetClassId);
                intent.putExtra("targetAlbum",targetAlbumId);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<AlbumsObject> albumsObjects = DataSupport.where("networkId=?", targetAlbumId).
                find(AlbumsObject.class);
        albumsObject = albumsObjects.get(0);
        String[] reals = albumsObject.getRealPhotoIdWithArray();
        if (photoObjectList.size() > 0) {
            photoObjectList.clear();
            adapter.notifyDataSetChanged();
        }
        for (String real : reals) {
            if (real.equals("")) {
                continue;
            }
            PhotoObject temp = new PhotoObject(real, "");
            photoObjectList.add(0,temp);
        }

        Log.e("TAG",photoObjectList.size() + "");
        gridView.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        }, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FlareBitmapUtils.INSTANCE.cancelAllTask();
    }
}
