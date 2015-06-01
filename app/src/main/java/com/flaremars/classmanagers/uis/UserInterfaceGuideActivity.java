package com.flaremars.classmanagers.uis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.flaremars.classmanagers.R;
import com.flaremars.classmanagers.mainui.BeforeMainActivity;
import com.flaremars.classmanagers.model.AppConst;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import java.util.ArrayList;
import java.util.List;

public class UserInterfaceGuideActivity extends FragmentActivity {

    private static final int [] GUIDE_PAGE_IDS = {R.drawable.guide_page_one,R.drawable.guide_page_two,R.drawable.guide_page_three};

    private static final int [] INDICATOR_IDS = {R.drawable.ic_indicator_one,R.drawable.ic_indicator_two,R.drawable.ic_indicator_three};

    private static final String [] BIG_TITLES = {"公告提醒","表格录制","通知已读回执"};

    private static final String [] SECOND_TITLES = {"简洁明了 轻松获取班级动态","一键制表 方便快捷",
            "已读未读一目了然\n确保重要消息的传达 一个都不能少"};

    private List<View> imageViewList = new ArrayList<>();

    private List<TextView> bigTitleList = new ArrayList<>();

    private List<TextView> secondTitleList = new ArrayList<>();

    private List<ImageView> mainContentList = new ArrayList<>();

    private List<ImageView> magnifierList = new ArrayList<>();

    private int curSelected = 0;

    private ViewPager contentViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface_guide);

        final ImageView indicatorIv = (ImageView) findViewById(R.id.iv_pages_indicator);
        contentViewPager = (ViewPager) findViewById(R.id.vp_content);

        magnifierList.add((ImageView)findViewById(R.id.magnifier_one));
        magnifierList.add((ImageView)findViewById(R.id.magnifier_two));
        magnifierList.add((ImageView)findViewById(R.id.magnifier_three));

        for (ImageView temp : magnifierList) {
            temp.setVisibility(View.INVISIBLE);
            temp.setScaleX(0.1f);
            temp.setScaleY(0.1f);
        }
        magnifierList.get(0).setVisibility(View.VISIBLE);
        magnifierList.get(0).setScaleX(1.5f);
        magnifierList.get(0).setScaleY(1.5f);

        View page1 = View.inflate(this,R.layout.layout_guide_page,null);
        View page2 = View.inflate(this,R.layout.layout_guide_page,null);
        View page3 = View.inflate(this,R.layout.layout_guide_page,null);

        imageViewList.add(page1);
        imageViewList.add(page2);
        imageViewList.add(page3);
        for (int i = 0;i < 3;i++) {
            View temp = imageViewList.get(i);
            TextView bigTitleTv = (TextView) temp.findViewById(R.id.tv_big_title);
            TextView secondTitleTv = (TextView) temp.findViewById(R.id.tv_second_title);
            ImageView mainContent = (ImageView) temp.findViewById(R.id.main_content);
            ImageView endGuideBtn = (ImageView) temp.findViewById(R.id.iv_btn_end_guide);
            if (i == 2) {
                endGuideBtn.setVisibility(View.VISIBLE);
                endGuideBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        endGuide();
                    }
                });
            }

            bigTitleTv.setText(BIG_TITLES[i]);
            secondTitleTv.setText(SECOND_TITLES[i]);
            mainContent.setImageResource(GUIDE_PAGE_IDS[i]);

            bigTitleList.add(bigTitleTv);
            secondTitleList.add(secondTitleTv);
            mainContentList.add(mainContent);
        }

        contentViewPager.setAdapter(new GuidePageAdapter(imageViewList));

        contentViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e("TAG",positionOffsetPixels + "");
            }

            @Override
            public void onPageSelected(int position) {
                curSelected = position;
                indicatorIv.setImageResource(INDICATOR_IDS[position]);

//                for (ImageView temp : magnifierList) {
//                    temp.setVisibility(View.INVISIBLE);
//                    temp.setScaleX(0.1f);
//                    temp.setScaleY(0.1f);
//                }
//                magnifierList.get(position).animate().
//                        scaleX(1.4f).
//                        scaleY(1.4f).
//                        setDuration(1000).
//                        setInterpolator(new OvershootInterpolator()).
//                        start();
//                magnifierList.get(position).setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                magnifierList.get(curSelected).setVisibility(View.INVISIBLE);
                if (state == ScrollState.STOP.ordinal()) {
                    for (ImageView temp : magnifierList) {
                        temp.setVisibility(View.INVISIBLE);
                        temp.setScaleX(0.1f);
                        temp.setScaleY(0.1f);
                    }
                    magnifierList.get(curSelected).setVisibility(View.VISIBLE);
                    magnifierList.get(curSelected).animate().
                            scaleX(1.4f).
                            scaleY(1.4f).
                            setDuration(500).
                            setInterpolator(new OvershootInterpolator()).
                            start();
                }
            }
        });
    }

    private void endGuide() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConst.SHARE_PREFERENCE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("start_up_count",sharedPreferences.getInt("start_up_count",0) + 1);
        editor.apply();

        Intent intent = new Intent(this, BeforeMainActivity.class);
        startActivity(intent);
    }

    private class GuidePageAdapter extends PagerAdapter {
        List<View> items;

        public GuidePageAdapter(List<View> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            container.removeView(items.get(position));

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(items.get(position));
            return items.get(position);
        }
    }
}
