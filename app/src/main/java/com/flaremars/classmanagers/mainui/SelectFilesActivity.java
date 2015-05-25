package com.flaremars.classmanagers.mainui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.model.LocalFileObject;
import com.flaremars.classmanagers.utils.BitmapUtils;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SelectFilesActivity extends FragmentActivity {

    private static final SimpleDateFormat DATE_FAMATOR = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);

//    private static final String [] HEADERS = {"文档","图片","其他"};

    public static final int ACTION_SELECT_FILES = 3;

    public static final int ACTION_SELECT_PHOTO = 5;

    private static final int FILE_SELECT_CODE = 2;

//    private List<GroupItem> datas;

//    private List<FileObject> selectedFiles;

    private List<LocalFileObject> selectedFiles;

    private List<GroupObject> groups;

    private MyAdapter adapter;

    private ExpandableListView expandableListView;

    public static final long LIMIT_FILE_SIZE = 5 * 1024 * 1024;

    public static final long TIME_FOR_ONE_DAY = 24 * 60 * 60 * 1000;

    public static final long TIME_FOR_THREE_DAY = 3 * TIME_FOR_ONE_DAY;

    public static final long TIME_FOR_ONE_WEEK = 7 * TIME_FOR_ONE_DAY;

//    private FILE_SELECT_MODE mode;

    private int curSize = 0;

    public enum FILE_SELECT_MODE {
        ALL_KIND,
        ONLY_PICTURE,
        ONLY_DOCUMENT,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_files);

        selectedFiles = new ArrayList<>();
        expandableListView = (ExpandableListView) findViewById(R.id.elv_select_files_content);

        groups = new ArrayList<>();
        adapter = new MyAdapter(this,groups);
        expandableListView.setAdapter(adapter);
