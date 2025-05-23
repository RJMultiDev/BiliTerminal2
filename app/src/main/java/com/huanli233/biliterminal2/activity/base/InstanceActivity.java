package com.huanli233.biliterminal2.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.huanli233.biliterminal2.BiliTerminal;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.MenuActivity;

public class InstanceActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BiliTerminal.setInstance(this);
        super.onCreate(savedInstanceState);
    }

    public void setMenuClick() {
        findViewById(R.id.top_bar).setOnClickListener(view -> menuClick.run());
    }

    public Runnable menuClick = (() -> {
        Intent intent = new Intent();
        intent.setClass(this, MenuActivity.class);
        if (getIntent().hasExtra("from"))
            intent.putExtra("from", getIntent().getStringExtra("from"));
        startActivity(intent);
        overridePendingTransition(R.anim.anim_activity_in_down, 0);
    });

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) menuClick.run();
        return super.onKeyDown(keyCode, event);
    }
}
