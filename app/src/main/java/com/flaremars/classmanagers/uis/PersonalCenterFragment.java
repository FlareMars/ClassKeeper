package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.PictureObject;
import com.flaremars.classmanagers.model.UserPersonalInfo;
import com.flaremars.classmanagers.utils.BitmapUtils;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.utils.LocalDataBaseHelper;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.utils.UploadUtils;
import com.fourmob.datetimepicker.date.DatePickerDialog;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonalCenterFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener{

    private static int ACTION_LOAD_IMAGE = 8;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    private final static int[] dayArr = new int[] { 20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22 };
    private final static String[] constellationArr = new String[] { "摩羯座", "水瓶座", "双鱼座", "白羊座",
            "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };

    private EditText sexEditText;

    private TextView birthdayTextView;

    private EditText phoneEditText;

    private TextView ageTextView;

    private TextView constellationTextView;

    private ImageView headerImageView;

    private UserPersonalInfo userPersonalInfo;

    private AVObject cmUserInfo;

    public PersonalCenterFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        BitmapUtils.INSTANCE.initBitmapUtils(getContainerActivity());

        View view = inflater.inflate(R.layout.fragment_personal_center, container, false);
        sexEditText = (EditText) view.findViewById(R.id.fragment_personal_center_sex);
        birthdayTextView = (TextView) view.findViewById(R.id.fragment_personal_center_birthday);
        phoneEditText = (EditText) view.findViewById(R.id.fragment_personal_center_phone);
        headerImageView = (ImageView) view.findViewById(R.id.fragment_personal_center_img);
        ageTextView = (TextView) view.findViewById(R.id.fragment_personal_center_age);
        constellationTextView = (TextView) view.findViewById(R.id.fragment_personal_center_constellation);
        ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.fragment_personal_center_classes_list);
        TextView nameTextView = (TextView) view.findViewById(R.id.fragment_personal_center_name);
        TextView idTextView = (TextView) view.findViewById(R.id.fragment_personal_center_id);
        TextView baseInfoTextView = (TextView) view.findViewById(R.id.fragment_personal_center_base_info);
        TextView classesTextView = (TextView) view.findViewById(R.id.fragment_personal_center_classes);

        SharedPreferences preferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,Context.MODE_PRIVATE);
        String imgId = preferences.getString(AppConst.USER_HEADER_ID, "");
        String realName = preferences.getString(AppConst.USER_REAL_NAME,"");
        String userId = preferences.getString(AppConst.USER_ID,"");
        String phoneNum = preferences.getString(AppConst.USER_NAME,"");

        //预先获取网络个人信息对象
        AVQuery<AVObject> query = new AVQuery<>("CMUserInfo");
        query.whereEqualTo("userId", userId);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        cmUserInfo = list.get(0);
                    }
                } else {
                    NormalUtils.INSTANCE.showErrorLog(getContainerActivity(),e);
                }
            }
        });

        //头像显示逻辑
        if (imgId.equals("")) {
            headerImageView.setImageResource(R.drawable.default_ic_contact);
        } else {
            FlareBitmapUtils.INSTANCE.loadBitmap(headerImageView, imgId);
        }

        nameTextView.setText(realName);
        idTextView.setText(userId);

        List<UserPersonalInfo> temp = DataSupport.where("userId=?",userId).
                find(UserPersonalInfo.class);
        if (temp.size() == 0) {
            userPersonalInfo = new UserPersonalInfo();
            userPersonalInfo.setBirthday(new Date());
            userPersonalInfo.setAcademy("乱搞");
            userPersonalInfo.setSex("男");
            userPersonalInfo.setSchool("乱来");
            userPersonalInfo.setUserId(userId);
        } else {
            userPersonalInfo = temp.get(0);
        }

        baseInfoTextView.setText(userPersonalInfo.getSchool() + " | " + userPersonalInfo.getAcademy());
        Date birthday = userPersonalInfo.getBirthday();
        Calendar today = Calendar.getInstance(Locale.CHINA);
        Calendar birthdayCalendar = null;
        if (birthday != null) {
            birthdayCalendar = Calendar.getInstance(Locale.CHINA);
            birthdayCalendar.setTime(birthday);
        }

        ageTextView.setText(birthday == null?"": (today.get(Calendar.YEAR) - birthdayCalendar.get(Calendar.YEAR)) + "");
        constellationTextView.setText(birthday == null? "" : getConstellation(birthdayCalendar.get(Calendar.MONTH),
                birthdayCalendar.get(Calendar.DAY_OF_MONTH)));
        birthdayTextView.setText(birthday == null?"请长按设置生日日期" : DATE_FORMAT.format(birthday));
        phoneEditText.setText(phoneNum);
        sexEditText.setText(userPersonalInfo.getSex() == null ? "男" : userPersonalInfo.getSex());
        phoneEditText.setInputType(EditorInfo.TYPE_NULL);
        sexEditText.setInputType(EditorInfo.TYPE_NULL);

        //设置已加入的班级
        List<ClassObject> classObjectList = DataSupport.findAll(ClassObject.class);
        List<String> tempIds = new ArrayList<>();
        List<ClassObject> curUserClass = new ArrayList<>();
        StringBuilder sb = new StringBuilder("");
        for (ClassObject classObject : classObjectList) {
            if (classObject.getMemberIds().contains(userId)) {
                if (!tempIds.contains(classObject.getClassID())) {
                    sb.append(classObject.getName());
                    sb.append(" ");
                    tempIds.add(classObject.getClassID());
                    curUserClass.add(classObject);
                }
            }
        }
        if (sb.toString().equals("")) {
            classesTextView.setText(" ");
        } else {
            classesTextView.setText(sb.toString());
        }
        listView.setDivider(null);
        listView.setGroupIndicator(null);
        listView.setAdapter(new MyAdapter(getContainerActivity(), curUserClass));

        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR) - 20, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        birthdayTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                datePickerDialog.setVibrate(false);
                datePickerDialog.setYearRange(1980, calendar.get(Calendar.YEAR));
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getContainerActivity().getSupportFragmentManager(), "date_picker_tag");
                return false;
            }
        });

        final ImageView phoneConfirmIvBtn = (ImageView) view.findViewById(R.id.iv_btn_phone_confirm);
        final ImageView phoneCancelIvBtn = (ImageView) view.findViewById(R.id.iv_btn_phone_cancel);

        headerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,ACTION_LOAD_IMAGE);
            }
        });

        //修改性别
        sexEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String target = "男";
                if (sexEditText.getText().toString().equals("男")) {
                    target = "女";
                }
                sexEditText.setText(target);
                userPersonalInfo.setSex(target);
                userPersonalInfo.update(userPersonalInfo.getId());
                return false;
            }
        });

        //修改性别
        view.findViewById(R.id.ll_sex_container).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String target = "男";
                if (sexEditText.getText().toString().equals("男")) {
                    target = "女";
                }
                sexEditText.setText(target);
                userPersonalInfo.setSex(target);
                userPersonalInfo.update(userPersonalInfo.getId());
                return false;
            }
        });

        //TODO 暂时不能修改手机号码
