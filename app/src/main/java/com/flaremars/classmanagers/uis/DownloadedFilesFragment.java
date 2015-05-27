package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.utils.BitmapUtils;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DownloadedFilesFragment extends BaseFragment {

    /*
        DOCUMENT,      //doc,excel
    PICTURE,  //jpg,png
    VIDEO,      //zip,rar
    VOICE,    //mp4,rmvb
    COMPRESSED,    //mp3
    APPLICATION,//apk
    SOURCE_FILE,   //.c .cpp .java
    OTHERS;
     */

    private static final String[] TYPES = {"文档","图片","视频","音像","压缩文件","应用","程序文件","其他"};

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 hh:mm", Locale.CHINA);

    public DownloadedFilesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloaded_files, container, false);
        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.elv_fragment_downloaded_files_content);

        final List<GroupItem> groups = new ArrayList<>();

        for (int i = 0;i < 8;i++) {
            List<FileObject> files = DataSupport.where("type=? and path is not null and path <> ''",i+"").order("updateTime desc").find(FileObject.class);
            if (files == null || files.size() == 0) {
                continue;
            }
            groups.add(new GroupItem(TYPES[i],files));
        }
        MyAdapter adapter = new MyAdapter(getContainerActivity(),groups);
        listView.setAdapter(adapter);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                FileObject target = groups.get(groupPosition).getFiles().get(childPosition);
                FileUtils.getInstance().openFile(new File(target.getPath()),getContainerActivity());
                return false;
            }
        });
        return view;
    }

    private class GroupItem {
        private String name;

        private List<FileObject> files;

        private GroupItem(String name, List<FileObject> files) {
            this.name = name;
            this.files = files;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<FileObject> getFiles() {
            return files;
        }

        public void setFiles(List<FileObject> files) {
            this.files = files;
        }
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        private Context context;

        private List<GroupItem> items;

        private MyAdapter(Context context, List<GroupItem> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return getGroup(groupPosition).getFiles().size();
        }

        @Override
        public GroupItem getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public FileObject getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).getFiles().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getChild(groupPosition,childPosition).getId();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        private class GroupViewHolder {
            public TextView nameTextView;
        }

        private class ChildViewHolder {

            public ImageView picImageImageView;

            public TextView nameTextView;

            public TextView sizeTextView;

            public TextView baseInfoTextView;
        }
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupItem item = getGroup(groupPosition);
            GroupViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new GroupViewHolder();
                convertView = View.inflate(context,R.layout.item_downloaded_file_category,null);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_downloaded_file_category_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) convertView.getTag();
            }

            viewHolder.nameTextView.setText(item.getName());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            FileObject item = getChild(groupPosition,childPosition);
            ChildViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_downloaded_file,null);
                viewHolder = new ChildViewHolder();
                viewHolder.baseInfoTextView = (TextView) convertView.findViewById(R.id.item_downloaded_file_base_info);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_downloaded_file_name);
                viewHolder.sizeTextView = (TextView) convertView.findViewById(R.id.item_downloaded_file_size);
                viewHolder.picImageImageView = (ImageView) convertView.findViewById(R.id.item_downloaded_file_content_img);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChildViewHolder) convertView.getTag();
            }

            Bitmap bitmap = null;
            boolean isPic = false;
            switch (FileType.valueOfInt(item.getType())) {
                case DOCUMENT:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_doc);
                    break;
                case PICTURE:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_ic_pic);
                    isPic = true;
                    break;
                case COMPRESSED:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_zip);
                    break;
                case VIDEO:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_video);
                    break;
                case VOICE:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_pic_voice);
                    break;
                case APPLICATION:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_pic_app);
                    break;
                case SOURCE_FILE:
                default:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_others);
                    break;
            }
            viewHolder.picImageImageView.setImageBitmap(bitmap);


            if (isPic){
                BitmapUtils.INSTANCE.loadLocalThumbnail(viewHolder.picImageImageView,item.getPath());
            }

            viewHolder.baseInfoTextView.setText(item.getSource() + " " +
                    DATE_FORMAT.format(item.getUpdateTime()));
            viewHolder.nameTextView.setText(item.getName());
            viewHolder.sizeTextView.setText(NormalUtils.INSTANCE.sizeToString(item.getSize()));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    @Override
    void backAction() {
        Intent intent = new Intent(getContainerActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    String fragmentTitle() {
        return "已下载文件夹";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }
}
