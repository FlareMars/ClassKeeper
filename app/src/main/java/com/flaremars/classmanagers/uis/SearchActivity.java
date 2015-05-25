package com.flaremars.classmanagers.uis;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.GetFileCallback;
import com.avos.avoscloud.ProgressCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.ChattingActivity;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.FileObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.utils.FileUtils;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.Dialog;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SearchActivity extends FragmentActivity {

    private EditText searchInput;

    private MyAdapter adapter;

    //模拟数据库
    private List<ItemObject> tempDataBase;

    private List<ItemObject> data;

    //分割点记录 contactsStart contactsLimit groupStart groupLimit fileStart fileLimit
    private int[] limits = {0,0,0,0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        /*查找当前班级联系人，所加入的班级，文件，*/
        tempDataBase = new ArrayList<>();
        //首先将内容缓存起来，todo 可能这样内存消耗有点大

        //获取联系人
        final ClassObject curClass = DataSupport.where("classID=?", MainActivity.BASE_GLOBAL_DATA.getCurClassID()).
                find(ClassObject.class).get(0);
        if (!MainActivity.BASE_GLOBAL_DATA.getCurClassName().equals("")) {

            List<ClassMemberObject> tempList = curClass.getMemberList();
            List<UserObject> classMembers = new ArrayList<>();
            for (ClassMemberObject temp : tempList) {
                UserObject tempUserObject = DataSupport.where("userID=?",temp.getMemberID()).find(UserObject.class).get(0);
                classMembers.add(tempUserObject);
            }
            for (UserObject temp : classMembers) {
                ItemObject object = new ItemObject(new SpannableStringBuilder(temp.getUserRealName()),
                        ItemType.CONTACT,temp.getId());
                object.setImgPath(temp.getImgId());
                tempDataBase.add(object);
            }
            limits[0] = 0;
            limits[1] = classMembers.size();
        }

        //获取所有群组
        List<ClassObject> classObjectList = DataSupport.findAll(ClassObject.class);
        for (ClassObject temp : classObjectList) {
            tempDataBase.add(new ItemObject(new SpannableStringBuilder(temp.getName()),
                    ItemType.GROUP,temp.getId()));
        }
        limits[2] = limits[1];
        limits[3] = limits[2] + classObjectList.size();

        //获取文件列表
        List<FileObject> fileObjectList = DataSupport.where("classobject_id=?",String.valueOf(curClass.getId())).find(FileObject.class);
        for (FileObject temp : fileObjectList) {
            tempDataBase.add(new ItemObject(new SpannableStringBuilder(temp.getName()),
                    ItemType.DOCUMENT,temp.getId()));
        }
        limits[4] = limits[3];
        limits[5] = limits[4] + fileObjectList.size();

        searchInput = (EditText) findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                closeSoftInput();
                data.clear();
                if (!s.toString().isEmpty()) {
                    String target = s.toString();
                    for (int i = 0;i < 3;i++) {
                        boolean kindHasValue = false;
                        for (int j = limits[i*2];j < limits[i*2 + 1];j++) {
                            ItemObject item = tempDataBase.get(j);
                            Pattern p = Pattern.compile(target,Pattern.CASE_INSENSITIVE);
                            Matcher matcher = p.matcher(item.getName());

                            if (matcher.find()) {
                                matcher.reset();
                                SpannableStringBuilder temp = item.getName();
                                while (matcher.find()) {
                                    kindHasValue = true;
                                    SpannableStringBuilder style = new SpannableStringBuilder(temp);

                                    style.setSpan(new ForegroundColorSpan(Color.BLUE),matcher.start(),
                                            matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    temp = style;
                                }
                                data.add(new ItemObject(temp,ItemType.fromInt(i),item.getTargetID()));
                            }
                        }
                        if (kindHasValue) {
                            data.add(new ItemObject(new SpannableStringBuilder(""),ItemType.DIVIDER,-1));
                        }
                    }
                }
                int size = data.size();
                if (size > 0 && data.get(size -1).getType() == ItemType.DIVIDER) {
                    data.remove(size -1);
                }
                if (data.size() > 0) {
                    data.add(new ItemObject(new SpannableStringBuilder(""),ItemType.DIVIDER,-1));
                    data.add(0,new ItemObject(new SpannableStringBuilder(""),ItemType.DIVIDER,-1));
                }
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.search_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.search_clean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.setText("");
            }
        });

        ListView listView = (ListView) findViewById(R.id.search_content);
        data = new ArrayList<>();
        adapter = new MyAdapter(this,data);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemObject object = data.get(position);
                switch (object.getType()) {
                    case CONTACT:
                        Intent intent = new Intent(SearchActivity.this, ChattingActivity.class);
                        intent.putExtra("target",object.getTargetID());
                        startActivity(intent);
                        break;
                    case GROUP:
                        Intent intent2 = new Intent(SearchActivity.this, ChattingActivity.class);
                        intent2.putExtra("target",object.getTargetID());
                        intent2.putExtra("is_group",true);
                        startActivity(intent2);
                        break;
                    case DOCUMENT:
                        final FileObject fileObject = DataSupport.find(FileObject.class,object.getTargetID());
                        String path = fileObject.getPath();
                        if (path == null) {
                            path = "";
                        }
                        File targetFile = new File(path);
                        if (targetFile.exists()) {
                            FileUtils.getInstance().openFile(targetFile, SearchActivity.this);
                        } else {
                            String infoFormat = "文件名称 : %s \n 文件大小 : %s";
                            final Dialog dialog = new Dialog(SearchActivity.this,"文件尚未下载",
                                    String.format(infoFormat,fileObject.getName(),NormalUtils.INSTANCE.sizeToString(fileObject.getSize())));
                            dialog.addCancelButton("取消");
                            dialog.setButtonAcceptText("确认下载");
                            dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final DownloadProgressDialog downloadProgressDialog = DownloadProgressDialog.getDownloadDialog();
                                    downloadProgressDialog.show(getSupportFragmentManager(),"download_dialog");
                                    AVFile.withObjectIdInBackground(fileObject.getFileId(), new GetFileCallback<AVFile>() {
                                        @Override
                                        public void done(AVFile avFile, AVException e) {
                                            if (e == null) {
                                                avFile.getDataInBackground(new GetDataCallback() {
                                                    @Override
                                                    public void done(byte[] bytes, AVException e) {
                                                        if (e == null) {
                                                            String newPath = FileUtils.getInstance().saveNormalFile(bytes, fileObject.getName());
                                                            fileObject.setPath(newPath);
                                                            fileObject.update(fileObject.getId());

                                                            FileUtils.getInstance().updateLocalFile(fileObject.getName(), newPath, "下载自 " + curClass.getName(),
                                                                    fileObject.getSize(), fileObject.getType());

                                                            Toast.makeText(SearchActivity.this, "下载成功~", Toast.LENGTH_LONG).show();
                                                            FileUtils.getInstance().openFile(new File(newPath), SearchActivity.this);
                                                        } else {
                                                            NormalUtils.INSTANCE.showError(SearchActivity.this,e);
                                                        }
                                                    }
                                                }, new ProgressCallback() {
                                                    @Override
                                                    public void done(Integer integer) {
                                                        downloadProgressDialog.setProgress(integer);
                                                    }
                                                });
                                            } else {
                                                NormalUtils.INSTANCE.showError(SearchActivity.this,e);
                                            }
                                        }
                                    });
                                }
                            });
                            dialog.show();
                        }
                        break;
                    default:
                }
            }
        });
    }

    private enum ItemType {
        CONTACT,
        GROUP,
        DOCUMENT,
        DIVIDER;

        public static ItemType fromInt(int i) {
            switch (i) {
                case 0:
                    return CONTACT;
                case 1:
                    return GROUP;
                case 2:
                    return DOCUMENT;
                default:
                    return CONTACT;
            }
        }
    }

    private class ItemObject {

        private SpannableStringBuilder name;

        private String imgPath;

        private ItemType type;

        private int targetID;

        public ItemObject(SpannableStringBuilder name, ItemType type, int targetID) {
            this.name = name;
            this.type = type;
            this.targetID = targetID;
        }

        public String getImgPath() {
            return imgPath;
        }

        public void setImgPath(String imgPath) {
            this.imgPath = imgPath;
        }

        public int getTargetID() {
            return targetID;
        }

        public void setTargetID(int targetID) {
            this.targetID = targetID;
        }

        public ItemType getType() {
            return type;
        }

        public void setType(ItemType type) {
            this.type = type;
        }

        public SpannableStringBuilder getName() {
            return name;
        }

        public void setName(SpannableStringBuilder name) {
            this.name = name;
        }
    }

    private class MyAdapter extends BaseAdapter {

        private Context context;

        private List<ItemObject> items;

        //0为divider 1为普通

        private MyAdapter(Context context, List<ItemObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) == 1;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == getCount() - 1) {
                return 2;
            }
            return getItem(position).getType() == ItemType.DIVIDER ? 0 : 1;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ItemObject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            public TextView nameTextView;

            public ImageView headerImageView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            ItemObject item = getItem(position);
            int type = getItemViewType(position);

            if (convertView == null) {
                if (type == 1) {
                    convertView = View.inflate(context, R.layout.item_contacts, null);
                    viewHolder = new ViewHolder();
                    viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_contacts_name);
                    viewHolder.headerImageView = (ImageView) convertView.findViewById(R.id.item_contacts_img);
                    convertView.setTag(viewHolder);
                } else if (type == 0){
                    convertView = View.inflate(SearchActivity.this,R.layout.item_transparent_divider,null);
                } else {
                    convertView = View.inflate(SearchActivity.this,R.layout.item_only_line,null);
                }
            } else {
                if (type == 1) {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
            }
            if (type == 1) {
                viewHolder.nameTextView.setText(item.getName());
                Bitmap headerImg = null;
                switch (item.getType()) {
                    case CONTACT:
                        if (item.getImgPath() != null && !item.getImgPath().equals("")) {
                            headerImg = BitmapFactory.decodeFile(item.getImgPath());
                        } else {
                            headerImg = BitmapFactory.decodeResource(getResources(),R.drawable.default_ic_contact);
                        }
                        break;
                    case GROUP:
                        headerImg = BitmapFactory.decodeResource(getResources(),R.drawable.ic_search_group);
                        break;
                    case DOCUMENT:
                        headerImg = BitmapFactory.decodeResource(getResources(),R.drawable.ic_search_doc);
                        break;
                    default:
                        break;
                }
                viewHolder.headerImageView.setImageBitmap(headerImg);
            }
            return convertView;
        }
    }
}
