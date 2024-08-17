package com.niu.csie.edu.app.ScoreQuoteAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.graphics.Color;

import java.util.List;
import java.util.Objects;

import com.niu.csie.edu.app.R;
import com.niu.csie.edu.app.ScoreQuote.ScoreQuote;


public class ScoreQuoteAdapter extends BaseAdapter {
    private Context context;
    private List<ScoreQuote> grades;
    private LayoutInflater inflater;


    public ScoreQuoteAdapter(Context context, List<ScoreQuote> grades) {
        this.context = context;
        this.grades = grades;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return grades.size();
    }

    @Override
    public Object getItem(int position) {
        return grades.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity3_score_inquiry_term_result, parent, false);
            holder = new ViewHolder();
            holder.textViewMain = convertView.findViewById(R.id.textViewMain);
            holder.textScore = convertView.findViewById(R.id.text_score);
            holder.textElective = convertView.findViewById(R.id.text_Elective);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ScoreQuote grade = grades.get(position);
        holder.textViewMain.setText(grade.getLesson());
        // 多國語言用
        String Score = "";
        if (Objects.equals(grade.getScore(), "未上傳")) {
            Score = context.getResources().getString(R.string.Not_uploaded);
        } else {
            Score = grade.getScore();
        }

        holder.textScore.setText(context.getResources().getString(R.string.Score) + Score);
        // 多國語言用
        String Type = "";
        if (Objects.equals(grade.getType(), "必修")) {
            Type = context.getResources().getString(R.string.Obligatory);
        } else if (Objects.equals(grade.getType(), "選修")) {
            Type = context.getResources().getString(R.string.Elective);
        } else {
            Type = grade.getType();
        }
        holder.textElective.setText(Type);

        // 低於 60 改變文字顏色
        try {
            double score = Double.parseDouble(grade.getScore());
            if (score < 60) {
                holder.textScore.setTextColor(Color.RED);
            }
        } catch (Exception e) {
            // 未上傳
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textViewMain;
        TextView textScore;
        TextView textElective;
    }
}
