package com.RobinNotBad.BiliClient.activity.tutorial;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.R;

public class DynamicFragment extends Fragment {
    public DynamicFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial_dynamic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.argb(190,230,42,42));

        TextView text1_view = view.findViewById(R.id.text1);
        SpannableString text1 = new SpannableString("这是动态的详情，这个页面也可以左右滑动\n第一页是动态详情\n第二页是评论区");
        text1.setSpan(foregroundColorSpan,12,19,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        //多个加粗只能每次new一个StyleSpan
        text1.setSpan(new StyleSpan(Typeface.BOLD),12,19, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        text1.setSpan(new StyleSpan(Typeface.BOLD),33,36, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        text1_view.setText(text1);
    }
}
