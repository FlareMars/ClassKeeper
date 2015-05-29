package com.flaremars.classmanagers.uis;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FunctionCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.ClassMemberObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.NoticeGroupMemberObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.RecentNoticeGroupMsg;
import com.flaremars.classmanagers.model.SubClassMemberObject;
import com.flaremars.classmanagers.model.SubClassObject;
import com.flaremars.classmanagers.model.UserObject;
import com.flaremars.classmanagers.utils.NormalUtils;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateClassFragment extends BaseFragment {

//    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

    private TextView academySelectedTextView;

    private TextView schoolSelectedTextView;

    private static final  int BASE_FONT_COLOR = Color.rgb(94, 92, 93);

    //1 班级 2 社团 3 选修班
    private int category = 1;

    private TextView categoryClass;

    private TextView categoryOrganization;

    private TextView categoryElectiveClass;

    private EditText newClassNameEditText;

    private UserObject creator;

    private AVUser avUser;

    private static final String [] SCHOOLS = {"武汉理工大学","西南石油大学"};

    private static final String[] WHUT_ACADEMYS = {
            "材料学院",
            "交通学院",
            "管理学院",
            "机电学院",
            "能动学院",
            "土建学院",
            "汽车学院",
            "资环学院",
            "信息学院",
            "经济学院",
            "艺术学院",
            "外语学院",
            "航运学院",
            "文法学院",
            "理学院",
            "计算机学院",
            "自动化学院",
            "网络(继续)教育",
            "职业技术学院",
            "政治与行政学院",
            "物流学院",
            "化工学院",
            "体育部",
            "国际教育学院",
            "思政部"
    };

    private static final String[] SWPU_ACADEMYS = {
            "石油与天然气工程学院",
            "地球科学与技术学院",
            "机电工程学院",
            "化学化工学院",
            "材料科学与工程学院",
            "计算机科学学院",
            "电气信息学院",
            "土木工程与建筑学院",
            "理学院",
            "经济管理学院（MBA教育中心)",
            "法学院",
            "马克思主义学院 ",
            "外国语学院",
            "体育学院",
            "艺术学院",
            "应用技术学院"
    };

    private String selectedSchool;

    public CreateClassFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Bmob.initialize(getContainerActivity(),AppConst.APP_ID);
        selectedSchool = "武汉理工大学";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_create_class, container, false);
        LinearLayout schoolSelector = (LinearLayout) view.findViewById(R.id.fragment_create_class_school);
        LinearLayout academySelector = (LinearLayout) view.findViewById(R.id.fragment_create_class_academy);
        schoolSelectedTextView = (TextView) view.findViewById(R.id.fragment_create_class_school_name);
        academySelectedTextView = (TextView) view.findViewById(R.id.fragment_create_class_academy_name);
        categoryClass = (TextView) view.findViewById(R.id.fragment_create_class_category_class);
        categoryElectiveClass = (TextView) view.findViewById(R.id.fragment_create_class_category_elective_class);
        categoryOrganization = (TextView) view.findViewById(R.id.fragment_create_class_category_organization);
        newClassNameEditText = (EditText) view.findViewById(R.id.fragment_create_class_new_name);
        final TextView createButton = (TextView) view.findViewById(R.id.fragment_create_class_create);

        SharedPreferences preferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        creator = DataSupport.where("userID=?", preferences.getString(AppConst.USER_ID,"")).find(UserObject.class).get(0);
        AVQuery<AVUser> query = AVUser.getQuery();
        query.getInBackground(preferences.getString(AppConst.USER_ID, ""), new GetCallback<AVUser>() {
            @Override
            public void done(AVUser object, AVException e) {
                avUser = object;
            }
        });

        schoolSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContainerActivity());
                builder.setIcon(R.drawable.cmicon_small);
                builder.setTitle("请选择学校");
                builder.setItems(SCHOOLS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedSchool = SCHOOLS[which];
                        schoolSelectedTextView.setText(selectedSchool);
                        if (which == 0) {
                            academySelectedTextView.setText("材料学院");
                        } else {
                            academySelectedTextView.setText("石油与天然气工程学院");
                        }
                    }
                });
                builder.show();
            }
        });

        academySelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContainerActivity());
                builder.setIcon(R.drawable.cmicon_small);
                builder.setTitle("请选择学院");
                final int whichSchool = selectedSchool.equals("武汉理工大学") ? 0 : 1;
                builder.setItems(selectedSchool.equals("武汉理工大学") ? WHUT_ACADEMYS : SWPU_ACADEMYS,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String selected;
                                selected = whichSchool == 0 ? WHUT_ACADEMYS[which] : SWPU_ACADEMYS[which];
                                academySelectedTextView.setText(selected);

                            }
                        });
                builder.show();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createButton.setEnabled(false);
                final String newClassName = newClassNameEditText.getText().toString();
                if (newClassName.equals("")) {
                    Toast.makeText(getContainerActivity(),"班级名不能为空~",Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getContainerActivity(),"正在创建班级，请稍候~",Toast.LENGTH_LONG).show();

                Map<String,String> parameters = new HashMap<>();
                parameters.put("school",selectedSchool);
                AVCloud.callFunctionInBackground("getClassCode", parameters, new FunctionCallback<String>() {
                    public void done(final String result, AVException e) {
                        if (e == null) {

                            if (!CMApplication.isClientOpened) {
                                CMApplication.openIMClient(creator.getUserId());
                                NormalUtils.INSTANCE.showToast(getContainerActivity(),"当前网络状态不佳，请稍候重试");
                                createButton.setEnabled(true);
                                return;
                            }

                            List<String> memberIds = new ArrayList<>();
                            memberIds.add(creator.getUserId());

                            Map<String, Object> attr = new HashMap<>();
                            attr.put("type", AppConst.ConversationType_Group);
                            final String academy = academySelectedTextView.getText().toString();

                            CMApplication.imClient.createConversation(memberIds, "【" + newClassName + "】" + "全体成员",
                                    attr, new AVIMConversationCreatedCallback() {
                                        @Override
                                        public void done(final AVIMConversation avimConversation, AVException e) {
                                            if (e == null) {
                                                final AVObject newCMClass = new AVObject("CMClassObject");
                                                newCMClass.put("name", newClassName);
                                                newCMClass.put("classCode", result);
                                                newCMClass.put("headerImg", "");
                                                newCMClass.put("creatorId", creator.getUserId());
                                                newCMClass.put("school", selectedSchool);
                                                newCMClass.put("academy", academy);
                                                newCMClass.put("studentCount", 1);
                                                newCMClass.put("managers", new ArrayList<String>());
                                                AVRelation<AVUser> members = newCMClass.getRelation("members");
                                                members.add(avUser);
                                                //全员子群
                                                newCMClass.put("allMembersConversation", avimConversation.getConversationId());

                                                newCMClass.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(AVException e) {
                                                        if (e == null) {
                                                            Map<String, Object> attr = new HashMap<>();
                                                            attr.put("type", AppConst.ConversationType_Group);
                                                            attr.put("classId", newCMClass.getObjectId());
                                                            avimConversation.setAttributes(attr);
                                                            avimConversation.updateInfoInBackground(new AVIMConversationCallback() {
                                                                @Override
                                                                public void done(AVException e) {
                                                                    if (e != null) {
                                                                        NormalUtils.INSTANCE.showError(getContainerActivity(), e);
                                                                    }
                                                                }
                                                            });

                                                            //绑定新加入的班级和子群
                                                            avUser.add("subClassIds", avimConversation.getConversationId());
                                                            AVRelation<AVObject> inClasses = avUser.getRelation("inClasses");
                                                            inClasses.add(newCMClass);
                                                            avUser.saveInBackground();

                                                            //数据本地化
                                                            ClassObject classObject = new ClassObject();
                                                            classObject.setCreator(creator.getUserId());
                                                            classObject.setType(0);
                                                            classObject.setClassCode(newCMClass.getString("classCode"));
                                                            Date now = new Date();
                                                            classObject.setClassID(newCMClass.getObjectId());
                                                            classObject.setTime(now);
                                                            classObject.setName(newCMClass.getString("name"));
                                                            classObject.setInAcademy(academy);
                                                            classObject.setInSchool(selectedSchool);
                                                            classObject.setTotalPeopleCount(1);
                                                            classObject.setHeaderPath("");
                                                            classObject.save();

                                                            ClassMemberObject newMember = new ClassMemberObject();
                                                            newMember.setMemberID(creator.getUserId());
                                                            newMember.setMemberName(creator.getUserRealName());
                                                            newMember.setInClass(classObject);
                                                            newMember.save();

                                                            SubClassObject subClassObject = new SubClassObject();
                                                            subClassObject.setInClass(classObject);
                                                            subClassObject.setTime(classObject.getTime());
                                                            subClassObject.setSubClassName("【" + newClassName + "】" + "全体成员");
                                                            subClassObject.setSubClassID(avimConversation.getConversationId());
                                                            subClassObject.save();

                                                            SubClassMemberObject subClassMember = new SubClassMemberObject();
                                                            subClassMember.setMemberName(creator.getUserRealName());
                                                            subClassMember.setMemberID(creator.getUserId());
                                                            subClassMember.setInSubClass(subClassObject);
                                                            subClassMember.save();

                                                            //创建一个全体成员的通知组
                                                            NoticeGroupObject noticeGroupObject = new NoticeGroupObject();
                                                            noticeGroupObject.setName("【 " + classObject.getName() + "】全体成员通知组");
                                                            noticeGroupObject.setNoticeGroupID(new Date().getTime() + "");
                                                            noticeGroupObject.setInClass(classObject);
                                                            noticeGroupObject.setAllMember(true);
                                                            noticeGroupObject.save();

                                                            RecentNoticeGroupMsg recentNoticeGroupMsg = new RecentNoticeGroupMsg();
                                                            recentNoticeGroupMsg.setContent("");
                                                            recentNoticeGroupMsg.setNoReadNumber(0);
                                                            recentNoticeGroupMsg.setTime(new Date());
                                                            recentNoticeGroupMsg.setNoticeGroup(noticeGroupObject);
                                                            recentNoticeGroupMsg.save();

                                                            NoticeGroupMemberObject newNoticeGroupMember = new NoticeGroupMemberObject();
                                                            newNoticeGroupMember.setMemberName(creator.getUserRealName());
                                                            newNoticeGroupMember.setMemberID(creator.getUserId());
                                                            newNoticeGroupMember.setInNoticeGroup(noticeGroupObject);
                                                            newNoticeGroupMember.save();

                                                            //更新用户信息中的当前班级
                                                            AVQuery<AVObject> query = new AVQuery<>("UserToCurClass");

                                                            SharedPreferences sharedPreferences = getContainerActivity().getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
                                                            query.getInBackground(sharedPreferences.getString(AppConst.USER_TO_CURCLASS, ""), new GetCallback<AVObject>() {
                                                                @Override
                                                                public void done(AVObject avObject, AVException e) {
                                                                    avObject.put("curClassId", newCMClass.getObjectId());
                                                                    avObject.put("curClassName", newCMClass.get("name"));
                                                                    avObject.saveInBackground();
                                                                }
                                                            });

                                                            Intent finishActivityBroadCast = new Intent("FRAGMENT_TO_FINISH");
                                                            getContainerActivity().sendBroadcast(finishActivityBroadCast);

                                                            //转到成功创建页面
                                                            getContainerActivity().replaceFragment(CreateClassSuccessFragment.newInstance(classObject.getClassCode(),
                                                                    classObject.getName(), classObject.getClassID()), false);
                                                        } else {
                                                            NormalUtils.INSTANCE.showError(getContainerActivity(), e);
                                                        }
                                                    }
                                                });
                                            } else {
                                                NormalUtils.INSTANCE.showError(getContainerActivity(), e);
                                            }
                                        }
                                    });
                        } else {
                            NormalUtils.INSTANCE.showError(getContainerActivity(),e);
                        }
                    }
                });
            }
        });

        categoryClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category == 1) {
                    return;
                }
                categoryClass.setBackgroundResource(R.drawable.view_category_bg);
                categoryOrganization.setBackgroundResource(R.drawable.view_category_bg_white);
                categoryElectiveClass.setBackgroundResource(R.drawable.view_category_bg_white);
                categoryClass.setTextColor(Color.WHITE);
                categoryOrganization.setTextColor(BASE_FONT_COLOR);
                categoryElectiveClass.setTextColor(BASE_FONT_COLOR);
                category = 1;
            }
        });
        categoryOrganization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category == 2) {
                    return;
                }
                categoryClass.setBackgroundResource(R.drawable.view_category_bg_white);
                categoryOrganization.setBackgroundResource(R.drawable.view_category_bg);
                categoryElectiveClass.setBackgroundResource(R.drawable.view_category_bg_white);
                categoryClass.setTextColor(BASE_FONT_COLOR);
                categoryOrganization.setTextColor(Color.WHITE);
                categoryElectiveClass.setTextColor(BASE_FONT_COLOR);
                category = 2;
            }
        });
        categoryElectiveClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category == 3) {
                    return;
                }
                categoryClass.setBackgroundResource(R.drawable.view_category_bg_white);
                categoryOrganization.setBackgroundResource(R.drawable.view_category_bg_white);
                categoryElectiveClass.setBackgroundResource(R.drawable.view_category_bg);
                categoryClass.setTextColor(BASE_FONT_COLOR);
                categoryOrganization.setTextColor(BASE_FONT_COLOR);
                categoryElectiveClass.setTextColor(Color.WHITE);
                category = 3;
            }
        });
        return view;
    }

    private String getCode() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
//        AVQuery<AVObject> countClass = new AVQuery<>("CMClassObject");
//        countClass.whereEqualTo("school","武汉理工大学");
//        try {
//            countClass.countInBackground(new CountCallback() {
//                @Override
//                public void done(int i, AVException e) {
//
//                }
//            });
//            DecimalFormat decimalFormat = new DecimalFormat("00000");
//            return "whut" + decimalFormat.format(count);
//        } catch (AVException e) {
//            e.printStackTrace()
//        };
        return "whut" + (int)(Math.random() * 10000);
    }

    @Override
    void backAction() {
        if (!getContainerActivity().getSupportFragmentManager().popBackStackImmediate()) {
            getContainerActivity().finish();
        }
    }

    @Override
    String fragmentTitle() {
        return "创建班级";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }
}
