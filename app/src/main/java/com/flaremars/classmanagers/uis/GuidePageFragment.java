package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.FragmentConst;
import com.flaremars.classmanagers.utils.FlareBitmapUtils;


public class GuidePageFragment extends Fragment {

    private Context context;

    private ImageView headerView;

    public GuidePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide_page, container, false);
        LinearLayout toPersonalCenter = (LinearLayout) view.findViewById(R.id.ll_to_personal_center);
        LinearLayout toAddClass = (LinearLayout) view.findViewById(R.id.ll_to_add_class);
        headerView = (ImageView) view.findViewById(R.id.guide_page_header);

        SharedPreferences preferences = context.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,Context.MODE_PRIVATE);
        String userRealName = preferences.getString(AppConst.USER_REAL_NAME, "");
        String userId = preferences.getString(AppConst.USER_ID, "");
        TextView nameTextView = (TextView) view.findViewById(R.id.tv_guide_page_name);
        nameTextView.setText(userRealName);

        if (MainActivity.BASE_GLOBAL_DATA != null) {
            //清除之前用户的影响
            MainActivity.BASE_GLOBAL_DATA.setCurClassID("");
        }
        MainActivity.BASE_GLOBAL_DATA.setUserID(userId);

        toPersonalCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ContainerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_PERSONAL_CENTER);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        toAddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, JoinClassActivity.class);
                startActivity(intent);
            }
        });

        if (!CMApplication.isClientOpened) {
            CMApplication.openIMClient(userId);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = context.getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,Context.MODE_PRIVATE);
        String headerId = preferences.getString(AppConst.USER_HEADER_ID, "");
        if (headerId == null || headerId.equals("")) {
            headerView.setImageResource(R.drawable.default_ic_contact);
        } else {
            FlareBitmapUtils.INSTANCE.loadBitmap(headerView,headerId);
        }
    }
}
