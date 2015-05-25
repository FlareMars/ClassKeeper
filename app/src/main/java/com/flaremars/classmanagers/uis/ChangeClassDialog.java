package com.flaremars.classmanagers.uis;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.ClassObject;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

//切换班级对话框
public class ChangeClassDialog extends DialogFragment {
    private static ChangeClassDialog instance;

    private TextView headerTextView;

    private String headerTitle;

    private MyAdapter adapter;

    private OnConfirmClickListener tempListener;

    public ChangeClassDialog() {

    }

    public static ChangeClassDialog getClassDialog(String title) {
        if (instance == null) {
            instance = new ChangeClassDialog();
            instance.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        }
        instance.headerTitle = title;
        if (instance.headerTextView != null) {
            instance.headerTextView.setText(title);
        }
        return instance;
    }

    public void setOnConfirmClickListener(OnConfirmClickListener listener) {
        tempListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_class_dialog, container, false);
        ListView listView = (ListView) view.findViewById(R.id.lv_change_class_dialog_content);
        headerTextView = (TextView) view.findViewById(R.id.tv_classlist_header);
        headerTextView.setText(headerTitle);
        listView.setDivider(null);
        List<ClassObject> items = DataSupport.findAll(ClassObject.class);
        final List<ClassObject> targetObjects = new ArrayList<>();
        for (ClassObject temp : items) {
            if (temp.getMemberIds().contains(MainActivity.BASE_GLOBAL_DATA.getUserID()) &&
                    !temp.getClassID().equals(MainActivity.BASE_GLOBAL_DATA.getCurClassID())) {
                targetObjects.add(temp);
            }
        }
        adapter = new MyAdapter(getActivity(),targetObjects);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tempListener.onConfirmClick(targetObjects.get(position));
                instance.dismiss();
            }
        });
        return view;
    }


    public interface OnConfirmClickListener {
        void onConfirmClick(ClassObject item);
    }

    private class MyAdapter extends BaseAdapter {

        private Context context;

        private List<ClassObject> items;

        public MyAdapter(Context context, List<ClassObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ClassObject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        private class ViewHolder {
            public TextView nameTextView;

            public ImageView confirmBtn;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ClassObject item = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(context, R.layout.item_change_class, null);
                viewHolder.confirmBtn = (ImageView) convertView.findViewById(R.id.btn_item_change_class_confirm);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.tv_item_change_class_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.nameTextView.setText(item.getName());
            return convertView;
        }
    }
}