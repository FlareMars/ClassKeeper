package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.mainui.SelectContactsActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.DataWrapper;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.model.ManagerObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.model.UserPersonalInfo;
import com.flaremars.classmanagers.utils.BitmapUtils;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;
import com.flaremars.classmanagers.utils.LocalDataBaseHelper;
import com.flaremars.classmanagers.utils.MessagePostUtil;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.utils.UploadUtils;
import com.flaremars.classmanagers.views.RoundImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClassInfoFragment extends BaseFragment {

    private static int ACTION_LOAD_IMAGE = 8;

    private String targetClassID;

    private ClassObject targetClassObject;

    private AVObject cmClassObject;

    private List<ManagerObject> managers;

    private List<String> curManagers = new ArrayList<>();

    private TextView managersTextView;

    private RoundImageView roundImageView;

    public static boolean isManagerChanged ;

    private static boolean isCreator;

    public static ClassInfoFragment newInstance(String targetClassID) {
        ClassInfoFragment fragment = new ClassInfoFragment();
        Bundle args = new Bundle();
        args.putString(AppConst.CLASS_ID, targetClassID);
        fragment.setArguments(args);
        return fragment;
    }

    public ClassInfoFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            targetClassID = getArguments().getString(AppConst.CLASS_ID);
            List<ClassObject> temp  = DataSupport.where("classID=?",targetClassID).find(ClassObject.class);
            if (temp.size() > 0) {
                targetClassObject = temp.get(0);
            } else {
                NormalUtils.INSTANCE.showToast(getContainerActivity(),"未知错误，请联系班级管家开发团队");
                targetClassObject = new ClassObject();
            }
        }
        isManagerChanged = false;
        isCreator = false;
        super.onCreate(savedInstanceState);
    }

    @Override
    void backAction() {
        Intent intent = new Intent(getContainerActivity(), MainActivity.class);
        startActivity(intent);
        getContainerActivity().finish();
    }

    @Override
    String fragmentTitle() {
        return "班级中心";
    }

    @Override
    List<Integer> setActionViews() {
        List<Integer> targetAction = new ArrayList<>();
        targetAction.add(R.id.main_container_create_class);
        return targetAction;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_info, container, false);
        TextView baseInfoTextView = (TextView) view.findViewById(R.id.class_info_base);
        TextView creatorTextView = (TextView) view.findViewById(R.id.class_info_creator);
        TextView timeTextView = (TextView) view.findViewById(R.id.class_info_time);
        TextView idTextView = (TextView) view.findViewById(R.id.class_info_number);
        roundImageView = (RoundImageView) view.findViewById(R.id.class_info_pic);
        managersTextView = (TextView) view.findViewById(R.id.class_info_managers);

        baseInfoTextView.setText(targetClassObject.getInSchool() + " | " + targetClassObject.getInAcademy() + "\n" +
                targetClassObject.getName() + " " + targetClassObject.getTotalPeopleCount() + "人");
        timeTextView.setText(new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(targetClassObject.getTime()));
        idTextView.setText(targetClassObject.getClassCode());

        //获取创建者数据
        List<UserObject> tempCreator = DataSupport.where("userId=?",targetClassObject.getCreator()).find(UserObject.class);
        UserObject creator = null;
        if (tempCreator.size() != 0) {
            creator = tempCreator.get(0);
        }
        if (creator == null) {
            creatorTextView.setText("");
        } else {
            creatorTextView.setText(creator.getUserRealName());
        }

        //显示班级头像
        if (targetClassObject.getHeaderPath().equals("")) {
            roundImageView.setImageResource(R.drawable.pic_testimg);
        } else {
            FlareBitmapUtils.INSTANCE.loadBitmap(roundImageView,targetClassObject.getHeaderPath());
        }

        //切换班级
        view.findViewById(R.id.class_info_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeClassDialog dialog = ChangeClassDialog.getClassDialog("切换班级");
                dialog.setOnConfirmClickListener(new ChangeClassDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(final ClassObject item) {
                        UserPersonalInfo userPersonalInfo = DataSupport.where("userId=?",MainActivity.BASE_GLOBAL_DATA.getUserID()).
                                find(UserPersonalInfo.class).get(0);
                        LocalDataBaseHelper.INSTANCE.changeCurClass(getContainerActivity(),item.getClassID(),item.getName(),
                                userPersonalInfo);

                        Intent i = new Intent(getActivity(), ContainerActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_CLASS_CENTER);
                        bundle.putString(AppConst.CLASS_ID, item.getClassID());
                        i.putExtras(bundle);
                        startActivity(i);
                        getActivity().finish();
                    }
                });
                dialog.show(getFragmentManager(), "tag_change_class_dialog");
            }
        });

        //如果是创建者或者管理员，才有资格换头像
        roundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!targetClassObject.getClassID().equals("")) {
                    if (!NormalUtils.INSTANCE.isCreatorOrManager(targetClassObject)) {
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, ACTION_LOAD_IMAGE);
                }
            }
        });

        isCreator = targetClassObject.getCreator().equals(MainActivity.BASE_GLOBAL_DATA.getUserID());
        if (isCreator) {
            managers = DataSupport.where("classobject_id=?", String.valueOf(targetClassObject.getId())).find(ManagerObject.class);
            for (ManagerObject manager : managers) {
                curManagers.add(manager.getManagerID());
            }

            //如果是创建者，允许进行管理员更换
            managersTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChangeManagerDialog dialog2 = ChangeManagerDialog.getManagerDialog(managers);
                    dialog2.show(getFragmentManager(), "tag_change_manager_dialog");
                    dialog2.setOnDismissListener(new ChangeManagerDialog.OnManagerDialogDismissListener() {
                        @Override
                        public void onManagerDialogDismiss() {
                            StringBuilder sb = new StringBuilder();
                            for (ManagerObject managerObject : managers) {
                                sb.append(managerObject.getName());
                                sb.append(" ");
                            }
                            managersTextView.setText(sb.toString());
                        }
                    });
                }
            });
        } else {
            managersTextView.setOnClickListener(null);
        }


        //预先获取网络班级信息对象
        if (!targetClassObject.getClassID().equals("")) {
            AVQuery<AVObject> query = new AVQuery<>("CMClassObject");
            query.getInBackground(targetClassID, new GetCallback<AVObject>() {
                @Override
                public void done(AVObject avObject, AVException e) {
                    if (e == null) {
                        cmClassObject = avObject;

                        //获取当前管理员列表
                        List<String> values = cmClassObject.getList("managers");
                        if (values == null) {
                            values = new ArrayList<>();
                        }
                        StringBuilder sb = new StringBuilder("");
                        for (String value : values) {
                            if (value.equals("")) {
                                continue;
                            }
                            UserObject object = DataSupport.where("userId=?", value).find(UserObject.class).get(0);
                            if (value.equals(MainActivity.BASE_GLOBAL_DATA.getUserID())) {
                                List<ManagerObject> temp = DataSupport.where("classobject_id=?", String.valueOf(targetClassObject.getId())).
                                        find(ManagerObject.class);
                                if (temp.size() == 0) {
                                    ManagerObject newManager = new ManagerObject();
                                    newManager.setManagerID(MainActivity.BASE_GLOBAL_DATA.getUserID());
                                    newManager.setName(MainActivity.BASE_GLOBAL_DATA.getUserRealName());
                                    newManager.setInClass(targetClassObject);
                                    newManager.save();
                                }
                            }
                            sb.append(object.getUserRealName());
                            sb.append(" ");
                        }
                        if (sb.toString().equals("") && isCreator) {
                            managersTextView.setText("点击设置管理员");
                        } else {
                            managersTextView.setText(sb.toString());
                        }
                    } else {
                        NormalUtils.INSTANCE.showErrorLog(getContainerActivity(),e);
                    }
                }
            });
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int ACTION_GET_HEADER = 7;
        if (requestCode == ACTION_LOAD_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                /*start to cut photo*/
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
                    roundImageView.setImageBitmap(roundPhoto);

                    UploadUtils.INSTANCE.uploadClassHeaderImg(roundPhoto, cmClassObject, targetClassObject, new UploadUtils.UploadListener() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //向存在变动的管理发出变动消息
        final List<String> newManagers = new ArrayList<>();
        if (isManagerChanged) {
            for (ManagerObject temp : managers) {
                newManagers.add(temp.getManagerID());
                if (curManagers.contains(temp.getManagerID())) {
                    curManagers.remove(temp.getManagerID());
                    newManagers.remove(temp.getManagerID());
                }
            }

            //curManagers剩下的就是被清除管理员资格的
            if (curManagers.size() > 0) {
                final JSONObject content = new JSONObject();
                try {
                    content.put(MessageConst.MSG_TYPE, MessageConst.REMOVE_YOUR_MANAGER);
                    content.put(MessageConst.CONTENT_IN_CLASS, targetClassObject.getClassID());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                cmClassObject.removeAll("managers",curManagers);
                cmClassObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            if (curManagers.size() > 0) {
                                for (String manageId : curManagers) {
                                    ManagerObject temp = DataSupport.where("managerID = ?",manageId).find(ManagerObject.class).get(0);
                                    temp.delete();
                                }
                            }
                            Log.e("TAG","删除管理成员");
                        } else {
                            NormalUtils.INSTANCE.showError(getContainerActivity(),e);
                        }
                    }
                });


                DataWrapper wrapper = new DataWrapper(
                        MainActivity.BASE_GLOBAL_DATA.getUserID(),
                        targetClassObject.getClassID(),
                        "",
                        curManagers,
                        content
                );

                int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                Log.e("TAG", "messageId = " + messageId);

            }
            //managers剩下的就是新增的管理员
            if (managers.size() > 0) {
                final JSONObject content = new JSONObject();
                try {
                    content.put(MessageConst.MSG_TYPE, MessageConst.TO_BE_MANAGER);
                    content.put(MessageConst.CONTENT_IN_CLASS, targetClassObject.getClassID());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                cmClassObject.addAll("managers",newManagers);
                cmClassObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            Log.e("TAG","新增管理成员");
                            for (String manageId : curManagers) {
                                ManagerObject newManager = new ManagerObject();
                                UserObject userObject = DataSupport.where("userId=?",manageId).find(UserObject.class).get(0);
                                newManager.setManagerID(userObject.getUserId());
                                newManager.setName(userObject.getUserRealName());
                                newManager.setInClass(targetClassObject);
                                newManager.save();
                            }
                        } else {
                            NormalUtils.INSTANCE.showError(getContainerActivity(),e);
                        }
                    }
                });

                DataWrapper wrapper = new DataWrapper(
                        MainActivity.BASE_GLOBAL_DATA.getUserID(),
                        targetClassObject.getClassID(),
                        "",
                        newManagers,
                        content
                );

                int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                Log.e("TAG", "messageId = " + messageId);
            }
        }
    }

    //管理管理员对话框
    public static class ChangeManagerDialog extends DialogFragment {
        public static int ACTION_SELECT_CONTACTS_ClASS_INFO = 2;

        private static ChangeManagerDialog instance;

        private List<ManagerObject> items;

        private MyAdapter adapter;

        private OnManagerDialogDismissListener listener;

        public interface OnManagerDialogDismissListener {
            void onManagerDialogDismiss();
        }

        public ChangeManagerDialog() {

        }

        public void setOnDismissListener(OnManagerDialogDismissListener listener) {
            instance.listener = listener;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            instance.listener.onManagerDialogDismiss();
        }

        public static ChangeManagerDialog getManagerDialog (List<ManagerObject> items) {
            if (instance == null) {
                instance = new ChangeManagerDialog();
                instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
            }
            instance.items = items;
            return instance;
        }

        private class MyAdapter extends BaseAdapter {

            private Context context;

            private List<ManagerObject> items;

            public MyAdapter(Context context,List<ManagerObject> items) {
                this.context = context;
                this.items = items;
            }

            @Override
            public int getCount() {
                return items.size();
            }

            @Override
            public ManagerObject getItem(int position) {
                return items.get(position);
            }

            @Override
            public long getItemId(int position) {
                return getItem(position).getId();
            }

            private class ViewHolder {
                public TextView nameTextView;

                public ImageView deleteBtn;
            }

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ManagerObject item = getItem(position);
                ViewHolder viewHolder;

                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = View.inflate(context,R.layout.item_change_manager,null);
                    viewHolder.deleteBtn = (ImageView) convertView.findViewById(R.id.btn_item_change_manager_delete);
                    if (!isCreator) {
                        viewHolder.deleteBtn.setVisibility(View.GONE);
                    }
                    viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.tv_item_change_manager_name);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.nameTextView.setText(item.getName());
                viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        items.remove(item);
                        MyAdapter.this.notifyDataSetChanged();
                        isManagerChanged = true;
                    }
                });
                return convertView;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_change_manager_dialog, container, false);
            ListView listView = (ListView) view.findViewById(R.id.lv_change_manager_dialog_content);
            ImageView addManagerBtn = (ImageView) view.findViewById(R.id.btn_change_manager_add);

            listView.setDivider(null);
            adapter = new MyAdapter(getActivity(),items);
            listView.setAdapter(adapter);

            if (isCreator) {
                addManagerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SelectContactsActivity.class);
                        startActivityForResult(intent, ACTION_SELECT_CONTACTS_ClASS_INFO);
                    }
                });
            } else {
                addManagerBtn.setVisibility(View.GONE);
            }
            return view;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == ACTION_SELECT_CONTACTS_ClASS_INFO) {
                if (resultCode == Activity.RESULT_OK) {
                    String contactsStr = data.getStringExtra("contacts_data");
                    String [] contacts = contactsStr.split("_");
                    for (String contact : contacts) {
                        UserObject userObject = DataSupport.where("userId=?",contact).find(UserObject.class).get(0);
                        ManagerObject newManager = new ManagerObject();
                        newManager.setManagerID(userObject.getUserId());
                        newManager.setName(userObject.getUserRealName());
                        if (!items.contains(newManager)) {
                            items.add(newManager);
                            isManagerChanged = true;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
