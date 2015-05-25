package com.flaremars.classmanagers.uis;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.views.Dialog;
import com.rengwuxian.materialedittext.MaterialEditText;


public class FeedbackActivity extends Activity {

    private MaterialEditText contentEditText;

    private MaterialEditText contactEditText;

    private Dialog feedbackSuccess ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        contactEditText = (MaterialEditText) findViewById(R.id.feedback_contact);
        contentEditText = (MaterialEditText) findViewById(R.id.feedback_main_content);

        feedbackSuccess = new Dialog(this,"提交成功","感谢你宝贵的建议/意见，若合理，我们会认真处理~");

        findViewById(R.id.fragment_container_backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedbackActivity.this.finish();
            }
        });

        findViewById(R.id.feedback_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AVObject feedback = new AVObject("SoftwareFeedback");
                feedback.put("content",contentEditText.getText().toString());
                feedback.put("contact",contactEditText.getText().toString());
                feedback.put("senderId",MainActivity.BASE_GLOBAL_DATA.getUserID());
                feedback.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        feedbackSuccess.show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (feedbackSuccess.isShowing()) {
            feedbackSuccess.dismiss();
        }
    }
}
