package com.huanli233.biliterminal2.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.RefreshListActivity;
import com.huanli233.biliterminal2.adapter.SettingsAdapter;
import com.huanli233.biliterminal2.bean.SettingSection;
import com.huanli233.biliterminal2.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class SettingPrefActivity extends RefreshListActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("偏好设置");

        final List<SettingSection> sectionList = new ArrayList<>() {{
            add(new SettingSection("switch", "禁用返回键", "back_disable", getString(R.string.desc_back_disable), "false"));
            add(new SettingSection("switch", "创作中心", "creative_enable", getString(R.string.desc_creative_enable), "true"));
            add(new SettingSection("switch", "长按复制", "copy_enable", getString(R.string.desc_copy_enable), "true"));
            add(new SettingSection("switch", "加载渐入渐出动画", Preferences.LOAD_TRANSITION, getString(R.string.desc_load_transition), "true"));
            add(new SettingSection("switch", "翻动时不加载图片", "image_no_load_onscroll", getString(R.string.desc_img_no_load_onscroll), "false"));
            add(new SettingSection("switch", "请求JPG格式图片", "image_request_jpg", getString(R.string.desc_img_request_jpg), "false"));
            add(new SettingSection("switch", "识别链接", "link_enable", getString(R.string.desc_link_enable), "true"));
            add(new SettingSection("switch", "异步加载布局", Preferences.ASYNC_INFLATE_ENABLE, getString(R.string.desc_async_inflate_enable), "true"));
            add(new SettingSection("switch", "新提示信息显示方式", Preferences.SNACKBAR_ENABLE, "打开此选项，会启用新提示信息显示方式", "true"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);

    }

}