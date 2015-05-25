package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.networkmodel.ICMAlbum;
import com.flaremars.classmanagers.model.AlbumsObject;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.model.IAlbum;
import com.flaremars.classmanagers.model.PictureObject;
import com.flaremars.classmanagers.utils.DisplayUtils;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.litepal.crud.DataSupport;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AlbumsFragment extends BaseFragment {
    private static final String TARGET_CLASS = "param1";

//    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA);

    private String targetClass;

    private ClassObject classObject;

    private List<IAlbum> items;

    private MyAdapter adapter;

    private static int itemWidth = 0;

    private static int offset = 12;

    public static AlbumsFragment newInstance(String classID) {

        AlbumsFragment fragment = new AlbumsFragment();
        Bundle args = new Bundle();
        args.putString(TARGET_CLASS, classID);
        fragment.setArguments(args);
        return fragment;
    }

    public AlbumsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (itemWidth == 0) {
            int screenWidth = DisplayUtils.INSTANCE.getSystemInfo(getContainerActivity()).getScreenWidth();
//            int paddingWidth = DisplayUtils.INSTANCE.dp2px(getContainerActivity(), 15);
//            int horizontalSpacing = paddingWidth - paddingWidth / 3;
            offset = DisplayUtils.INSTANCE.dp2px(getContainerActivity(), offset) / 3;
            int space = DisplayUtils.INSTANCE.dp2px(getContainerActivity(), 44);
            itemWidth = (screenWidth - space) / 2;
        }

        if (getArguments() != null) {
            targetClass = getArguments().getString(TARGET_CLASS);
            classObject = DataSupport.where("classID=?",targetClass).find(ClassObject.class).get(0);
        }
    }

    @Override
    void backAction() {
        Intent intent = new Intent(getContainerActivity(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    String fragmentTitle() {
        return "相册";
    }

    @Override
    List<Integer> setActionViews() {
        List<Integer> action = new ArrayList<>();
        action.add(R.id.main_container_create_album);
        return action;
    }

    private class MyAdapter extends BaseAdapter {

        private Context context;

        private List<IAlbum> items;

        private MyAdapter(Context context, List<IAlbum> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public IAlbum getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            public TextView nameTextView;

            public TextView baseInfoTextView;

            public ImageView backgroundImageView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            IAlbum item = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_album,null);
                View outer = convertView.findViewById(R.id.album_outer);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(itemWidth + offset,itemWidth + offset);
                outer.setLayoutParams(layoutParams);
                viewHolder = new ViewHolder();
                viewHolder.backgroundImageView = (ImageView) convertView.findViewById(R.id.iv_item_album_background);

                layoutParams = new FrameLayout.LayoutParams(itemWidth,itemWidth);
                viewHolder.backgroundImageView.setLayoutParams(layoutParams);
                viewHolder.baseInfoTextView = (TextView) convertView.findViewById(R.id.tv_item_album_info);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.tv_item_album_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.nameTextView.setText(item.getName());
            viewHolder.baseInfoTextView.setText(item.getCreateTime().split(" ")[0] + //DATE_FORMAT.format(item.getCreateTime()) +
                    "   " + item.getSizeOfPhotos() + "张");

            if (item.getFirstPhotoThumbnail().equals("")) {
                viewHolder.backgroundImageView.setImageResource(R.drawable.default_album_background);
            } else {

//                PictureObject img = DataSupport.where("pictureId=?",item.getFirstPhotoThumbnail()).find(PictureObject.class).get(0);
                FlareBitmapUtils.INSTANCE.loadBitmap(viewHolder.backgroundImageView,item.getFirstPhotoThumbnail());
//                BitmapUls.INSTANCE.loadBitmap(viewHolder.backgroundImageView,item.getFirstPhotoThumbnail());
            }
            return convertView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.gv_fragment_albums_content);

        List<AlbumsObject> albumsObjects = DataSupport.order("id desc").where("classobject_id=?",String.valueOf(classObject.getId())).
                find(AlbumsObject.class);

        items = new ArrayList<>();
        items.addAll(albumsObjects);
        adapter = new MyAdapter(getContainerActivity(), items);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IAlbum temp = items.get(position);
                Intent intent = new Intent(getActivity(),ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_PHOTOS);
                bundle.putString("target_album_id", temp.getAlbumId());
                bundle.putInt("targetClass",classObject.getId());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        return view;
    }

    private class LoadAlbumsTask extends AsyncTask<String,ICMAlbum,Void> {

        @Override
        protected Void doInBackground(String... params) {
            final String targetClass = params[0];
            AVQuery<AVObject> classQuery = new AVQuery<>("CMClassObject");
            classQuery.getInBackground(targetClass, new GetCallback<AVObject>() {
                @Override
                public void done(AVObject avObject, AVException e) {
                    if (e == null) {
                        AVQuery<ICMAlbum> query = ICMAlbum.getQuery(ICMAlbum.class);
                        query.whereEqualTo("inClass", avObject);
                        query.findInBackground(new FindCallback<ICMAlbum>() {
                            public void done(List<ICMAlbum> list, AVException e) {
                                if (e == null) {
                                    if (list.size() > 0) {
                                        for (final ICMAlbum temp : list) {
                                            List<AlbumsObject> albumsObjects = DataSupport.where("networkId=?",temp.getObjectId()).
                                                    find(AlbumsObject.class);
                                            if (albumsObjects.size() == 0) {
                                                AlbumsObject albumsObject = new AlbumsObject();
                                                albumsObject.setInClass(classObject);
                                                albumsObject.setCreateTime(temp.getCreateTime());
                                                albumsObject.setFirstPhotoThumbnail(temp.getFirstPhotoThumbnail());
                                                albumsObject.setName(temp.getName());
                                                albumsObject.setSizeOfPhotos(temp.getSizeOfPhotos());
                                                albumsObject.setRealPhotoFromList(temp.getRealPhotoIds());
                                                albumsObject.setNetworkId(temp.getObjectId());
                                                albumsObject.saveThrows();
                                            } else {
                                                AlbumsObject albumsObject = albumsObjects.get(0);
                                                if (!albumsObject.getFirstPhotoThumbnail().equals(temp.getFirstPhotoThumbnail()) ||
                                                        albumsObject.getSizeOfPhotos() != temp.getSizeOfPhotos()) {
                                                    albumsObject.setFirstPhotoThumbnail(temp.getFirstPhotoThumbnail());
                                                    albumsObject.setSizeOfPhotos(temp.getSizeOfPhotos());
                                                    albumsObject.setRealPhotoFromList(temp.getRealPhotoIds());
                                                    albumsObject.update(albumsObject.getId());
                                                }
                                            }
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    List<String> photoIds = temp.getRealPhotoIds();
                                                    if (photoIds == null) {
                                                        photoIds = new ArrayList<>();
                                                    }
                                                    //更新图片数据
                                                    for (String id : photoIds) {
                                                        try {
                                                            List<PictureObject> check = DataSupport.where("pictureId=?",id).find(PictureObject.class);
                                                            if (check.size() == 0) {
                                                                AVFile photoFile = AVFile.withObjectId(id);
                                                                PictureObject pictureObject = new PictureObject();
                                                                pictureObject.setTime(new Date().getTime());
                                                                pictureObject.setHeight((Integer) photoFile.getMetaData("height"));
                                                                pictureObject.setWidth((Integer) photoFile.getMetaData("width"));
                                                                pictureObject.setName(photoFile.getName());
                                                                pictureObject.setPictureId(photoFile.getObjectId());
                                                                pictureObject.setType(1);
                                                                pictureObject.setThumbnail(true);
                                                                pictureObject.setOwner(photoFile.getOwnerObjectId());
                                                                pictureObject.save();
                                                            }
                                                        } catch (AVException e1) {
                                                            e1.printStackTrace();
                                                        } catch (FileNotFoundException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }).start();
                                        }
                                        publishProgress(list.toArray(new ICMAlbum[list.size()]));
                                    }
                                }
                            }
                        });
                    } else {
                        NormalUtils.INSTANCE.showError(getContainerActivity(),e);
                    }
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(ICMAlbum... values) {
            items.clear();
            Collections.addAll(items, values);
            Collections.reverse(items);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        new LoadAlbumsTask().execute(targetClass);
    }
}
