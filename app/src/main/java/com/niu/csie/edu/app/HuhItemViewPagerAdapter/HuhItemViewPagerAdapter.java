package com.niu.csie.edu.app.HuhItemViewPagerAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.niu.csie.edu.app.R;



public class HuhItemViewPagerAdapter extends PagerAdapter {

    private final Context context;
    private final int position;

    public HuhItemViewPagerAdapter(Context context, int position) {
        this.context = context;
        this.position = position;
    }

    @Override
    public int getCount() {
        return getPageCountForPosition(position); // 返回頁面的數量
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int pagePosition) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity1_huhviewpagerlayout, container, false);

        TextView titleTextView = view.findViewById(R.id.textView_Title);
        ImageView imageView =  view.findViewById(R.id.imageView);
        TextView messageTextView = view.findViewById(R.id.textView_message);

        switch (position) {
            case 0:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.EUNI_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_euni_001));
                    messageTextView.setText(context.getString(R.string.EUNI_Page_0_1_Message));
                } else if (pagePosition == 1) {
                    titleTextView.setText(context.getString(R.string.EUNI_Page_1_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_euni_002));
                    messageTextView.setText(context.getString(R.string.EUNI_Page_1_1_Message));
                } else if (pagePosition == 2) {
                    titleTextView.setText(context.getString(R.string.EUNI_Page_2_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_euni_003));
                    messageTextView.setText(context.getString(R.string.EUNI_Page_2_1_Message));
                } else if (pagePosition == 3) {
                    titleTextView.setText(context.getString(R.string.EUNI_Page_3_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_euni_004));
                    messageTextView.setText(context.getString(R.string.EUNI_Page_3_1_Message));
                }
                break;
            case 1:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Score_Inquiry_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_score_inquiry_001));
                    messageTextView.setText(context.getString(R.string.Score_Inquiry_Page_0_1_Message));
                } else if (pagePosition == 1) {
                    titleTextView.setText(context.getString(R.string.Score_Inquiry_Page_1_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_score_inquiry_002));
                    messageTextView.setText(context.getString(R.string.Score_Inquiry_Page_1_1_Message));
                }
                break;
            case 2:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Class_Schedule_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_class_schedule_001));
                    messageTextView.setText(context.getString(R.string.Class_Schedule_Page_0_1_Message));
                }
                break;
            case 3:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Event_Registration_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_event_registration_001));
                    messageTextView.setText(context.getString(R.string.Event_Registration_Page_0_1_Message));
                } else if (pagePosition == 1) {
                    titleTextView.setText(context.getString(R.string.Event_Registration_Page_1_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_event_registration_002));
                    messageTextView.setText(context.getString(R.string.Event_Registration_Page_1_1_Message));
                } else if (pagePosition == 2) {
                    titleTextView.setText(context.getString(R.string.Event_Registration_Page_2_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_event_registration_003));
                    messageTextView.setText(context.getString(R.string.Event_Registration_Page_2_1_Message));
                } else if (pagePosition == 3) {
                    titleTextView.setText(context.getString(R.string.Event_Registration_Page_3_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_event_registration_004));
                    messageTextView.setText(context.getString(R.string.Event_Registration_Page_3_1_Message));
                }
                break;
            case 4:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Contact_Us_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_contact_us_001));
                    messageTextView.setText(context.getString(R.string.Contact_Us_Page_0_1_Message));
                } else if (pagePosition == 1) {
                    titleTextView.setText(context.getString(R.string.Contact_Us_Page_1_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_contact_us_002));
                    messageTextView.setText(context.getString(R.string.Contact_Us_Page_1_1_Message));
                }
                break;
            case 5:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Graduation_Threshold_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_graduation_threshold_001));
                    messageTextView.setText(context.getString(R.string.Graduation_Threshold_Page_0_1_Message));
                } else if (pagePosition == 1) {
                    titleTextView.setText(context.getString(R.string.Graduation_Threshold_Page_1_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_graduation_threshold_002));
                    messageTextView.setText(context.getString(R.string.Graduation_Threshold_Page_1_1_Message));
                }
                break;
            case 6:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Subject_System_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_subject_system_001));
                    messageTextView.setText(context.getString(R.string.Subject_System_Page_0_1_Message));
                }
                break;
            case 7:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Bus_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_bus_001));
                    messageTextView.setText(context.getString(R.string.Bus_Page_0_1_Message));
                } else if (pagePosition == 1) {
                    titleTextView.setText(context.getString(R.string.Bus_Page_1_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_bus_002));
                    messageTextView.setText(context.getString(R.string.Bus_Page_1_1_Message));
                }
                break;
            case 8:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Zuvio_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_zuvio_001));
                    messageTextView.setText(context.getString(R.string.Zuvio_Page_0_1_Message));
                } else if (pagePosition == 1) {
                    titleTextView.setText(context.getString(R.string.Zuvio_Page_1_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_zuvio_002));
                    messageTextView.setText(Html.fromHtml(context.getString(R.string.Zuvio_Page_1_1_Message), Html.FROM_HTML_MODE_LEGACY));
                }
                break;
            case 9:
                if (pagePosition == 0) {
                    titleTextView.setText(context.getString(R.string.Take_Leave_Page_0_1_Title));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.frame_take_leave_001));
                    messageTextView.setText(context.getString(R.string.Take_Leave_Page_0_1_Message));
                }
                break;
            default:
                titleTextView.setText("ʕ\u2060´\u2060•\u2060ᴥ\u2060•\u2060`\u2060ʔ");
                messageTextView.setText("ʕ\u2060´\u2060•\u2060ᴥ\u2060•\u2060`\u2060ʔ");
                break;
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private int getPageCountForPosition(int position) {
        switch (position) {
            case 0:
                return 4; // 4頁
            case 1:
                return 2; // 2頁
            case 2:
                return 1; // 1頁
            case 3:
                return 4; // 4頁
            case 4:
                return 2; // 2頁
            case 5:
                return 2; // 2頁
            case 6:
                return 1; // 1頁
            case 7:
                return 2; // 2頁
            case 8:
                return 2; // 2頁
            case 9:
                return 1; // 1頁
            default:
                return 0;
        }
    }
}

