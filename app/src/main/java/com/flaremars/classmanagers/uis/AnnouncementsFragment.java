package com.flaremars.classmanagers.uis;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.model.AnnouncementObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.FragmentConst;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AnnouncementsFragment extends Fragment {
    private static final String TITLE = "title";
    private static final String BASE_INFO = "base_info";
    private static final String CONTENT = "content";
    private static final String TARGET = "targetClass";

    private String title;
    private String baseInfo;
    private String content;
    private String targetClass;

    private AnnouncementObject announcementObject;


    public static AnnouncementsFragment newInstance(AnnouncementObject announcementObject,ClassObject targetClass) {
        AnnouncementsFragment fragment = new AnnouncementsFragment();
        Bundle args = new Bundle();
        fragment.announcementObject = announcementObject;
        String title = "";
        String baseInfo = "";
        String content = "";
        String target = "";

        if (announcementObject.getDeadline() != null) {
            title = announcementObject.getTitle();
            baseInfo = announcementObject.getNameOfSender() +
                    " 有效至 " + new SimpleDateFormat("MM月dd日", Locale.CHINA).format(announcementObject.getDeadline());
            content = announcementObject.getContent();
            target = targetClass == null?"" : targetClass.getClassID();
        }
        args.putString(TITLE, title);
        args.putString(BASE_INFO, baseInfo);
        args.putString(CONTENT,content);
        args.putString(TARGET, target);
        fragment.setArguments(args);
        return fragment;
    }

    public AnnouncementObject getObject() {
        return announcementObject;
    }

    public AnnouncementsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.title = getArguments().getString(TITLE);
            this.baseInfo = getArguments().getString(BASE_INFO);
            this.content = getArguments().getString(CONTENT);
            this.targetClass = getArguments().getString(TARGET);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_announcements, container, false);
        ((TextView)view.findViewById(R.id.announcement_tv_title)).setText(title);
        ((TextView)view.findViewById(R.id.announcement_tv_base_info)).setText(baseInfo);
        ((TextView)view.findViewById(R.id.announcement_tv_content)).setText(content);

        view.findViewById(R.id.fragment_announcement_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!targetClass.equals("") && !content.equals("")) {
                    Intent intent = new Intent(getActivity(), ContainerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_ANNOUNCEMENTS_LIST);
                    bundle.putString(AppConst.CLASS_ID, targetClass);
                    bundle.putInt(AppConst.TARGET_ANNOUNCEMENT, announcementObject.getId());
                    bundle.putBoolean(AppConst.NEED_ADD_TO_BACK_STACK, false);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        return view;
    }


}
