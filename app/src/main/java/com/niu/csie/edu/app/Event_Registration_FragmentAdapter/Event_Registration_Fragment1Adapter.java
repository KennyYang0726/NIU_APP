package com.niu.csie.edu.app.Event_Registration_FragmentAdapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.niu.csie.edu.app.R;


public class Event_Registration_Fragment1Adapter extends BaseAdapter {
    private Context context;
    private List<JSONObject> eventList;
    private LayoutInflater inflater;
    private Map<Integer, Boolean> itemExpandedMap; // 記錄每個父項的展開狀態
    private OnRegisterButtonClickListener listener;

    public Event_Registration_Fragment1Adapter(Context context, List<JSONObject> eventList, OnRegisterButtonClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
        inflater = LayoutInflater.from(context);
        itemExpandedMap = new HashMap<>(); // 初始化 itemExpandedMap
        initItemExpandedMap(); // 初始化展開狀態，預設都是 false，即未展開
    }

    public interface OnRegisterButtonClickListener {
        void onDetailButtonClick(String name, String ID, String time, String location, String detail, String department, String contactInfoName, String contactInfoTel, String contactInfoMail, String Related_links, String Remark, String Multi_factor_authentication, String eventRegisterTime);
        void onRegisterButtonClick(String eventID);
    }

    private void initItemExpandedMap() {
        for (int i = 0; i < eventList.size(); i++) {
            itemExpandedMap.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public Object getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.event_registration_customlistview, parent, false);
            holder = new ViewHolder();
            holder.textViewMain = convertView.findViewById(R.id.textViewMain);
            holder.textViewSub = convertView.findViewById(R.id.textViewSub);
            holder.textViewDetailProgress = convertView.findViewById(R.id.textView_Detail_Progress);
            holder.textViewDetailArrow = convertView.findViewById(R.id.textView_Detail_arrow);
            holder.linearDetail = convertView.findViewById(R.id.Linear_Detail);
            holder.textViewEventID = convertView.findViewById(R.id.textView_Event_ID);
            holder.textViewEventTime = convertView.findViewById(R.id.textView_Event_Time);
            holder.textViewEventLocation = convertView.findViewById(R.id.textView_Event_Location);
            holder.textViewEventRegisterTime = convertView.findViewById(R.id.textView_Event_RegisterTime);
            holder.textViewEventType = convertView.findViewById(R.id.textView_Event_Type);
            holder.textViewEventPeople = convertView.findViewById(R.id.textView_Event_People);
            holder.btnDetail = convertView.findViewById(R.id.btn_detail);
            holder.btnRegister = convertView.findViewById(R.id.btn_register);
            convertView.setTag(holder);

            // Initialize all Linear_Detail as gone (collapsed)
            holder.linearDetail.setVisibility(View.GONE);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind data
        JSONObject event = eventList.get(position);
        try {
            String state = event.getString("state");
            String name = event.getString("name");
            String ID = event.getString("eventSerialID");
            String time = event.getString("eventTime");
            String location = event.getString("eventLocation");
            String department = event.getString("department");


            String detail = event.getString("eventDetail");
            String contactInfoName = event.getString("contactInfoName");
            String contactInfoTel = event.getString("contactInfoTel");
            String contactInfoMail = event.getString("contactInfoMail");
            String Related_links = event.getString("Related_links");
            String Remark = event.getString("Remark");


            String Multi_factor_authentication = event.getString("Multi_factor_authentication");
            String eventRegisterTime = event.getString("eventRegisterTime");
            String people = event.getString("eventPeople");

            if ("報名中".equals(state)) {
                holder.textViewDetailProgress.setText(context.getString(R.string.Event_State_ok));
                holder.textViewDetailProgress.setTextColor(context.getResources().getColor(R.color.Event_OK));
                holder.textViewDetailProgress.setBackgroundResource(R.drawable.event_state_ok);
                holder.btnRegister.setVisibility(View.VISIBLE);
            } else if ("準備中".equals(state)) {
                holder.textViewDetailProgress.setText(context.getString(R.string.Event_State_notyet));
                holder.textViewDetailProgress.setTextColor(context.getResources().getColor(R.color.Event_NotYet));
                holder.textViewDetailProgress.setBackgroundResource(R.drawable.event_state_notyet);
                holder.btnRegister.setVisibility(View.GONE);
            } else {
                holder.textViewDetailProgress.setText(context.getString(R.string.Event_State_registerOver));
                holder.textViewDetailProgress.setTextColor(Color.RED);
                holder.textViewDetailProgress.setBackgroundResource(R.drawable.event_state_done);
                holder.btnRegister.setVisibility(View.GONE);
            }

            holder.textViewMain.setText(name);
            holder.textViewSub.setText(department);
            holder.textViewEventID.setText(ID);
            holder.textViewEventTime.setText(time);
            holder.textViewEventLocation.setText(location);
            holder.textViewEventRegisterTime.setText(eventRegisterTime);
            holder.textViewEventType.setText(Multi_factor_authentication);
            holder.textViewEventPeople.setText(people);

            holder.btnDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetailButtonClick(name, ID, time, location, detail, department, contactInfoName, contactInfoTel, contactInfoMail, Related_links, Remark, Multi_factor_authentication, eventRegisterTime);
                }

            });

            holder.btnRegister.setOnClickListener(v -> {
                if (listener != null && "報名中".equals(state)) {
                    listener.onRegisterButtonClick(holder.textViewEventID.getText().toString());
                }
            });

            // 根據展開狀態設定子項目可見性，以及設置 holder.textViewDetailArrow 箭頭方向
            if (itemExpandedMap.getOrDefault(position, false)) {
                holder.linearDetail.setVisibility(View.VISIBLE);
                holder.textViewDetailArrow.setText(context.getString(R.string.Event_State_Arrow_UP));
            } else {
                holder.linearDetail.setVisibility(View.GONE);
                holder.textViewDetailArrow.setText(context.getString(R.string.Event_State_Arrow_DOWN));
            }

            holder.textViewDetailArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemExpandedMap.getOrDefault(position, false)) {
                        collapse(holder.linearDetail);
                        itemExpandedMap.put(position, false);
                        holder.textViewDetailArrow.setText(context.getString(R.string.Event_State_Arrow_DOWN));
                    } else {
                        expand(holder.linearDetail);
                        itemExpandedMap.put(position, true);
                        holder.textViewDetailArrow.setText(context.getString(R.string.Event_State_Arrow_UP));
                    }

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private void expand(final View v) {
        v.setVisibility(View.VISIBLE);
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight()+31; // 某些 dpi 下會吃到，給他加 31

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

    private void showMessage(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }


    private static class ViewHolder {
        TextView textViewMain;
        TextView textViewSub;
        TextView textViewDetailProgress;
        TextView textViewDetailArrow;
        LinearLayout linearDetail;
        TextView textViewEventID;
        TextView textViewEventTime;
        TextView textViewEventLocation;
        TextView textViewEventRegisterTime;
        TextView textViewEventType;
        TextView textViewEventPeople;
        Button btnDetail;
        Button btnRegister;
    }
}

