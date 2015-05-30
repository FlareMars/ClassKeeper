package com.flaremars.classmanagers.uis;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.flaremars.classmanagers.CMApplication;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AnnouncementObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.ProgressDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateAnnouncementFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener {

    public static final String DATE_PICKER_TAG = "datepicker";

    private static final String TARGET_CLASS = "param1";

    private TextView deadLineTextView;

    private String targetClass;

    private ClassObject curClass;

    private Date deadLine;

    public static CreateAnnouncementFragment newInstance(String classID) {
        CreateAnnouncementFragment fragment = new CreateAnnouncementFragment();
        Bundle args = new Bundle();
        args.putString(TARGET_CLASS, classID);
        fragment.setArguments(args);
        return fragment;
    }

    public CreateAnnouncementFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetClass = getArguments().getString(TARGET_CLASS);
        }
        if (targetClass == null) {
            targetClass = MainActivity.BASE_GLOBAL_DATA.getCurClassID();
        }
        curClass = DataSupport.where("classID=?",targetClass).find(ClassObject.class).get(0);
    }

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_announcement, container, false);

        final MaterialEditText titleEditText = (MaterialEditText) view.findViewById(R.id.et_create_announcement_title);
        titleEditText.setPrimaryColor(Color.argb(60, 0, 255, 0));
        final EditText contentEditText = (EditText) view.findViewById(R.id.et_create_announcement_content);

        view.findViewById(R.id.btn_create_announcement_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = titleEditText.getText().toString();
                final String content = contentEditText.getText().toString();

                if (title.equals("") || content.equals("")) {
                    Toast.makeText(getContainerActivity(),"标题和内容都不可为空",Toast.LENGTH_LONG).show();
                    return;
                }

                final AVObject newAnnouncement = new AVObject("CMAnnouncement");
                newAnnouncement.put("content",content);
                newAnnouncement.put("title",title);
                newAnnouncement.put("deadLine",deadLine);
                newAnnouncement.put("inClassId",targetClass);
                newAnnouncement.put("nameOfSender",MainActivity.BASE_GLOBAL_DATA.getUserRealName());
                progressDialog = new ProgressDialog(getContainerActivity(), "请稍候~");
                progressDialog.show();
                newAnnouncement.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        progressDialog.dismiss();
                        if (e == null) {
                            AnnouncementObject announcementObject = new AnnouncementObject();
                            announcementObject.setNetworkId(newAnnouncement.getObjectId());
                            announcementObject.setDeadline(deadLine);
                            announcementObject.setInClass(curClass);
                            announcementObject.setNameOfSender(MainActivity.BASE_GLOBAL_DATA.getUserRealName());
                            announcementObject.setTitle(title);
                            announcementObject.setContent(content);
                            announcementObject.save();

                            MainActivity.hasNewAnnouncement = true;

                            Intent intent = new Intent(getContainerActivity(), MainActivity.class);
                            startActivity(intent);
                            getContainerActivity().finish();
                            NormalUtils.INSTANCE.showToast(CMApplication.GLOBAL_CONTEXT,"公告发出成功");
                        }
                    }
                });

            }
        });
        deadLineTextView = (TextView) view.findViewById(R.id.tv_create_announcement_deadline);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,1);
        Date tomorrow = calendar.getTime();
        deadLine = tomorrow;
        deadLineTextView.setText(dateFormat.format(tomorrow));
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);

        deadLineTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.setVibrate(false);
                datePickerDialog.setYearRange(calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + 1);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getContainerActivity().getSupportFragmentManager(), DATE_PICKER_TAG);
            }
        });
        return view;
    }


    @Override
    void backAction() {
        if (!getContainerActivity().getSupportFragmentManager().popBackStackImmediate()) {
            Intent intent = new Intent(getContainerActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    @Override
    String fragmentTitle() {
        return "新公告";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        deadLineTextView.setText(year + "-" + (month + 1) + "-" + day);
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.set(year,month,day);
        deadLine = calendar.getTime();
    }
}
