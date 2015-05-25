package com.flaremars.classmanagers.uis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.*;
import com.flaremars.classmanagers.model.ExcelTaskObject;
import com.flaremars.classmanagers.model.NoticeGroupObject;
import com.flaremars.classmanagers.model.NoticeTaskObject;
import com.flaremars.classmanagers.model.AppConst;
import com.flaremars.classmanagers.model.FragmentConst;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeGroupLayout extends FrameLayout{

    private static final SimpleDateFormat DATE_FAMATOR = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);

    private ImageView noticeIvBtn;

    private ImageView excelIvBtn;

    private ObservableListView contentListView;

    private int headerHeight = 115;

    private int lastListViewScroll = 0;

    private boolean isHeaderMoving = false;

    private boolean isInputLayoutShow = false;

    private List<ItemObject> items;

    private MyAdapter adapter;

    private NoticeGroupObject noticeGroupObject;

    public NoticeGroupLayout(final Context context, AttributeSet attrs) {
        super(context, attrs);

        final View headerView = View.inflate(context,R.layout.content_header_notice_group,null);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        contentListView = new ObservableListView(context);
        contentListView.setDivider(null);
        contentListView.setLayoutParams(params);

        headerHeight = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, headerHeight, getResources()
                        .getDisplayMetrics()));

        items = new ArrayList<>();
        View paddingView = new View(context);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                headerHeight);
        paddingView.setLayoutParams(lp);
        paddingView.setClickable(true);
        paddingView.setBackgroundColor(Color.TRANSPARENT);
        contentListView.addHeaderView(paddingView);

        adapter = new MyAdapter(context,items);
        contentListView.setAdapter(adapter);
        contentListView.setVerticalScrollBarEnabled(false);
        contentListView.setScrollViewCallbacks(new ObservableScrollViewCallbacks() {
            @Override
            public void onScrollChanged(int i, boolean b, boolean b1) {
//                ViewHelper.setTranslationY(headerView,-i);
                int curScroll = i;

                if (isInputLayoutShow && Math.abs(curScroll - lastListViewScroll) >= 5) {
                    ((NoticeGroupActivity) context).setInputLayoutVisible(false);
                    isInputLayoutShow = false;
                }

                if (isHeaderMoving) {
                    return;
                }
                if (headerView.getTranslationY() == 0 && curScroll > lastListViewScroll) {
                    //内容向上滑动
                    ObjectAnimator animator = ObjectAnimator.ofFloat(headerView, "translationY", 0, -headerHeight).setDuration(200);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            isHeaderMoving = false;
                        }
                    });
                    animator.start();
                } else {
                    //内容向下滑动
                    if (headerView.getTranslationY() == -headerHeight && curScroll < lastListViewScroll) {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(headerView, "translationY", -headerHeight, 0).setDuration(200);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                isHeaderMoving = false;
                            }
                        });
                        animator.start();
                    }
                }

                lastListViewScroll = curScroll;
            }

            @Override
            public void onDownMotionEvent() {

            }

            @Override
            public void onUpOrCancelMotionEvent(ScrollState scrollState) {

            }
        });

        contentListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    closeSoftInput();
                }
                return false;
            }
        });

        noticeIvBtn = (ImageView) headerView.findViewById(R.id.iv_btn_notice_group_ui_notice);
        excelIvBtn = (ImageView) headerView.findViewById(R.id.iv_btn_notice_group_ui_excel);

        noticeIvBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((com.flaremars.classmanagers.mainui.NoticeGroupActivity) context).setInputLayoutVisible(true);
                isInputLayoutShow = true;
            }
        });

        excelIvBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((com.flaremars.classmanagers.mainui.NoticeGroupActivity) context).sendExcelModel();
            }
        });

        addView(contentListView);
        addView(headerView);

    }

    public void setNoticeGroupObject(NoticeGroupObject noticeGroupObject) {
        this.noticeGroupObject = noticeGroupObject;

        List<ExcelTaskObject> excelObjectList = DataSupport.where("noticegroupobject_id=?",String.valueOf(noticeGroupObject.getId())).find(ExcelTaskObject.class);
        List<NoticeTaskObject> noticeObjectList = DataSupport.where("noticegroupobject_id=?",String.valueOf(noticeGroupObject.getId())).find(NoticeTaskObject.class);
        for (ExcelTaskObject excelObject : excelObjectList) {
            items.add(new ItemObject(excelObject.getName(),R.mipmap.ic_excel_mid, excelObject.isNewFeedback(),
                    excelObject.getTime(),excelObject.getId()));
        }
        for (NoticeTaskObject noticeObject : noticeObjectList) {
            items.add(new ItemObject(noticeObject.getContent(),0, noticeObject.isNewFeedback(),
                    noticeObject.getTime(),noticeObject.getId()));
        }
        Collections.sort(items);
        adapter.notifyDataSetChanged();
    }

    private void closeSoftInput() {
        //关闭输入法
        View view = ((Activity)getContext()).getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setSignForInputLayout() {
        isInputLayoutShow = true;
    }

    public void addItem(String content,int picID,Date time,long targetId) {
        closeSoftInput();
        items.add(0,new ItemObject(content, picID,false,time,targetId));
        adapter.notifyDataSetChanged();
    }

    private class ItemObject implements Comparable<ItemObject>{
        private String text;

        private Integer picId;

        private Date time;

        private long targetID;

        private boolean newFeedback;

        public ItemObject(String text, Integer picId,boolean newFeedback ,Date time,long targetID) {
            this.text = text;
            this.picId = picId;
            this.time = time;
            this.targetID = targetID;
            this.newFeedback = newFeedback;
        }

        public long getTargetID() {
            return targetID;
        }

        public void setTargetID(long targetID) {
            this.targetID = targetID;
        }

        public String getText() {
            return text;
        }

        public Integer getPicId() {
            return picId;
        }

        public Date getTime() {
            return time;
        }

        public boolean isNewFeedback() {
            return newFeedback;
        }

        @Override
        public int compareTo(ItemObject another) {
            if (this.newFeedback && !another.newFeedback) {
                return -1;
            }
            if (!this.newFeedback && another.newFeedback) {
                return 1;
            }
            return -this.time.compareTo(another.getTime());
        }
    }

    private class MyAdapter extends BaseAdapter{

        private Context context;

        private List<ItemObject> items;

        public MyAdapter(Context context, List<ItemObject> items) {
            this.context = context;
            this.items = items;
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
            public ImageView picImageView;

            public TextView infoTextView;

            public ImageView detailIvBtn;

            public TextView timeTextView;

            public LinearLayout containerBg;

            public LinearLayout contentContainer;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            final ItemObject item = getItem(position);
            //0代表通知，1代表信息录制
            final int type = item.getPicId() == 0 ? 0 : 1;
            if (convertView == null) {
                convertView = View.inflate(context,R.layout.item_notice_group,null);
                holder = new ViewHolder();
                holder.detailIvBtn = (ImageView) convertView.findViewById(R.id.iv_btn_notice_group_ui_detail);
                holder.infoTextView = (TextView) convertView.findViewById(R.id.tv_notice_group_ui_info);
                holder.picImageView = (ImageView) convertView.findViewById(R.id.iv_notice_group_ui_pic);
                holder.timeTextView = (TextView) convertView.findViewById(R.id.tv_notice_group_ui_time);
                holder.containerBg = (LinearLayout) convertView.findViewById(R.id.rl_notice_group_ui_container);
                holder.contentContainer = (LinearLayout) convertView.findViewById(R.id.mark_notice_group_ll);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (item.getPicId() != 0) {

                holder.picImageView.setVisibility(View.VISIBLE);
                holder.picImageView.setImageResource(item.getPicId());
            } else {
                holder.picImageView.setVisibility(View.GONE);
                holder.picImageView.setImageBitmap(null);
            }

            holder.infoTextView.setText(item.getText());
            holder.detailIvBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (type == 1) {
                        ExcelTaskObject excelTaskObject = DataSupport.find(ExcelTaskObject.class, item.getTargetID());
                        excelTaskObject.setToDefault("newFeedback");
                        excelTaskObject.update(excelTaskObject.getId());
                    } else {
                        NoticeTaskObject noticeTaskObject = DataSupport.find(NoticeTaskObject.class, item.getTargetID());
                        noticeTaskObject.setToDefault("newFeedback");
                        noticeTaskObject.update(noticeTaskObject.getId());
                    }
                    Collections.sort(items);
                    adapter.notifyDataSetChanged();
                    Intent intent = new Intent(context, ContainerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(AppConst.TARGET_FRAGMENT, FragmentConst.FRAGMENT_FEEDBACK_DETAIL);
                    bundle.putInt("target_object_id", (int) item.getTargetID());
                    bundle.putString("target_name", "“" + item.getText() + "” " + (type == 0 ? "已读情况" : "填表情况"));
                    bundle.putInt("target_type", type);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

            holder.contentContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.detailIvBtn.performClick();
                }
            });

            if (type == 0) {
                NoticeTaskObject noticeTaskObject = DataSupport.find(NoticeTaskObject.class,item.getTargetID());
                if (noticeTaskObject.isNewFeedback()) {
//                    holder.containerBg.setBackgroundColor(Color.argb(20,255,0,0));
                    holder.detailIvBtn.setVisibility(VISIBLE);
                } else {
//                    holder.containerBg.setBackgroundColor(Color.parseColor("#e4e2e3"));
                    holder.detailIvBtn.setVisibility(GONE);
                }
            } else {
                ExcelTaskObject excelTaskObject = DataSupport.find(ExcelTaskObject.class,item.getTargetID());
                if (excelTaskObject.isNewFeedback()) {
//                    holder.containerBg.setBackgroundColor(Color.argb(20,255,0,0));
                    holder.detailIvBtn.setVisibility(VISIBLE);
                } else {
//                    holder.containerBg.setBackgroundColor(Color.parseColor("#e4e2e3"));
                    holder.detailIvBtn.setVisibility(GONE);
                }
            }
            holder.timeTextView.setText(DATE_FAMATOR.format(item.getTime()));
            return convertView;
        }
    }
}
