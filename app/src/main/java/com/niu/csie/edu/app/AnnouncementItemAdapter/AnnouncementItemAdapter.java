package com.niu.csie.edu.app.AnnouncementItemAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import com.niu.csie.edu.app.Activity1_HomeActivity;
import com.niu.csie.edu.app.AnnouncementItem.AnnouncementItem;
import com.niu.csie.edu.app.R;



public class AnnouncementItemAdapter extends ArrayAdapter<AnnouncementItem> {

    private Context context;
    private int resource;
    private List<AnnouncementItem> items;

    public AnnouncementItemAdapter(@NonNull Context context, int resource, @NonNull List<AnnouncementItem> items) {
        super(context, resource, items);
        this.context = context;
        this.resource = resource;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(resource, null);
        }

        AnnouncementItem item = items.get(position);

        TextView textViewMain = view.findViewById(R.id.textViewMain);
        TextView textViewDate = view.findViewById(R.id.textViewDate);

        textViewMain.setText(item.getTitle());
        textViewDate.setText(item.getDate());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof Activity1_HomeActivity) {
                    ((Activity1_HomeActivity) context).onAnnouncementItemClick(position);
                }
            }
        });

        return view;
    }
}

