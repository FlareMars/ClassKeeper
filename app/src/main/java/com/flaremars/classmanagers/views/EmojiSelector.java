package com.flaremars.classmanagers.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.flaremars.classmanagers.R;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 表情选择器
 */
public class EmojiSelector extends LinearLayout {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("000");

    //todo 记得增加一张表情就要修改最大值
    private static final int EMOJI_MAX_SIZE = 40;

//    private OnEmojiSelectedListener listener;

    private EditText binderEditText;

    private Context context;

    private int curPager = 0;

    private ViewPager emojiViewPager;

    private ImageView locator;

    private List<Integer> LOCATOR_IMAGES;

    private static int EMOJI_SIZE = 0;

    public EmojiSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public void setBinder(EditText target) {
        binderEditText = target;
    }

    private void initView() {
        //两个子控件，一个是ViewPager，一个是定位指示器
        setOrientation(LinearLayout.VERTICAL);
        emojiViewPager = new ViewPager(context);
        locator = new ImageView(context);

        //位置指示器的高度10dp
        int locatorHeight = 10;
        float scale = context.getResources().getDisplayMetrics().density;
        locatorHeight = (int)(locatorHeight * scale + 0.5f);
        LinearLayout.LayoutParams locatorLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,locatorHeight);
        locator.setLayoutParams(locatorLayoutParams);
        locator.setImageResource(R.drawable.emoji_locator1);

        //每个小表情所占的区域大小
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        EMOJI_SIZE = screenWidth / 7;

        //ViewPager的高度
//        int emojiViewPagerHeight = 130;
//        emojiViewPagerHeight = (int)(emojiViewPagerHeight * scale + 0.5f);
        locatorLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,EMOJI_SIZE * 3);
        emojiViewPager.setLayoutParams(locatorLayoutParams);

        //只使用一张图片作为选择页面，通过计算当前页面位置和点击的位置确定选择的表情
        //页面图片应该是一张9patch图片，只允许边缘位置被拉伸，而且需要做多种分辨率
        //todo 修改位置2 添加页面
        List<ImageView> emojiPagers = new ArrayList<>();
        ImageView page1 = new ImageView(context);
        locatorLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,EMOJI_SIZE * 3);
        page1.setLayoutParams(locatorLayoutParams);
        page1.setImageResource(R.drawable.emojis_page1_new);


        ImageView page2 = new ImageView(context);
        locatorLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,EMOJI_SIZE * 3);
        page2.setLayoutParams(locatorLayoutParams);
        page2.setImageResource(R.drawable.emojis_page2_new);

        emojiPagers.add(page1);
        emojiPagers.add(page2);

        //添加指示器的切换图片
        //todo 日后修改位置1
        LOCATOR_IMAGES  = new ArrayList<>();
        LOCATOR_IMAGES.add(R.drawable.emoji_locator1);
        LOCATOR_IMAGES.add(R.drawable.emoji_locator2);

        emojiViewPager.setOnPageChangeListener(new ViewPagerSelectedListener());
        emojiViewPager.setAdapter(new EmojiViewPagerAdapter(emojiPagers));
        emojiViewPager.setOnTouchListener(new ViewPagerOnTouchListner());

        addView(emojiViewPager);
        addView(locator);

        //todo 表情的格式要规定好
    }

    private class ViewPagerSelectedListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            locator.setImageResource(LOCATOR_IMAGES.get(i));
            curPager = i;
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    private int downPagerIndex;

    private int firstSelectedRow;

    private int firstSelectedCol;

    private class ViewPagerOnTouchListner implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downPagerIndex = curPager;
                    firstSelectedRow = (int)(event.getY() / EMOJI_SIZE);
                    firstSelectedCol = (int)(event.getX() / EMOJI_SIZE);
                    break;
                case MotionEvent.ACTION_UP:
                    int upPagerIndex = curPager;
                    int upSelectedRow = (int)(event.getY() / EMOJI_SIZE);
                    int upSelectedCol = (int)(event.getX() / EMOJI_SIZE);

                    if (downPagerIndex == upPagerIndex &&
                            firstSelectedRow == upSelectedRow &&
                            firstSelectedCol == upSelectedCol) {
                        int selectedIndex = upSelectedRow * 7 + upSelectedCol;
                        if (selectedIndex != 0 && (selectedIndex % 20) == 0) {
                            //后退键
                            onEmojiSelected("000");
                        } else {
                            int selectedEmoji = curPager * 20 + selectedIndex + 1;
                            if (selectedEmoji >= 1 && selectedEmoji <= EMOJI_MAX_SIZE) {
                                onEmojiSelected(DECIMAL_FORMAT.format(selectedEmoji));
                            }
                        }
                    }
                    break;
                default:
            }
            return false;
        }
    }

    private void onEmojiSelected(String emojiResource) {
        if (!emojiResource.equals("000")) {
            Field field = null;
            try {
                String targetField = "emoji" + emojiResource;
                field = R.mipmap.class.getDeclaredField(targetField);
                int resourceId = 0;
                try {
                    resourceId = Integer.parseInt(field.get(null).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
                ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
                SpannableString spannableString = new SpannableString("[" + targetField); //格式[emojiXXX
                spannableString.setSpan(imageSpan, 0, 1 + targetField.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                int selectionStart = binderEditText.getSelectionStart();
                binderEditText.getEditableText().insert(selectionStart,spannableString);

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            int selectionStart = binderEditText.getSelectionStart();// 获取光标的位置
            if (selectionStart > 0) {
                String body = binderEditText.getText().toString();
                if (!TextUtils.isEmpty(body)) {
                    if (selectionStart < 9) {
                        //如果字符不够9个，肯定没有表情，直接回退一格
                        binderEditText.getEditableText().delete(selectionStart - 1,
                                selectionStart);
                    } else {
                        String tempStr = body.substring(selectionStart - 9, selectionStart);
                        if (tempStr.substring(0,6).equals("[emoji")) {
                            binderEditText.getEditableText().delete(selectionStart - 9, selectionStart);
                        } else {
                            binderEditText.getEditableText().delete(selectionStart - 1, selectionStart);
                        }
                    }
                }
            }
        }
    }

    private class EmojiViewPagerAdapter extends PagerAdapter {

        private List<ImageView> emojiPagers;

        public EmojiViewPagerAdapter(List<ImageView> emojiPagers) {
            this.emojiPagers = emojiPagers;
        }

        @Override
        public int getCount() {
            return emojiPagers.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView(emojiPagers.get(position));
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            container.addView(emojiPagers.get(position), 0);
            return emojiPagers.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }
    }

    /*
    public void setOnEmojiSelectedListener (OnEmojiSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnEmojiSelectedListener {
        void onEmojiSelected(String emojiResource);
    }
    */
}
