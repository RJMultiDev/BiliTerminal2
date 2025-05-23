package com.huanli233.biliterminal2.api;

import com.huanli233.biliterminal2.util.network.NetWorkUtil;
import com.huanli233.biliterminal2.util.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class LikeCoinFavApi {


    public static int like(long aid, int likeState) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/archive/like";
        String per = "aid=" + aid + "&like=" + likeState + "&csrf=" + Preferences.getString(Preferences.CSRF, "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        return result.getInt("code");
    }

    public static int coin(long aid, int multiply) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/coin/add";
        String per = "aid=" + aid + "&multiply=" + multiply + "&csrf=" + Preferences.getString(Preferences.CSRF, "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        return result.getInt("code");
    }

    public static int favorite(long aid, long fid) throws IOException, JSONException {
        String strMid = String.valueOf(Preferences.getLong("mid", 0));
        String addFid = fid + strMid.substring(strMid.length() - 2);
        String url = "https://api.bilibili.com/medialist/gateway/coll/resource/deal";
        String per = "rid=" + aid + "&type=2&add_media_ids=" + addFid + "&del_media_ids=&csrf=" + Preferences.getString(Preferences.CSRF, "");

        JSONObject result = new JSONObject(Objects.requireNonNull(NetWorkUtil.post(url, per, NetWorkUtil.webHeaders).body()).string());
        return result.getInt("code");
    }

    public static boolean getLiked(long aid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/archive/has/like?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return (result.getInt("code") == 0 && result.getInt("data") == 1);
    }

    public static int getCoined(long aid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/web-interface/archive/coins?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return (result.getInt("code") == 0 ? result.getJSONObject("data").getInt("multiply") : 0);
    }

    public static boolean getFavoured(long aid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/fav/video/favoured?aid=" + aid;
        JSONObject result = NetWorkUtil.getJson(url);
        return (result.getInt("code") == 0 && result.getJSONObject("data").getBoolean("favoured"));
    }

}
