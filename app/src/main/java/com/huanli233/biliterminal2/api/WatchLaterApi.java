package com.huanli233.biliterminal2.api;

import com.huanli233.biliterminal2.bean.VideoCardKt;
import com.huanli233.biliterminal2.util.network.NetWorkUtil;
import com.huanli233.biliterminal2.util.Preferences;
import com.huanli233.biliterminal2.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Response;

//稍后再看API
//2023-08-17

public class WatchLaterApi {
    public static ArrayList<VideoCardKt> getWatchLaterList() throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/history/toview/web";

        JSONObject result = NetWorkUtil.getJson(url);
        JSONObject data = result.getJSONObject("data");


        ArrayList<VideoCardKt> videoCardList = new ArrayList<>();
        if (!data.isNull("list")) {
            JSONArray list = data.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject videoCard = list.getJSONObject(i);
                long aid = videoCard.getLong("aid");
                String bvid = videoCard.getString("bvid");
                String title = videoCard.getString("title");
                String cover = videoCard.getString("pic");
                String upName = videoCard.getJSONObject("owner").getString("name");
                long view = videoCard.getJSONObject("stat").getLong("view");
                String viewStr = Utils.toWan(view) + "观看";
                videoCardList.add(VideoCardKt.of(title, upName, viewStr, cover, aid, bvid));
            }
        }
        return videoCardList;
    }

    public static int delete(long aid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/history/toview/del";
        String per = "aid=" + aid + "&csrf=" + Preferences.getString(Preferences.CSRF, "");

        Response response = NetWorkUtil.post(url, per, NetWorkUtil.webHeaders);

        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());

        return result.getInt("code");
    }

    public static int add(long aid) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/v2/history/toview/add";
        String per = "aid=" + aid + "&csrf=" + Preferences.getString(Preferences.CSRF, "");

        Response response = NetWorkUtil.post(url, per, NetWorkUtil.webHeaders);

        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());

        return result.getInt("code");
    }
}
