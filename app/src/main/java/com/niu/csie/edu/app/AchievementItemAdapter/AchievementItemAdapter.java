package com.niu.csie.edu.app.AchievementItemAdapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.niu.csie.edu.app.AchievementItem.AchievementItem;
import com.niu.csie.edu.app.Activity1_HomeActivity;
import com.niu.csie.edu.app.R;



public class AchievementItemAdapter extends BaseAdapter {

    private Context context;
    private List<AchievementItem> achievements;

    public AchievementItemAdapter(Context context, List<AchievementItem> achievements) {
        this.context = context;
        this.achievements = achievements;
    }

    @Override
    public int getCount() {
        return achievements.size();
    }

    @Override
    public Object getItem(int position) {
        return achievements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.achievements_customlistview, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.title = convertView.findViewById(R.id.textView_Title);
            holder.description = convertView.findViewById(R.id.textView_Description);
            holder.finished = convertView.findViewById(R.id.imageView2);

            // 確保這些控制不會搶佔焦點
            holder.imageView.setFocusable(false);
            holder.imageView.setFocusableInTouchMode(false);
            holder.title.setFocusable(false);
            holder.title.setFocusableInTouchMode(false);
            holder.description.setFocusable(false);
            holder.description.setFocusableInTouchMode(false);
            holder.finished.setFocusable(false);
            holder.finished.setFocusableInTouchMode(false);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AchievementItem achievement = achievements.get(position);
        holder.imageView.setImageResource(achievement.getImageResId());
        holder.title.setText(achievement.getTitle());
        holder.description.setText(achievement.getDescription());
        holder.finished.setImageResource(achievement.getImageResultResId());

        // 每次 getView 被呼叫時，都重新設定點擊監聽器
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("AchievementItemAdapter", "Item clicked at position: " + position);
                if (context instanceof Activity1_HomeActivity) {
                    ((Activity1_HomeActivity) context).onAchievementItemClick(position);
                }
            }
        });

        return convertView;
    }

    public void updateImageResult(int position, int newImageResultResId) {
        AchievementItem achievement = achievements.get(position);
        achievement.setImageResultResId(newImageResultResId);
        notifyDataSetChanged(); // 刷新
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView title;
        TextView description;
        ImageView finished;
    }
}


