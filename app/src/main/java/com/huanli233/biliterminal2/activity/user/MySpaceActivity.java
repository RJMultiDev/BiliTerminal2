package com.huanli233.biliterminal2.activity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.InstanceActivity;
import com.huanli233.biliterminal2.activity.settings.login.LoginActivity;
import com.huanli233.biliterminal2.activity.user.favorite.FavoriteFolderListActivity;
import com.huanli233.biliterminal2.activity.user.info.UserInfoActivity;
import com.huanli233.biliterminal2.api.UserInfoApi;
import com.huanli233.biliterminal2.bean.UserInfo;
import com.huanli233.biliterminal2.util.view.AsyncLayoutInflaterX;
import com.huanli233.biliterminal2.util.ThreadManager;
import com.huanli233.biliterminal2.util.GlideUtil;
import com.huanli233.biliterminal2.util.MsgUtil;
import com.huanli233.biliterminal2.util.Preferences;
import com.huanli233.biliterminal2.util.Utils;

import kotlin.Unit;

public class MySpaceActivity extends InstanceActivity {

    private ImageView userAvatar;
    private TextView userName, userFans, userExp;
    private MaterialCardView myInfo, follow, watchLater, favorite, bangumi, history, creative, logout;

    private boolean confirmLogout = false;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_myspace, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setMenuClick();

            userAvatar = findViewById(R.id.user_avatar);
            userName = findViewById(R.id.user_name);
            userFans = findViewById(R.id.userFans);
            userExp = findViewById(R.id.userExp);

            myInfo = findViewById(R.id.myinfo);
            follow = findViewById(R.id.follow);
            watchLater = findViewById(R.id.watchlater);
            favorite = findViewById(R.id.favorite);
            bangumi = findViewById(R.id.bangumi);
            history = findViewById(R.id.history);
            creative = findViewById(R.id.creative);
            logout = findViewById(R.id.logout);


            ThreadManager.run(() -> {
                try {
                    UserInfo userInfo = UserInfoApi.getCurrentUserInfo();
                    int userCoin = UserInfoApi.getCurrentUserCoin();
                    if (!this.isDestroyed()) runOnUiThread(() -> {
                        Glide.with(MySpaceActivity.this).load(GlideUtil.url(userInfo.avatar))
                                .transition(GlideUtil.getTransitionOptions())
                                .placeholder(R.mipmap.akari).apply(RequestOptions.circleCropTransform())
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(userAvatar);
                        userName.setText(userInfo.name);
                        userFans.setText(Utils.toWan(userInfo.fans) + "粉丝 " + userCoin + "硬币");

                        myInfo.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, UserInfoActivity.class);
                            intent.putExtra("mid", userInfo.mid);
                            startActivity(intent);
                        });

                        follow.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, FollowUsersActivity.class);
                            intent.putExtra("mid", userInfo.mid);
                            intent.putExtra("mode", 0);
                            startActivity(intent);
                        });

                        watchLater.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, WatchLaterActivity.class);
                            startActivity(intent);
                        });

                        favorite.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, FavoriteFolderListActivity.class);
                            startActivity(intent);
                        });

                        bangumi.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, FollowingBangumisActivity.class);
                            startActivity(intent);
                        });

                        history.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, HistoryActivity.class);
                            startActivity(intent);
                        });

                        creative.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, CreativeCenterActivity.class);
                            startActivity(intent);
                        });
                        if (!Preferences.getBoolean("creative_enable", true))
                            creative.setVisibility(View.GONE);

                        logout.setOnClickListener(view -> {
                            if (confirmLogout) {
                                ThreadManager.run(UserInfoApi::exitLogin);
                                Preferences.removeValue(Preferences.COOKIES);
                                Preferences.removeValue(Preferences.MID);
                                Preferences.removeValue(Preferences.CSRF);
                                Preferences.removeValue(Preferences.REFRESH_TOKEN);
                                Preferences.removeValue(Preferences.COOKIE_REFRESH);
                                MsgUtil.showMsg("账号已退出");
                                Intent intent = new Intent(this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else MsgUtil.showMsg("再点一次退出登录！");
                            confirmLogout = !confirmLogout;
                        });
                    });
                } catch (Exception e) {
                    report(e);
                }
            });

            return Unit.INSTANCE;
        });
    }
}