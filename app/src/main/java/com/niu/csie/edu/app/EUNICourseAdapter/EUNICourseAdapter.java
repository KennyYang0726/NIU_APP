package com.niu.csie.edu.app.EUNICourseAdapter;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.niu.csie.edu.app.Activity2_EUNI2;
import com.niu.csie.edu.app.R;


public class EUNICourseAdapter extends BaseAdapter {
    private Context context;
    private List<String> mainItems;
    private LayoutInflater inflater;
    private SharedPreferences sharedPreferences;
    private OnSubItemClickListener listener; // 介面實例
    private Map<Integer, Boolean> itemExpandedMap; // 記錄每個父項的展開狀態

    public EUNICourseAdapter(Context context, List<String> mainItems, OnSubItemClickListener listener) {
        this.context = context;
        this.mainItems = mainItems;
        this.listener = listener; // 初始化介面實例
        this.inflater = LayoutInflater.from(context);
        this.sharedPreferences = context.getSharedPreferences("EUNI_course_data", Context.MODE_PRIVATE);
        this.itemExpandedMap = new HashMap<>();
        initItemExpandedMap(); // 初始化展開狀態，預設都是false，即未展開
    }

    private void initItemExpandedMap() {
        for (int i = 0; i < mainItems.size(); i++) {
            itemExpandedMap.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return mainItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mainItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity2_euni_item_main, parent, false);
            holder = new ViewHolder();
            holder.textViewMain = convertView.findViewById(R.id.textViewMain);
            holder.subItemsContainer = convertView.findViewById(R.id.subItemsContainer);
            holder.mainItemLayout = convertView.findViewById(R.id.mainItemLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String courseName = sharedPreferences.getString("課程_" + position + "_名稱", "");
        holder.textViewMain.setText(courseName);

        // 清空之前的子項視圖
        holder.subItemsContainer.removeAllViews();

        // 設定子項內容
        String[] subItemTitles = {context.getString(R.string.EUNI_Sub_Item1), context.getString(R.string.EUNI_Sub_Item2), context.getString(R.string.EUNI_Sub_Item3), context.getString(R.string.EUNI_Sub_Item4), context.getString(R.string.EUNI_Sub_Item5)};
        for (int i = 0; i < subItemTitles.length; i++) {
            View subItemView = inflater.inflate(R.layout.activity2_euni_item_sub, holder.subItemsContainer, false);
            TextView textViewSub = subItemView.findViewById(R.id.textViewSub);
            textViewSub.setText(subItemTitles[i]);
            final int subItemIndex = i + 1;
            subItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String CourseName = sharedPreferences.getString("課程_" + position + "_名稱", null);
                    try {
                        CourseName = CourseName.split("_")[1];
                    } catch (Exception e) { // 可能像是我的 「甲1112電子電路實驗 二(B4ET0...」
                        Pattern pattern = Pattern.compile("\\d{4}(.*)");
                        Matcher matcher = pattern.matcher(CourseName);
                        if (matcher.find()) {
                            CourseName = matcher.group(1);
                        }
                    }
                    try {
                        CourseName = CourseName.split("\\(")[0];
                    } catch (Exception e) { // 可能名稱太長，後面沒有()，只有...
                    }
                    if (CourseName.length() > 8) { // 名稱 > 8 再刪減
                        CourseName = CourseName.substring(0,8) + "...";
                    }
                    String CourseID = sharedPreferences.getString("課程_" + position + "_ID", null);
                    String Choose = subItemTitles[subItemIndex-1]; // 公告、成績、資源、作業、進入課程
                    String Url_domain = "https://euni.niu.edu.tw/";
                    String Url_Final = "";
                    if (subItemIndex == 1) {
                        if (!sharedPreferences.contains("課程_" + position + "_公告ID")) {
                            // 加載課程公告 ID 並存入 sp，因為公告 ID = CourseID + 一個數值，每學期皆不同，這裡直接載入 webview 抓取
                            Toast.makeText(context, context.getString(R.string.EUNI_First_Loading_Tip), Toast.LENGTH_SHORT).show();
                            listener.onSubItemClick(Url_domain+"course/view.php?id="+CourseID, String.valueOf(position), CourseName);
                            //...
                        } else {
                            // 直接加載
                            Url_Final = Url_domain+"/mod/forum/view.php?id="+sharedPreferences.getString("課程_" + position + "_公告ID", null);
                        }
                    } else if (subItemIndex == 2) {
                        Url_Final = Url_domain+"grade/report/user/index.php?id="+CourseID;
                    } else if (subItemIndex == 3) {
                        Url_Final = Url_domain+"course/resources.php?id="+CourseID;
                    } else if (subItemIndex == 4) {
                        Url_Final = Url_domain+"mod/assign/index.php?id="+CourseID;
                    } else if (subItemIndex == 5) {
                        Url_Final = Url_domain+"course/view.php?id="+CourseID;
                    }
                    if (subItemIndex != 1 || sharedPreferences.contains("課程_" + position + "_公告ID")) {
                        Intent page = new Intent(context, Activity2_EUNI2.class);
                        page.putExtra("Url_Final", Url_Final);
                        page.putExtra("Title", CourseName + "-" + Choose);
                        //Toast.makeText(context, CourseName + "-" + Choose, Toast.LENGTH_SHORT).show();
                        context.startActivity(page);
                        ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
            });
            holder.subItemsContainer.addView(subItemView);
        }

        // 根據展開狀態設定子項目可見性
        if (itemExpandedMap.get(position)) {
            holder.subItemsContainer.setVisibility(View.VISIBLE);
        } else {
            holder.subItemsContainer.setVisibility(View.GONE);
        }

        holder.mainItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemExpandedMap.get(position)) {
                    collapse(holder.subItemsContainer);
                    itemExpandedMap.put(position, false);
                } else {
                    expand(holder.subItemsContainer);
                    itemExpandedMap.put(position, true);
                }
            }
        });

        return convertView;
    }

    private void expand(final View v) {
        v.setVisibility(View.VISIBLE);
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        ValueAnimator animator = slideAnimator(0, targetHeight, v);
        animator.start();
    }

    private void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        ValueAnimator animator = slideAnimator(initialHeight, 0, v);
        animator.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(android.animation.Animator animation) {}
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {}
            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {}
        });
        animator.start();
    }

    private ValueAnimator slideAnimator(int start, int end, final View v) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    static class ViewHolder {
        TextView textViewMain;
        LinearLayout subItemsContainer;
        LinearLayout mainItemLayout;
    }
}
