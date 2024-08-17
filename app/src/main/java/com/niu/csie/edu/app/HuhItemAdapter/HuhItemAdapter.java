package com.niu.csie.edu.app.HuhItemAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import com.niu.csie.edu.app.Activity1_HomeActivity;
import com.niu.csie.edu.app.HuhItem.HuhItem;
import com.niu.csie.edu.app.R;


public class HuhItemAdapter extends BaseAdapter {
    private Context context;
    private List<HuhItem> itemList;
    private LayoutInflater inflater;

    public HuhItemAdapter(Context context, List<HuhItem> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.huh_customlistview, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.textViewMain = convertView.findViewById(R.id.textViewMain);
            holder.textViewSub = convertView.findViewById(R.id.textViewSub);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HuhItem item = itemList.get(position);
        holder.imageView.setImageResource(item.getImageResId());
        holder.textViewMain.setText(item.getMainText());
        holder.textViewSub.setText(item.getSubText());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof Activity1_HomeActivity) {
                    ((Activity1_HomeActivity) context).onDirectionsItemClick(position);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView textViewMain;
        TextView textViewSub;
    }
}

