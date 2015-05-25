package com.flaremars.classmanagers.uis;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flaremars.classmanagers.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeDetailFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.CHINA);

    private String nameOfSender;
    private String time;
    private String content;

    public static NoticeDetailFragment newInstance(String nameOfSender, String time,String content) {
        NoticeDetailFragment fragment = new NoticeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, nameOfSender);
        args.putString(ARG_PARAM2, time);
        args.putString(ARG_PARAM3, content);
        fragment.setArguments(args);
        return fragment;
    }

    public NoticeDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nameOfSender = getArguments().getString(ARG_PARAM1);
            time = getArguments().getString(ARG_PARAM2);
            time = DATE_FORMAT.format(new Date(Long.valueOf(time)));
            content = getArguments().getString(ARG_PARAM3);
        }
    }

    @Override
    void backAction() {
        getContainerActivity().finish();
    }

    @Override
    String fragmentTitle() {
        return "通知详情";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_notice_detail, container, false);
        TextView nameTextView = (TextView) view.findViewById(R.id.tv_notice_detail_name);
        TextView timeTextView = (TextView) view.findViewById(R.id.tv_notice_detail_time);
        TextView contentTextView = (TextView) view.findViewById(R.id.tv_notice_detail_content);

        nameTextView.setText(nameOfSender);
        timeTextView.setText(time);
        contentTextView.setText(content);
        return view;
    }


}
