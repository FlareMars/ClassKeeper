package com.flaremars.classmanagers.uis;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.GetFileCallback;
import com.avos.avoscloud.ProgressCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.mainui.SelectFilesActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.model.FileType;
import com.flaremars.classmanagers.model.ManagerObject;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.ContextMenuDialog;
import com.flaremars.classmanagers.views.ContextMenuDialogForFile;
import com.flaremars.classmanagers.views.ProgressBarDeterminate;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FilesFragment extends BaseFragment {

    private static final SimpleDateFormat DATE_FAMATOR = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);

    private String targetClass;

    private ClassObject classObject;

    public static FilesFragment newInstance(String classID) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putString(AppConst.CLASS_ID, classID);
        fragment.setArguments(args);
        return fragment;
    }

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetClass = getArguments().getString(AppConst.CLASS_ID);
            classObject = DataSupport.where("classID=?",targetClass).find(ClassObject.class).get(0);
        }
    }

    @Override
    void backAction() {
        if (!getContainerActivity().getSupportFragmentManager().popBackStackImmediate()) {
            Intent intent = new Intent(getContainerActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    String fragmentTitle() {
        return "文件";
    }

    @Override
    List<Integer> setActionViews() {
        List<Integer> views = new ArrayList<>();
        views.add(R.id.main_container_upload_file);
        return views;
    }

    private class GroupObject {
        private String time;

        private List<FileObject> children = new ArrayList<>();

        public List<FileObject> getChildren() {
            return children;
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

    private class LoadFilesTask extends AsyncTask<String,AVObject,Void> {

        private String targetClass;

        @Override
        protected Void doInBackground(String... params) {
            targetClass = params[0];
            SharedPreferences preferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,Context.MODE_PRIVATE);
            final Long lastUpdate = preferences.getLong("last_update_files" + targetClass, 0);

            AVQuery<AVObject> classQuery = new AVQuery<>("CMClassObject");
            classQuery.getInBackground(params[0], new GetCallback<AVObject>() {
                @Override
                public void done(AVObject avObject, AVException e) {
                    if (e == null) {
                        if (avObject != null) {
                            AVQuery<AVObject> filesQuery = new AVQuery<>("CMClassFile");
                            filesQuery.include("fileData");
                            filesQuery.whereEqualTo("inClass",avObject);
                            filesQuery.whereGreaterThanOrEqualTo("updatedAt", new Date(lastUpdate));
                            filesQuery.findInBackground(new FindCallback<AVObject>() {
                                @Override
                                public void done(List<AVObject> list, AVException e) {
                                    Log.e("TAG", list.size() + "");

                                    if (e == null) {
                                        publishProgress(list.toArray(new AVObject[list.size()]));
                                    } else {
                                        NormalUtils.INSTANCE.showError(getContainerActivity(),e);
                                    }
                                }
                            });
                        }
                    } else {
                        NormalUtils.INSTANCE.showError(getContainerActivity(), e);
                    }
                }
            });
            return null;
        }

        @Override
        protected void onProgressUpdate(AVObject... values) {
            SharedPreferences preferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("last_update_files" + targetClass, new Date().getTime());
            editor.apply();

            ClassObject targetClassObject = DataSupport.where("classID=?",targetClass).find(ClassObject.class).get(0);
            boolean hasNewFile = false;
            for (AVObject temp : values) {
                AVFile file = temp.getAVFile("fileData");
                List<FileObject> temps = DataSupport.where("name = ? and size = ?",file.getOriginalName(),String.valueOf(file.getSize())).find(FileObject.class);
                if (temps.size() == 0) {
                    hasNewFile = true;
                    FileObject newFile = new FileObject();
                    newFile.setInClass(targetClassObject);
                    newFile.setSource((String)file.getMetaData("source"));
                    newFile.setName(file.getOriginalName());
                    newFile.setUpdateTime(temp.getUpdatedAt());
                    newFile.setType((Integer)file.getMetaData("type"));
                    newFile.setSize(file.getSize());
                    newFile.setFileId(file.getObjectId());
                    newFile.setFileCode(temp.getObjectId());
                    newFile.setPath("");
                    newFile.save();
                } else {
                    FileObject updatedFile = temps.get(0);
                    updatedFile.setFileCode(temp.getObjectId());
                    updatedFile.update(updatedFile.getId());
                }
            }

            if (hasNewFile) {
                loadFiles();
            }
        }
    }

    private void loadFiles() {
        List<FileObject> fileObjects = DataSupport.where("classobject_id=?",String.valueOf(classObject.getId())).order("time desc").find(FileObject.class);
        if (groups.size() > 0) {
            groups.clear();
        }

        GroupObject groupObjectInDAY = new GroupObject("一天内");
        GroupObject groupObjectInWEEK = new GroupObject("一周内");
        GroupObject groupObjectOLD= new GroupObject("较早前");

        Calendar today = Calendar.getInstance(Locale.CHINA);
        today.set(Calendar.HOUR_OF_DAY,23);
        today.set(Calendar.MINUTE,59);

        for (FileObject fileObject : fileObjects) {
            long time = today.getTime().getTime() - fileObject.getUpdateTime().getTime();
            if (time <= SelectFilesActivity.TIME_FOR_ONE_DAY) {
                groupObjectInDAY.getChildren().add(fileObject);
            } else if (time <= SelectFilesActivity.TIME_FOR_ONE_WEEK){
                groupObjectInWEEK.getChildren().add(fileObject);
            } else {
                groupObjectOLD.getChildren().add(fileObject);
            }
        }

        if (groupObjectInDAY.getChildren().size() > 0) {
            Collections.reverse(groupObjectInDAY.getChildren());
            groups.add(groupObjectInDAY);
        }
        if (groupObjectInWEEK.getChildren().size() > 0) {
            Collections.reverse(groupObjectInWEEK.getChildren());
            groups.add(groupObjectInWEEK);
        }
        if (groupObjectOLD.getChildren().size() > 0) {
            Collections.reverse(groupObjectOLD.getChildren());
            groups.add(groupObjectOLD);
        }

        adapter.notifyDataSetChanged();
        if (groups.size() > 0) {
            listView.expandGroup(0);
        }
    }

    private ExpandableListView listView;

    private List<GroupObject> groups;

    private MyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        listView = (ExpandableListView) view.findViewById(R.id.fragment_files_content);

        groups = new ArrayList<>();
        adapter = new MyAdapter(getContainerActivity(),groups);
        listView.setAdapter(adapter);
        loadFiles();

        new LoadFilesTask().execute(targetClass);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                FileObject fileObject = groups.get(groupPosition).getChildren().get(childPosition);
                String path = fileObject.getPath();
                if (path != null && !path.equals("")) {
                    File target = new File(path);
                    if (!target.exists()) {
                        NormalUtils.INSTANCE.showToast(getContainerActivity(),"额额，文件君不见了，要不我们重新下载一个~");
                        fileObject.setPath("");
                        adapter.notifyDataSetChanged();
                    }
                    FileUtils.getInstance().openFile(new File(path), getContainerActivity());
                } else {
                    NormalUtils.INSTANCE.showToast(getContainerActivity(),"文件尚未下载，不能打开~");
                }
                return true;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int group1Size = groups.get(0).getChildren().size() + 1; //第二个分组的前面所有item
                int group2Size = 0;
                if (groups.size() > 1) {
                    group2Size =  groups.get(1).getChildren().size() + 1;//第三个分组的个数加上分割位
                }
                if (position != 0 && position != group1Size && position != group2Size) {
                    int groupIndex;
                    int index;
                    if (position - group1Size < 0) {
                        groupIndex = 0;
                        index = position - 1;
                    } else if (position - (group1Size + group2Size) < 0) {
                        groupIndex = 1;
                        index = position - group1Size - 1;
                    } else {
                        groupIndex = 2;
                        index = position - group1Size - group2Size - 1;
                    }
                    final FileObject item = groups.get(groupIndex).getChildren().get(index);

                    final int finalGroupIndex = groupIndex;
                    ContextMenuDialogForFile dialogForFile = ContextMenuDialogForFile.getDialog(new ContextMenuDialog.OnDeleteClickListener() {
                        @Override
                        public void onDeleteClick() {
                            if (!classObject.getCreator().equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
                                List<ManagerObject> managers = DataSupport.where("classobject_id=?",String.valueOf(classObject.getId())).find(ManagerObject.class);
                                if (managers.size() == 0) {
                                    if (!item.getSource().equals(MainActivity.BASE_GLOBAL_DATA.getUserRealName())) {
                                        //如果不是创建者、不是管理员也不是文件上传者，则不允许进行删除操作
                                        NormalUtils.INSTANCE.showToast(getContainerActivity(),"无权限操作，不过去搞个管理员就可以了哦~");
                                        return;
                                    }
                                }
                            }

                            DataSupport.delete(FileObject.class, item.getId());
                            groups.get(finalGroupIndex).getChildren().remove(item);
                            adapter.notifyDataSetChanged();

                            AVQuery<AVObject> query = new AVQuery<>("CMClassFile");
                            query.getInBackground(item.getFileCode(), new GetCallback<AVObject>() {
                                @Override
                                public void done(AVObject avObject, AVException e) {
                                    if (e == null) {
                                        avObject.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(AVException e) {
                                                if (e == null) {
                                                    NormalUtils.INSTANCE.showToast(getContainerActivity(),"成功删除文件["+
                                                            item.getName() +"]");
                                                } else {
                                                    NormalUtils.INSTANCE.showError(getContainerActivity(),e);
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    },item.getId());
                    dialogForFile.show(getContainerActivity().getSupportFragmentManager(),"tag");
                }

                return true;
            }
        });
        return view;
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        private List<GroupObject> groupObjects;

        private Context context;

        private MyAdapter(Context context,List<GroupObject> groupObjects) {
            this.groupObjects = groupObjects;
            this.context = context;
        }

        @Override
        public int getGroupCount() {
            return groupObjects.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return getGroup(groupPosition).getChildren().size();
        }

        @Override
        public GroupObject getGroup(int groupPosition) {
            return groupObjects.get(groupPosition);
        }

        @Override
        public FileObject getChild(int groupPosition, int childPosition) {
            return getGroup(groupPosition).getChildren().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition * 1000;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getGroupId(groupPosition) + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        private class GroupViewHolder {
            public TextView sectionText;
        }

        private class ChildViewHolder {
            public ImageView picImage;

            public TextView nameText;

            public TextView sizeText;

            public TextView baseInfoText;

            public ImageView downLoadImage;

            public ProgressBarDeterminate downloadProgressBar;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupObject item = getGroup(groupPosition);
            GroupViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_group_file_time,null);
                viewHolder = new GroupViewHolder();
                viewHolder.sectionText = (TextView) convertView.findViewById(R.id.item_group_file_section);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (GroupViewHolder) convertView.getTag();
            }

            viewHolder.sectionText.setText(item.getTime());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final FileObject itemObject = getChild(groupPosition, childPosition);
            final ChildViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_file_content,null);
                viewHolder = new ChildViewHolder();
                viewHolder.baseInfoText = (TextView) convertView.findViewById(R.id.item_file_base_info);
                viewHolder.downLoadImage = (ImageView) convertView.findViewById(R.id.item_file_download);
                viewHolder.nameText = (TextView) convertView.findViewById(R.id.item_file_name);
                viewHolder.sizeText = (TextView) convertView.findViewById(R.id.item_file_size);
                viewHolder.picImage = (ImageView) convertView.findViewById(R.id.item_file_content_img);
                viewHolder.downloadProgressBar = (ProgressBarDeterminate) convertView.findViewById(R.id.item_file_download_progress);
                viewHolder.downloadProgressBar.setMax(100);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChildViewHolder) convertView.getTag();
            }

            viewHolder.sizeText.setText(sizeToString(itemObject.getSize()));
            viewHolder.nameText.setText(itemObject.getName());
            viewHolder.baseInfoText.setText(itemObject.getSource() + " " +
                    DATE_FAMATOR.format(itemObject.getUpdateTime()));

            Bitmap bitmap;
            switch (FileType.valueOfInt(itemObject.getType())) {
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
            viewHolder.picImage.setImageBitmap(bitmap);

            viewHolder.downLoadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    viewHolder.downloadProgressBar.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    viewHolder.downloadProgressBar.setProgress(0);
                    viewHolder.downLoadImage.setVisibility(View.GONE);
                    AVFile.withObjectIdInBackground(itemObject.getFileId(), new GetFileCallback<AVFile>() {
                        @Override
                        public void done(AVFile avFile, AVException e) {
                            if (e == null) {
                                avFile.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, AVException e) {
                                        if (e == null) {
                                            FileObject fileObject = DataSupport.where("fileId=?", itemObject.getFileId()).find(FileObject.class).get(0);
                                            String newPath = FileUtils.getInstance().saveNormalFile(bytes, itemObject.getName());
                                            fileObject.setPath(newPath);
                                            itemObject.setPath(newPath);
                                            fileObject.update(fileObject.getId());

                                            FileUtils.getInstance().updateLocalFile(itemObject.getName(), newPath, "下载自 " + classObject.getName(),
                                                    itemObject.getSize(), itemObject.getType());

                                            Toast.makeText(getContainerActivity(), "下载成功~", Toast.LENGTH_LONG).show();
                                            FileUtils.getInstance().openFile(new File(newPath), getContainerActivity());

                                            viewHolder.downloadProgressBar.setVisibility(View.INVISIBLE);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            viewHolder.downloadProgressBar.setVisibility(View.INVISIBLE);
                                            NormalUtils.INSTANCE.showError(getContainerActivity(), e);
                                        }
                                    }
                                }, new ProgressCallback() {
                                    @Override
                                    public void done(Integer integer) {
                                        viewHolder.downloadProgressBar.setProgress(integer);
                                    }
                                });
                            } else {

                                ContentValues cv = new ContentValues();
                                cv.put("classobject_id", -1);
                                DataSupport.update(FileObject.class, cv, itemObject.getId());
                                NormalUtils.INSTANCE.showToast(getContainerActivity(), "下载失败，文件已被删除~");
                                NormalUtils.INSTANCE.showError(context, e);
                            }
                        }
                    });

                }
            });

            if (itemObject.getPath() == null || itemObject.getPath().equals("")) {
                viewHolder.downLoadImage.setVisibility(View.VISIBLE);
            } else {
                viewHolder.downLoadImage.setVisibility(View.GONE);
            }
            return convertView;
        }

        private String sizeToString(long size) {
            int numForM ;
            int numForK;

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
