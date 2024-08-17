package com.niu.csie.edu.app;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Activity3_Score_Inquiry_Fragment3 extends Fragment { // 此 Fragment 暫時無用

    /**Element*/


    /**Components*/


    /**Variable*/



    public Activity3_Score_Inquiry_Fragment3() {
        // Required empty public constructor
    }


    public static Activity3_Score_Inquiry_Fragment3 getInstance() {
        Activity3_Score_Inquiry_Fragment3 fragment = new Activity3_Score_Inquiry_Fragment3();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity3_score_inquiry_fragment3, container, false);
        // 主畫面UI
        // 初始化設置
        return v;
    }
}