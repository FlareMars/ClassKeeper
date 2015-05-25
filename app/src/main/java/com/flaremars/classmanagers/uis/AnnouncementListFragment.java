package com.flaremars.classmanagers.uis;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.GetCallback;
import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.MainActivity;
import com.flaremars.classmanagers.model.AnnouncementObject;
import com.flaremars.classmanagers.model.ClassObject;
import com.flaremars.classmanagers.utils.NormalUtils;
import com.flaremars.classmanagers.views.ContextMenuActionForAnnouncement;
import com.flaremars.classmanagers.views.ContextMenuDialog;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnnouncementListFragment extends BaseFragment {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);

    private static final String TARGET = "param1";
    private static final String SELECTED_ANNOUNCEMENT = "param2";

    private int selected;

    private ClassObject classObject;

    private AnnouncementAdapter adapter;

    public static AnnouncementListFragment newInstance(String targetClassID, int selected) {
        AnnouncementListFragment fragment = new AnnouncementListFragment();
        Bundle args = new Bundle();
        args.putString(TARGET, targetClassID);
        args.putInt(SELECTED_ANNOUNCEMENT, selected);
        fragment.setArguments(args);
        return fragment;
    }

    public AnnouncementListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String targetClassID = getArguments().getString(TARGET);
            selected = getArguments().getInt(SELECTED_ANNOUNCEMENT);
            classObject = DataSupport.where("classID=?",targetClassID).find(ClassObject.class).get(0);
        }
    }

    @Override
    void backAction() {
        if (!getContainerActivity().getSupportFragmentManager().popBackStackImmediate()) {
            Intent intent = new Intent(getContainerActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    String fragmentTitle() {
        return "公告列表";
    }

    @Override
    List<Integer> setActionViews() {
        List<Integer> actions = new ArrayList<>();
        actions.add(R.id.main_container_create_announcement);
        return actions;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcement_list, container, false);

        final List<AnnouncementObject> list = DataSupport.where("classobject_id=? and networkId is not null", String.valueOf(classObject.getId())).find(AnnouncementObject.class);
        ListView contentListView = (ListView) view.findViewById(R.id.fragment_announcement_list_content);

        adapter = new AnnouncementAdapter(this.getContainerActivity(), list);
        contentListView.setAdapter(adapter);
        contentListView.setDivider(null);

        //寻找id对应的list位置
        int position = 0;
        int size = list.size();
        for (int i = 0;i < size;i++) {
            if (list.get(i).getId() == selected) {
                position = i;
                break;
            }
        }
        contentListView.setSelection(position);

        contentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final AnnouncementObject object = list.get(position);
                final String content = object.getContent();
                final String title = object.getTitle();

                ContextMenuActionForAnnouncement.getFunctionDialog("复制公告", new ContextMenuActionForAnnouncement.OnActionClickListener() {
                    @Override
                    public void onActionClick() {
                        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("announcement_content",
                                "【" + title + "】" + content));
                        NormalUtils.INSTANCE.showToast(getContainerActivity(), "内容复制到剪贴板");
                    }
                }, new ContextMenuDialog.OnDeleteClickListener() {
                    @Override
                    public void onDeleteClick() {
                        if (!NormalUtils.INSTANCE.isCreatorOrManager(classObject)) {
                            NormalUtils.INSTANCE.showToast(getContainerActivity(), "权限不足，不能进行删除操作");
                            return;
                        }
                        //删除网络公告数据
                        AVQuery<AVObject> query = new AVQuery<>("CMAnnouncement");
                        query.getInBackground(object.getNetworkId(), new GetCallback<AVObject>() {
                            @Override
                            public void done(AVObject avObject, AVException e) {
                                if (e == null) {
                                    avObject.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(AVException e) {
                                            //删除本地公告数据
                                            DataSupport.delete(AnnouncementObject.class, object.getId());
                                            list.remove(object);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                } else {
                                    NormalUtils.INSTANCE.showError(getContainerActivity(), e);
                                }
                            }
                        });
                    }
                }).show(getContainerActivity().getSupportFragmentManager(), "action_dialog");

                return false;
            }
        });

        return view;
    }

    private class AnnouncementAdapter extends BaseAdapter {

        private List<AnnouncementObject> items;

        private Context context;

        public AnnouncementAdapter (Context context ,List<AnnouncementObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position).getId() != selected) {
                return 0; //普通背景
            } else {
                return 1; //强调背景
            }
        }

        @Override
        public AnnouncementObject getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            public TextView title;

            public TextView content;

            public TextView baseInfo;

            public FrameLayout mainBackGround;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AnnouncementObject item = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.item_announcement,null);
                viewHolder = new ViewHolder();
                viewHolder.baseInfo = (TextView) convertView.findViewById(R.id.item_announcement_base_info);
                viewHolder.title = (TextView) convertView.findViewById(R.id.item_announcement_title);
                viewHolder.content = (TextView) convertView.findViewById(R.id.item_announcement_content);
                viewHolder.mainBackGround = (FrameLayout) convertView.findViewById(R.id.item_announcement_main_container);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.content.setText(item.getContent());
            viewHolder.title.setText(item.getTitle());
            viewHolder.baseInfo.setText(item.getNameOfSender() + " 有效至 " + DATE_FORMAT.format(item.getDeadline()));

            if (item.getId() == selected) {
                viewHolder.mainBackGround.setBackgroundResource(R.drawable.announcement_selected_bg);
            } else {
                viewHolder.mainBackGround.setBackgroundResource(R.drawable.announcement_normal_bg);
            }
            return convertView;
        }
    }
}
