package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.ExcelModelObject;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class ModelSelectionFragment extends BaseFragment {

    private static final String TARGET = "param1";
    private static final String IS_POINT_TO_PERSON = "param2";

    private String target; //可能是通知组也有可能是个体联系人

    private boolean isPointToPerson;

    public static ModelSelectionFragment newInstance(String targetID,boolean isPointToPerson) {
        ModelSelectionFragment fragment = new ModelSelectionFragment();
        Bundle args = new Bundle();
        args.putString(TARGET, targetID);
        args.putBoolean(IS_POINT_TO_PERSON,isPointToPerson);
        fragment.setArguments(args);
        return fragment;
    }

    public ModelSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            target = getArguments().getString(TARGET);
            isPointToPerson = getArguments().getBoolean(IS_POINT_TO_PERSON);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_model_selection, container, false);
        ListView listView = (ListView) view.findViewById(R.id.fragment_model_selection_content);

        final List<ExcelModelObject> modelObjectList = DataSupport.findAll(ExcelModelObject.class);
        if (modelObjectList == null) {
            listView.setAdapter(new MyAdapter(this.getContainerActivity(), new ArrayList<ExcelModelObject>()));
        } else {
            listView.setAdapter(new MyAdapter(this.getContainerActivity(), modelObjectList));
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                assert modelObjectList != null;
                getContainerActivity().replaceFragment(CreateInfoCollectionFragment.newInstance(target, modelObjectList.get(position).getQuestions(), isPointToPerson), true);
            }
        });
        return view;
    }

    private class MyAdapter extends BaseAdapter {

        private List<ExcelModelObject> items;

        private Context context;

        public MyAdapter (Context context,List<ExcelModelObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ExcelModelObject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            public TextView nameTextView;

            public TextView contentTextView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            ExcelModelObject item = getItem(position);

            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_model_selection,null);
                viewHolder = new ViewHolder();
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.item_model_selection_name);
                viewHolder.contentTextView = (TextView) convertView.findViewById(R.id.item_model_selection_content);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.nameTextView.setText(item.getName());
            StringBuilder sb = new StringBuilder();
            String[] questionList = item.getQuestions().split("_");
            for (String question : questionList) {
                sb.append(question);
                sb.append(" ");
            }
            viewHolder.contentTextView.setText(sb.toString());
            return convertView;
        }
    }


    @Override
    void backAction() {
        if (!isPointToPerson) {
            Intent intent = new Intent(getContainerActivity(), MainActivity.class);
            startActivity(intent);
        } else {
            getContainerActivity().setResult(Activity.RESULT_CANCELED,null);
            getContainerActivity().finish();
        }
    }

    @Override
    String fragmentTitle() {
        return "选择模板";
    }

    @Override
    List<Integer> setActionViews() {
        List<Integer> action = new ArrayList<>();
        action.add(R.id.main_container_create_model);
        return action;
    }
}
