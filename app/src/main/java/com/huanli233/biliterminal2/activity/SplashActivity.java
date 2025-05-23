package com.huanli233.biliterminal2.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.huanli233.biliterminal2.BiliTerminal;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.InstanceActivity;
import com.huanli233.biliterminal2.activity.settings.setup.SetupUIActivity;
import com.huanli233.biliterminal2.activity.video.RecommendActivity;
import com.huanli233.biliterminal2.activity.video.local.LocalListActivity;
import com.huanli233.biliterminal2.api.CookieRefreshApi;
import com.huanli233.biliterminal2.api.CookiesApi;
import com.huanli233.biliterminal2.util.ThreadManager;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.network.NetWorkUtil;
import com.huanli233.biliterminal2.util.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

//启动页面
//一切的一切的开始

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends Activity {

    private TextView splashTextView;
    private int splashFrame;
    private Timer splashTimer;
    private Handler handler;
    private String splashText = "欢迎使用\n哔哩终端";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(BiliTerminal.getFitDisplayContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BiliTerminal2);
        setContentView(R.layout.activity_splash);

        handler = new Handler(Looper.getMainLooper());
        splashTextView = findViewById(R.id.splashText);
        splashText = Preferences.getString("ui_splashtext", "欢迎使用\n哔哩终端");

        splashTimer = new Timer();
        splashTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> showSplashText(splashFrame));
                splashFrame++;
                if (splashFrame > splashText.length()) this.cancel();
            }
        }, 100, 100);

        ThreadManager.run(() -> {

            //FileUtil.clearCache(this);  //先清个缓存（为了防止占用过大）
            //不需要了，我把大部分图片的硬盘缓存都关闭了，只有表情包保留，这样既可以缩减缓存占用又能在一定程度上减少流量消耗

            if (Preferences.getBoolean(Preferences.SETUP, false)) {//判断是否设置完成
                try {
                    // 未登录时请求bilibili.com
                    if (Preferences.getLong("mid", 0) != 0) {
                        checkCookieRefresh();
                    } else {
                        // [开发者]RobinNotBad: 如果提前不请求bilibili.com，未登录时的推荐有概率一直返回同样的内容
                        NetWorkUtil.get("https://www.bilibili.com", NetWorkUtil.webHeaders);
                    }

                    CookiesApi.checkCookies();

                    String firstActivity = null;
                    String sortConf = Preferences.getString(Preferences.MENU_SORT, "");
                    if (!TextUtils.isEmpty(sortConf)) {
                        String[] splitName = sortConf.split(";");
                        for (String name : splitName) {
                            if (!MenuActivity.btnNames.containsKey(name)) {
                                for (Map.Entry<String, Pair<String, Class<? extends InstanceActivity>>> entry : MenuActivity.btnNames.entrySet()) {
                                    firstActivity = entry.getKey();
                                    break;
                                }
                            } else {
                                firstActivity = name;
                            }
                            break;
                        }
                    } else {
                        for (Map.Entry<String, Pair<String, Class<? extends InstanceActivity>>> entry : MenuActivity.btnNames.entrySet()) {
                            firstActivity = entry.getKey();
                            break;
                        }
                    }

                    Class<? extends InstanceActivity> activityClass = Objects.requireNonNull(MenuActivity.btnNames.get(firstActivity)).second;

                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, (activityClass != null ? activityClass : RecommendActivity.class));
                    intent.putExtra("from", firstActivity);

                    interruptSplash();

                    handler.postDelayed(() -> {
                        startActivity(intent);
                        finish();
                    }, 100);

                } catch (IOException e) {
                    runOnUiThread(() -> {
                        MsgUtil.error(e);
                        interruptSplash();
                        splashTextView.setText("网络错误");
                        if (Preferences.getBoolean("setup", false)) {
                            handler.postDelayed(() -> {
                                Intent intent = new Intent();
                                intent.setClass(SplashActivity.this, LocalListActivity.class);
                                startActivity(intent);
                                finish();
                            }, 300);
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> MsgUtil.error(e));
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, LocalListActivity.class);
                    startActivity(intent);
                    interruptSplash();
                    finish();
                }
            } else {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, SetupUIActivity.class);   //没登录，去初次设置
                startActivity(intent);
                interruptSplash();
                finish();
            }

        });
    }

    private void checkCookieRefresh() throws IOException {
        try {
            JSONObject cookieInfo = CookieRefreshApi.cookieInfo();
            if (cookieInfo.getBoolean("refresh")) {
                Log.e("Cookie", "需要刷新");
                if (Objects.equals(Preferences.getString(Preferences.REFRESH_TOKEN, ""), ""))
                    runOnUiThread(() -> MsgUtil.showMsgLong("无法刷新Cookie，请重新登录！"));
                else {
                    String correspondPath = CookieRefreshApi.getCorrespondPath(cookieInfo.getLong("timestamp"));
                    Log.e("CorrespondPath", correspondPath);
                    String refreshCsrf = CookieRefreshApi.getRefreshCsrf(correspondPath);
                    Log.e("RefreshCsrf", refreshCsrf);
                    if (CookieRefreshApi.refreshCookie(refreshCsrf)) {
                        NetWorkUtil.refreshHeaders();
                        runOnUiThread(() -> MsgUtil.showMsg("Cookie已刷新"));
                    } else {
                        runOnUiThread(() -> MsgUtil.showMsgLong("登录信息过期，请重新登录！"));
                        resetLogin();
                    }
                }
            }
        } catch (JSONException e) {
            runOnUiThread(() -> MsgUtil.showMsgLong("登录信息过期，请重新登录！"));
            resetLogin();
        }
    }

    private void resetLogin() {
        Preferences.putLong(Preferences.MID, 0L);
        Preferences.putString(Preferences.CSRF, "");
        Preferences.putString(Preferences.COOKIES, "");
        Preferences.putString(Preferences.REFRESH_TOKEN, "");
        NetWorkUtil.refreshHeaders();
    }

    @SuppressLint("SetTextI18n")
    private void showSplashText(int i) {
        if (i > splashText.length()) splashTextView.setText(splashText);
        else splashTextView.setText(splashText.substring(0, i) + "_");
    }

    private void interruptSplash() {
        if (splashTimer != null) splashTimer.cancel();
        runOnUiThread(() -> splashTextView.setText(splashText));
    }
}