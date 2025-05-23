package com.huanli233.biliterminal2.activity.settings.login;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.SplashActivity;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.network.NetWorkUtil;
import com.huanli233.biliterminal2.util.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

public class SpecialLoginActivity extends BaseActivity {

    private EditText textInput;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_special);

        textInput = findViewById(R.id.loginInput);
        MaterialCardView confirm = findViewById(R.id.confirm);
        MaterialCardView refuse = findViewById(R.id.refuse);
        TextView desc = findViewById(R.id.desc);

        Intent intent = getIntent();

        if (intent.getBooleanExtra("login", true)) {
            refuse.setOnClickListener(v -> {
                if (getIntent().getBooleanExtra("from_setup", false))
                    startActivity(new Intent(this, SplashActivity.class));
                else finish();
            });

            confirm.setOnClickListener(view -> {
                String loginInfo = textInput.getText().toString();
                try {
                    JSONObject jsonObject = new JSONObject(loginInfo);
                    String cookies = jsonObject.getString("cookies");
                    Preferences.putLong(Preferences.MID, Long.parseLong(NetWorkUtil.getInfoFromCookie("DedeUserID", cookies)));
                    Preferences.putString(Preferences.CSRF, NetWorkUtil.getInfoFromCookie("bili_jct", cookies));
                    Preferences.putString(Preferences.COOKIES, cookies);
                    Preferences.putString(Preferences.REFRESH_TOKEN, jsonObject.getString("refresh_token"));
                    runOnUiThread(() -> MsgUtil.showMsg("登录成功！"));
                    Preferences.putBoolean(Preferences.SETUP, true);

                    NetWorkUtil.refreshHeaders();

                    Intent intent1 = new Intent();
                    intent1.setClass(SpecialLoginActivity.this, SplashActivity.class);
                    startActivity(intent1);
                    finish();
                } catch (JSONException e) {
                    runOnUiThread(() -> MsgUtil.showMsg("请检查输入的内容，不要有多余空格或字符"));
                }
            });
        } else {
            desc.setText(R.string.special_login_export);
            TextView buttonText = findViewById(R.id.confirm_text);
            buttonText.setText("复制");
            buttonText.setCompoundDrawables(null, null, null, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                buttonText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("cookies", Preferences.getString(Preferences.COOKIES, ""));
                jsonObject.put("refresh_token", Preferences.getString(Preferences.REFRESH_TOKEN, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            textInput.setText(jsonObject.toString());
            textInput.clearFocus();
            refuse.setVisibility(View.GONE);
            confirm.setOnClickListener((view) -> {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("label", textInput.getText());
                clipboardManager.setPrimaryClip(clipData);
                MsgUtil.showMsg("已复制");
            });
        }
    }

}