package com.flaremars.classmanagers.uis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.ChattingActivity;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.MsgType;
import com.flaremars.classmanagers.utils.NormalUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleExcelActivity extends FragmentActivity {

    //0是观看模式 1是填写模式
    private int mode = 0;

    private List<ItemObject> items;

    private AVIMConversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_excel);

        Intent data = getIntent();
        final String title = data.getStringExtra("single_excel_title");
        final String questions = data.getStringExtra("single_excel_question");
        final String answers = data.getStringExtra("single_excel_answers");
        final String targetUser = data.getStringExtra("single_excel_target");
        conversation = ChattingActivity.CacheIMConversation.get(data.getStringExtra("target_conversation"));

        final TextView excelName = (TextView) findViewById(R.id.tv_fragment_fill_info_collection_name);
        excelName.setText(title);

        TextView actionBtn = (TextView) findViewById(R.id.tv_btn_fragment_fill_info_collection_confirm);

        TextView titleTextView = (TextView) findViewById(R.id.fragment_container_titleTextView);
        if (answers.equals("")) {
            mode = 1;
            titleTextView.setText("信息录制-填写信息");
            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean isFillComplete = true;
                    StringBuilder sb = new StringBuilder();
                    for (ItemObject item : items) {
                        sb.append(item.getAnswer());
                        sb.append("_");
                        if (item.getAnswer().equals("")) {
                            isFillComplete = false;
                            break;
                        }
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    if (!isFillComplete) {
                        Toast.makeText(SingleExcelActivity.this, "尚有未填写的项目，不可提交~", Toast.LENGTH_LONG).show();
                        return;
                    }

                    int intType =MsgType.EXCEL_OTHERS.ordinal();
                    String inClass = MainActivity.BASE_GLOBAL_DATA.getCurClassID();
                    AVIMTextMessage message = new AVIMTextMessage();
                    message.setText(title+"&"+questions+"&"+sb.toString());
                    Map<String,Object> attrs = new HashMap<>();
                    attrs.put("type",intType);
                    attrs.put("inClass",inClass);
                    message.setAttrs(attrs);
                    conversation.sendMessage(message, new AVIMConversationCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                NormalUtils.INSTANCE.showToast(SingleExcelActivity.this, "发送反馈成功~");
                                excelName.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        SingleExcelActivity.this.finish();
                                    }
                                }, 1000);
                            } else {
                                NormalUtils.INSTANCE.showError(SingleExcelActivity.this, e);
                            }
                        }
                    });
//                    final List<String> targetIds = new ArrayList<>();
//                    targetIds.add(targetUser);
//                    final JSONObject msgContent = new JSONObject();
//                    try {
//                        msgContent.put(MessageConst.MSG_TYPE,MessageConst.NEW_CHATTING_MESSAGE);
//                        msgContent.put(MessageConst.CONTENT_FROM_ID,MainActivity.BASE_GLOBAL_DATA.getUserID());
//                        msgContent.put(MessageConst.CONTENT_IN_CLASS, MainActivity.BASE_GLOBAL_DATA.getCurClassID());
//                        msgContent.put(MessageConst.CONTENT_MSG,"excel_task:"+title+"&"+questions+"&"+sb.toString());
//                        msgContent.put(MessageConst.CONTENT_IN_SUB_CLASS,"");
//                        msgContent.put(MessageConst.CONTENT_TYPE, MsgType.EXCEL_OTHERS.ordinal());
//                        msgContent.put(MessageConst.CONTENT_TIME, new Date().getTime());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    DataWrapper wrapper = new DataWrapper(
//                            MainActivity.BASE_GLOBAL_DATA.getUserID(),
//                            MainActivity.BASE_GLOBAL_DATA.getCurClassID(),
//                            "",
//                            targetIds,
//                            msgContent
//                    );
//
//                    int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
//                    Log.e("TAG", "messageId = " + messageId);
                }
            });
        } else {
            mode = 0;
            titleTextView.setText("信息录制-查看反馈");
            actionBtn.setVisibility(View.GONE);
        }

        findViewById(R.id.fragment_container_backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleExcelActivity.this.finish();
            }
        });

        //关键内容
        ListView contentListView = (ListView) findViewById(R.id.lv_fragment_fill_info_collection_content);
        String[] questionArray = questions.split("_");
        String[] answerArray = null;
        if (mode == 0) {
            answerArray = answers.split("_");
        }
        items = new ArrayList<>();
        for (int i = 0; i < questionArray.length;i++) {
            items.add(new ItemObject(questionArray[i],answerArray == null ? "" : answerArray[i]));
        }

        MyAdapter adapter = new MyAdapter(this, items);
        contentListView.setAdapter(adapter);
    }

    private class ItemObject {
        private String question;

        private String answer;

        public ItemObject(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }

    private class MyAdapter extends BaseAdapter {

        private List<ItemObject> items;

        private Context context;

        public MyAdapter(Context context,List<ItemObject> items) {
            this.items = items;
            this.context = context;
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
            public TextView questionTextView;

            public EditText answerEditText;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ItemObject item = getItem(position);
            final ViewHolder viewHolder;

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_info_collection_watch_fill,null);
                viewHolder = new ViewHolder();
                viewHolder.answerEditText = (EditText) convertView.findViewById(R.id.et_item_info_collection_answer);
                viewHolder.questionTextView = (TextView) convertView.findViewById(R.id.tv_item_info_collection_question);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (mode == 0) {
                viewHolder.answerEditText.setEnabled(false);
            } else {
                viewHolder.answerEditText.setEnabled(true);
            }

            viewHolder.answerEditText.setText(item.getAnswer());
            viewHolder.questionTextView.setText(item.getQuestion());

            if (mode == 1) {
                viewHolder.answerEditText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                items.get(position).setAnswer(viewHolder.answerEditText.getText().toString());
                            }
                        }
                        return false;
                    }
                });

                viewHolder.answerEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            items.get(position).setAnswer(viewHolder.answerEditText.getText().toString());
                        }
                    }
                });

            }
            return convertView;
        }
    }
}