//        loadFiles();
        new LoadFilesTask().execute();

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                ChildObject item = groups.get(groupPosition).getChildren().get(childPosition);
                boolean targetState = !item.isSelected;
                if (targetState) {
                    curSize++;
                    if (curSize >= 4) {
                        NormalUtils.INSTANCE.showToast(SelectFilesActivity.this,"最多同时上传三个文件");
                        curSize--;
                    } else {
                        item.setIsSelected(!item.isSelected());
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    curSize--;
                    item.setIsSelected(!item.isSelected());
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        });

        findViewById(R.id.iv_btn_select_files_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED,null);
                SelectFilesActivity.this.finish();
            }
        });

        findViewById(R.id.tv_btn_select_files_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (GroupObject groupItem : groups) {
                    List<ChildObject> childItems = groupItem.getChildren();
                    for (ChildObject childItem : childItems) {
                        if (childItem.isSelected()) {
                            LocalFileObject fileObject = childItem.getFileObject();
                            selectedFiles.add(fileObject);
                        }
                    }
                }

                if (selectedFiles.size() == 0) {
                    setResult(RESULT_CANCELED,null);
                } else {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject object;
                    for (LocalFileObject item : selectedFiles) {
                        object = new JSONObject();
                        try {
                            object.put("file_name",item.getFileName());
                            object.put("file_path",item.getFilePath());
                            object.put("file_size",item.getSize());
                            object.put("file_type",item.getType());
                            object.put("file_id",item.getId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArray.put(object);
                    }

                    Intent data = new Intent();
                    data.putExtra("data",jsonArray.toString());
                    setResult(RESULT_OK,data);
                }
                SelectFilesActivity.this.finish();
            }
        });

        findViewById(R.id.btn_select_files_other_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "请选择一个文件"), FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SelectFilesActivity.this, "居然没有文件选择器~请先安装一个吧~", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class GroupObject {
        private String time;

        private List<ChildObject> children = new ArrayList<>();

        public List<ChildObject> getChildren() {
            return children;
        }

        public void setChildren(List<ChildObject> children) {
            this.children = children;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        private GroupObject(String time) {
            this.time = time;
        }
    }

    private class ChildObject {
        private LocalFileObject fileObject;

        private boolean isSelected;

        public ChildObject(LocalFileObject fileObject) {
            this.fileObject = fileObject;
            isSelected = false;
        }

        public LocalFileObject getFileObject() {
            return fileObject;
        }

        public void setFileObject(LocalFileObject fileObject) {
            this.fileObject = fileObject;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setIsSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }
    }

    private class LoadFilesTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            GroupObject groupObjectInDAY = new GroupObject("今天");
            GroupObject groupObjectInTHREE = new GroupObject("三天内");
            GroupObject groupObjectInWEEK = new GroupObject("一周内");
            GroupObject groupObjectOLD= new GroupObject("较早前");

            today = Calendar.getInstance(Locale.CHINA);
            today.set(Calendar.HOUR_OF_DAY,23);
            today.set(Calendar.MINUTE,59);

            //获取100个文件容量
            List<LocalFileObject> fileObjects = DataSupport.limit(100).order("time desc").find(LocalFileObject.class);
            for (LocalFileObject fileObject : fileObjects) {
                long time = today.getTime().getTime() - fileObject.getTime();
                if (time <= TIME_FOR_ONE_DAY) {
                    groupObjectInDAY.getChildren().add(new ChildObject(fileObject));
                } else if (time <= TIME_FOR_THREE_DAY) {
                    groupObjectInTHREE.getChildren().add(new ChildObject(fileObject));
                } else if (time <= TIME_FOR_ONE_WEEK){
                    groupObjectInWEEK.getChildren().add(new ChildObject(fileObject));
                } else {
                    groupObjectOLD.getChildren().add(new ChildObject(fileObject));
                }
            }

            if (groupObjectInDAY.getChildren().size() > 0) {
                groups.add(groupObjectInDAY);
            }
            if (groupObjectInTHREE.getChildren().size() > 0) {
                groups.add(groupObjectInTHREE);
            }
            if (groupObjectInWEEK.getChildren().size() > 0) {
                groups.add(groupObjectInWEEK);
            }
            if (groupObjectOLD.getChildren().size() > 0) {
                groups.add(groupObjectOLD);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            if (groups.size() > 0) {
                expandableListView.expandGroup(0);
            }
        }
    }

    private Calendar today;
    //首先加载今天的
    private void loadFiles() {

        GroupObject groupObjectInDAY = new GroupObject("今天");

        today = Calendar.getInstance(Locale.CHINA);

        //获取100个文件容量
        List<LocalFileObject> fileObjects = DataSupport.where("time > ?",String.valueOf(today.getTime().getTime() - TIME_FOR_ONE_DAY))
                .order("time desc").find(LocalFileObject.class);

        for (LocalFileObject fileObject : fileObjects) {
            groupObjectInDAY.getChildren().add(new ChildObject(fileObject));
        }

        adapter.notifyDataSetChanged();
        if (groups.size() > 0) {
            expandableListView.expandGroup(0);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = FileUtils.getInstance().getPath(this, uri);
                    String name = path.substring(path.lastIndexOf(File.separator) + 1);
                    LocalFileObject object = new LocalFileObject();
                    File file = new File(path);
                    if (file.length() > LIMIT_FILE_SIZE) {
                        NormalUtils.INSTANCE.showToast(SelectFilesActivity.this,"不能上传超过5M的文件");
                        return;
                    }

                    object.setSize(file.length());
                    object.setTime(new Date().getTime());
                    object.setFileName(name);
                    object.setFilePath(path);
                    object.setSource("本机");

                    ChildObject childItem = new ChildObject(object);
                    FileType type = FileUtils.getInstance().getFileType(name);

//                    Log.e("tag-SelectFiles",name + " " + file.length());

                    object.setType(type.ordinal());
                    object.save();

                    childItem.setIsSelected(true);
                    if (groups.size() == 0) {
                        GroupObject groupObjectInDAY = new GroupObject("今天");
                        groups.add(groupObjectInDAY);
                    }
                    groups.get(0).getChildren().add(0,childItem);
                    expandableListView.expandGroup(0);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this,name + " 添加成功~",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        private Context context;

        private List<GroupObject> items;

        private MyAdapter(Context context, List<GroupObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return getGroup(groupPosition).getChildren().size();
        }

        @Override
        public GroupObject getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        @Override
        public ChildObject getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).getChildren().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getChild(groupPosition,childPosition).getFileObject().getId();
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

            public ImageView selectedImageView;
        }
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupObject item = getGroup(groupPosition);
            GroupViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new GroupViewHolder();
                convertView = View.inflate(context,R.layout.item_downloaded_file_category,null);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_downloaded_file_category_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) convertView.getTag();
            }

            viewHolder.nameTextView.setText(item.getTime());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildObject item = getChild(groupPosition, childPosition);
            ChildViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_upload_file,null);
                viewHolder = new ChildViewHolder();
                viewHolder.baseInfoTextView = (TextView) convertView.findViewById(R.id.item_upload_file_base_info);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_upload_file_name);
                viewHolder.sizeTextView = (TextView) convertView.findViewById(R.id.item_upload_file_size);
                viewHolder.picImageImageView = (ImageView) convertView.findViewById(R.id.item_upload_file_content_img);
                viewHolder.selectedImageView = (ImageView) convertView.findViewById(R.id.iv_item_upload_file_select);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChildViewHolder) convertView.getTag();
            }

            Bitmap bitmap;
            switch (FileType.valueOfInt(item.getFileObject().getType())) {
                case DOCUMENT:
                    bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_doc);
                    break;
                case PICTURE:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_ic_pic);
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
            if (item.getFileObject().getType() == FileType.PICTURE.ordinal()){
                BitmapUtils.INSTANCE.loadLocalThumbnail(viewHolder.picImageImageView,item.getFileObject().getFilePath());
            }

            Date time = new Date(item.getFileObject().getTime());

            viewHolder.baseInfoTextView.setText(item.getFileObject().getSource() + " " +
                    DATE_FAMATOR.format(time));
            viewHolder.nameTextView.setText(item.getFileObject().getFileName());
            viewHolder.sizeTextView.setText(sizeToString(item.getFileObject().getSize()));

            viewHolder.selectedImageView.setImageResource(item.isSelected ? R.drawable.ic_selected_yes : R.drawable.ic_selected_no);
            return convertView;
        }

        private String sizeToString(long size) {
            int numForM = 0;
            int numForK = 0;

            numForM = (int)(size / (1024 * 1024));
            numForK = (int)(size / 1024);

            if (numForM > 0) {
                return numForM + "MB";
            } else {
                return numForK + "KB";
            }
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