//        phoneEditText.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                phoneCancelIvBtn.setVisibility(View.VISIBLE);
//                phoneConfirmIvBtn.setVisibility(View.VISIBLE);
//                phoneEditText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
//                return false;
//            }
//        });

        phoneCancelIvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
                String source = preferences.getString(AppConst.USER_PHONE, "");
                phoneEditText.setText(source);
                phoneCancelIvBtn.setVisibility(View.GONE);
                phoneConfirmIvBtn.setVisibility(View.GONE);
                phoneEditText.setInputType(EditorInfo.TYPE_NULL);
            }
        });

        phoneConfirmIvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = phoneEditText.getText().toString();
                Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
                Matcher m = p.matcher(input);
                if (!m.matches()) {
                    Toast.makeText(getContainerActivity(), "额额，输入好像不是正确的手机号码哦~", Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences preferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(AppConst.USER_PHONE, input);
                    editor.apply();
                    phoneCancelIvBtn.setVisibility(View.GONE);
                    phoneConfirmIvBtn.setVisibility(View.GONE);
                    phoneEditText.setInputType(EditorInfo.TYPE_NULL);
                }
            }
        });
        return view;
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int i, int i1, int i2) {
        String birthday = i + "-" + (i1 + 1) + "-" + i2;
        birthdayTextView.setText(birthday);
        final Calendar calendar = Calendar.getInstance();
        int age = calendar.get(Calendar.YEAR);
        age -= i;
        ageTextView.setText(age + "岁");
        String constellation = getConstellation(i1,i2);
        constellationTextView.setText(constellation);

        calendar.set(i, i1, i2);
        userPersonalInfo.setBirthday(calendar.getTime());
        userPersonalInfo.update(userPersonalInfo.getId());

    }

    public static String getConstellation(int month, int day) {
        return day < dayArr[month - 1] ? constellationArr[month - 1] : constellationArr[month];
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int ACTION_GET_HEADER = 7;
        if (requestCode == ACTION_LOAD_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(data.getData(), "image/*");
                // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
                intent.putExtra("crop", "true");
                // aspectX aspectY 是宽高的比例
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                // outputX outputY 是裁剪图片宽高
                intent.putExtra("outputX", 150);
                intent.putExtra("outputY", 150);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, ACTION_GET_HEADER);
            }
        } else if (requestCode == ACTION_GET_HEADER) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");
                    Bitmap roundPhoto = BitmapUtils.INSTANCE.toRoundBitmap(photo);
                    headerImageView.setImageBitmap(roundPhoto);

                    UploadUtils.INSTANCE.uploadPersonalHeaderImg(roundPhoto,cmUserInfo,new UploadUtils.UploadListener() {
                        @Override
                        public void onUploadFinished() {

                        }

                        @Override
                        public void onUploadProgress(int progress) {

                        }
                    });
                }
            }
        }
    }

    private class MyAdapter extends BaseExpandableListAdapter {

        private List<ClassObject> items;

        private Context context;

        public MyAdapter (Context context,List<ClassObject> items) {
            this.context = context;

            this.items = items;
        }

        @Override
        public int getGroupCount() {
            return items.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public ClassObject getGroup(int groupPosition) {
            return items.get(groupPosition);
        }

        //获取子项即获取班级ID
        @Override
        public String getChild(int groupPosition, int childPosition) {
            return items.get(groupPosition).getClassID();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return getGroup(groupPosition).getId();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        private class GroupViewHolder {
            public TextView className;
        }

        private class ChildViewHolder {
            public LinearLayout actionContacts;
            public LinearLayout actionFiles;
            public LinearLayout actionAnnouncements;
            public TextView actionChange;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ClassObject item = getGroup(groupPosition);
            GroupViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_classes_in_main,null);
                holder = new GroupViewHolder();
                holder.className = (TextView) convertView.findViewById(R.id.item_classes_in_name);
                convertView.setTag(holder);
            } else {
                holder = (GroupViewHolder) convertView.getTag();
            }

            holder.className.setText(item.getName());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ClassObject item = getGroup(groupPosition);
            ChildViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_classes_in_sub,null);
                holder = new ChildViewHolder();
                holder.actionContacts = (LinearLayout) convertView.findViewById(R.id.item_classes_in_action_contacts);
                holder.actionFiles = (LinearLayout) convertView.findViewById(R.id.item_classes_in_action_file);
                holder.actionAnnouncements = (LinearLayout) convertView.findViewById(R.id.item_classes_in_action_announcement);
                holder.actionChange = (TextView) convertView.findViewById(R.id.item_classes_in_action_change);
                convertView.setTag(holder);
            } else {
                holder = (ChildViewHolder) convertView.getTag();
            }

            if (MainActivity.BASE_GLOBAL_DATA.getCurClassID().equals(item.getClassID())) {
                holder.actionChange.setText("当前班级");
                holder.actionChange.setTextColor(Color.BLACK);
                holder.actionChange.setBackgroundResource(R.drawable.invalid_normal_bg);
                holder.actionChange.setOnClickListener(null);
            } else {
                holder.actionChange.setText("切换至本班");
                holder.actionChange.setTextColor(Color.WHITE);
                holder.actionChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LocalDataBaseHelper.INSTANCE.changeCurClass(getContainerActivity(),item.getClassID(),item.getName(),userPersonalInfo);
                        getContainerActivity().finish();
                    }
                });
            }

            holder.actionAnnouncements.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContainerActivity().replaceFragment(AnnouncementListFragment.newInstance(item.getClassID(),-1),true);
                }
            });
            holder.actionFiles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContainerActivity().replaceFragment(FilesFragment.newInstance(item.getClassID()),true);
                }
            });
            holder.actionContacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContainerActivity().replaceFragment(ContactsFragment.newInstance(item.getClassID()),true);
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }


    @Override
    void backAction() {
        getContainerActivity().finish();
    }

    @Override
    String fragmentTitle() {
        return "个人中心";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }
}
