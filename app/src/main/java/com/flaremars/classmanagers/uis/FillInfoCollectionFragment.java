package com.flaremars.classmanagers.uis;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.DataWrapper;
import com.flaremars.classmanagers.model.ExcelObject;
import com.flaremars.classmanagers.model.MessageConst;
import com.flaremars.classmanagers.utils.MessagePostUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FillInfoCollectionFragment extends BaseFragment {
    private static final String ARG_PARAM1 = "param1";

    private ExcelObject excelObject;

    private TextView actionTvBtn;

    private  MyAdapter adapter = null;

    private List<ItemObject> items;

    private VIEW_TYPE forWhat;

    private enum VIEW_TYPE {
        FOR_FILL,
        FOR_WATCH
    }

    public static FillInfoCollectionFragment newInstance(int targetID) {
        FillInfoCollectionFragment fragment = new FillInfoCollectionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, targetID);
        fragment.setArguments(args);
        return fragment;
    }

    public FillInfoCollectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int targetId = getArguments().getInt(ARG_PARAM1);
            excelObject = DataSupport.find(ExcelObject.class,targetId,true);
        }

        if (excelObject.getAnswers().equals("")) {
            forWhat = VIEW_TYPE.FOR_FILL;
        } else {
            forWhat = VIEW_TYPE.FOR_WATCH;
        }
    }

    @Override
    void backAction() {
        Intent intent = new Intent(getContainerActivity(), MainActivity.class);
        startActivity(intent);

    }

    @Override
    String fragmentTitle() {
        return "信息录制";
    }

    @Override
    List<Integer> setActionViews() {
        return null;
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

    private class MyAdapter extends BaseAdapter{

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

            if (forWhat == VIEW_TYPE.FOR_WATCH) {
                viewHolder.answerEditText.setEnabled(false);
            } else {
                viewHolder.answerEditText.setEnabled(true);
            }

            viewHolder.answerEditText.setText(item.getAnswer());
            viewHolder.questionTextView.setText(item.getQuestion());

            if (forWhat == VIEW_TYPE.FOR_FILL) {
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

                viewHolder.answerEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!s.toString().equals("")) {
                            items.get(position).setAnswer(viewHolder.answerEditText.getText().toString());
                        }
                    }
                });
            }
            return convertView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_fill_info_collection, container, false);
        TextView titleTextView = (TextView) view.findViewById(R.id.tv_fragment_fill_info_collection_name);
        actionTvBtn = (TextView) view.findViewById(R.id.tv_btn_fragment_fill_info_collection_confirm);
        if (forWhat == VIEW_TYPE.FOR_FILL) {
            actionTvBtn.setText("上传");
        } else {
            actionTvBtn.setText("修改");
        }
        titleTextView.setText(excelObject.getName());
        ListView contentListView = (ListView) view.findViewById(R.id.lv_fragment_fill_info_collection_content);
        actionTvBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (forWhat == VIEW_TYPE.FOR_FILL) {
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
                        Toast.makeText(getContainerActivity(), "尚有未填写的项目，不可提交~", Toast.LENGTH_LONG).show();
                    } else {
                        excelObject.setAnswers(sb.toString());
                        excelObject.update(excelObject.getId());

                        final List<String> targetIds = new ArrayList<>();
                        targetIds.add(excelObject.getIdOfSender());
                        final JSONObject msgContent = new JSONObject();
                        try {
                            msgContent.put(MessageConst.MSG_TYPE,MessageConst.NEW_EXCEL_FEEDBACK);
                            msgContent.put(MessageConst.CONTENT_FROM_ID,MainActivity.BASE_GLOBAL_DATA.getUserID());
                            msgContent.put(MessageConst.CONTENT_TARGET_ID,excelObject.getExcelId());
                            msgContent.put(MessageConst.CONTENT_IN_CLASS, excelObject.getInClassId());
                            msgContent.put(MessageConst.CONTENT_EXCEL_ANSWERS,excelObject.getAnswers());
                            msgContent.put(MessageConst.CONTENT_TIME, new Date().getTime());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        DataWrapper wrapper = new DataWrapper(
                                MainActivity.BASE_GLOBAL_DATA.getUserID(),
                                excelObject.getInClassId(),
                                "",
                                targetIds,
                                msgContent
                        );

                        int messageId = MessagePostUtil.INSTANCE.wannaPostMessage(wrapper);
                        Log.e("TAG", "messageId = " + messageId);
                        getContainerActivity().finish();
                    }
                } else {
                    Toast.makeText(getContainerActivity(), "进入修改模式~", Toast.LENGTH_LONG).show();
                    actionTvBtn.setText("上传");
                    forWhat = VIEW_TYPE.FOR_FILL;
                    adapter.notifyDataSetChanged();
                }
            }
        });

        String[] questions = excelObject.getQuestions().split("_");
        String[] answers = null;
        if (!excelObject.getAnswers().equals("")) {
            answers = excelObject.getAnswers().split("_");
        }

        items = new ArrayList<>();
        for (int i = 0; i < questions.length;i++) {
            items.add(new ItemObject(questions[i],answers == null ? "" : answers[i]));
        }

        adapter = new MyAdapter(getContainerActivity(),items);
        contentListView.setAdapter(adapter);
        return  view;
    }


}
