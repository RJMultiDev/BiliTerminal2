package com.huanli233.biliterminal2.api;

import com.elvishew.xlog.XLog;
import com.huanli233.biliterminal2.bean.Bangumi;
import com.huanli233.biliterminal2.bean.VideoCardKt;
import com.huanli233.biliterminal2.util.network.NetWorkUtil;
import com.huanli233.biliterminal2.util.Preferences;
import com.huanli233.biliterminal2.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BangumiApi {
    public static int getFollowingList(int page, List<VideoCardKt> cardList) throws JSONException, IOException {
        String url = "https://api.bilibili.com/x/space/bangumi/follow/list?type=1&follow_status=0&pn=" + page
                + "&ps=15&vmid=" + Preferences.getLong("mid", 0);

        JSONObject all = NetWorkUtil.getJson(url);
        if (all.getInt("code") != 0) throw new JSONException(all.getString("message"));

        JSONObject data = all.getJSONObject("data");
        if (!data.has("list") || data.isNull("list")) return 1;

        JSONArray list = data.getJSONArray("list");
        if (list.length() == 0) return 1;

        for (int i = 0; i < list.length(); i++) {
            JSONObject bangumi = list.getJSONObject(i);
            VideoCardKt card = new VideoCardKt();
            card.setType("media_bangumi");
            card.setAid(bangumi.getLong("media_id"));
            card.setTitle(bangumi.getString("title"));
            card.setCover(bangumi.getString("cover"));
            card.setView(Utils.toWan(bangumi.getJSONObject("stat").optInt("view")));

            cardList.add(card);
        }
        return 0;
    }


    public static Bangumi getBangumi(long mediaId) throws JSONException, IOException {
        Bangumi bangumi = new Bangumi();
        bangumi.info = getInfo(mediaId);
        bangumi.sectionList = getSections(bangumi.info.season_id);
        return bangumi;
    }

    public static Long getMdidFromEpid(long epid) {
        try {
            String url = "https://api.bilibili.com/pgc/view/web/season?ep_id=" + epid;
            JSONObject all = NetWorkUtil.getJson(url);

            int code = all.getInt("code");
            if (code != 0) return 0L;

            return all.getJSONObject("result").getLong("media_id");
        } catch (Exception e) {
            XLog.e(e);
            return 0L;
        }
    }

    public static Bangumi.Info getInfo(long mediaId) throws IOException, JSONException {
        String url = "https://api.bilibili.com/pgc/review/user?media_id=" + mediaId;
        JSONObject all = NetWorkUtil.getJson(url);

        int code = all.getInt("code");
        if (code != 0) {
            throw new JSONException("错误码：" + code);
        }

        JSONObject media = all.getJSONObject("result").getJSONObject("media");

        Bangumi.Info info = new Bangumi.Info();
        info.media_id = media.getLong("media_id");
        info.season_id = media.getLong("season_id");
        info.title = media.getString("title");
        info.cover = media.getString("cover");
        info.cover_horizontal = media.getString("horizontal_picture");
        info.type = media.getInt("type");
        info.type_name = media.getString("type_name");

        JSONObject new_ep = media.optJSONObject("new_ep");
        if (new_ep != null) {
            info.indexShow = new_ep.optString("index_show", "敬请期待");
        }

        JSONObject rating = media.optJSONObject("rating");
        if (rating != null) {
            info.count = rating.optInt("count");
            info.score = (float) rating.optInt("score");
        }

        JSONArray areas = media.getJSONArray("areas");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < areas.length(); i++) {
            stringBuilder.append(areas.getJSONObject(i).getString("name"));
            stringBuilder.append("|");
        }
        stringBuilder.substring(0, stringBuilder.length() - 1);
        info.area_name = stringBuilder.toString();

        return info;
    }

    public static ArrayList<Bangumi.Section> getSections(long season_id) throws IOException, JSONException {
        String url = "https://api.bilibili.com/pgc/web/season/section?season_id=" + season_id;
        JSONObject all = new JSONObject(Objects.requireNonNull(Objects.requireNonNull(NetWorkUtil.get(url)).body()).string());  //得到一整个json
        JSONObject result = all.getJSONObject("result");
        ArrayList<Bangumi.Section> sectionList = new ArrayList<>();

        JSONObject main_section = result.optJSONObject("main_section");
        if (main_section != null)
            sectionList.add(analyzeSection(result.getJSONObject("main_section")));

        JSONArray other_sections = result.optJSONArray("section");
        if (other_sections != null) {
            for (int i = 0; i < other_sections.length(); i++) {
                sectionList.add(analyzeSection(other_sections.getJSONObject(i)));
            }
        }

        return sectionList;
    }

    public static Bangumi.Section analyzeSection(JSONObject jsonObject) throws JSONException {
        Bangumi.Section section = new Bangumi.Section();
        section.id = jsonObject.getLong("id");
        section.title = jsonObject.getString("title");
        section.type = jsonObject.getInt("type");

        JSONArray episodes = jsonObject.getJSONArray("episodes");
        ArrayList<Bangumi.Episode> episodeList = new ArrayList<>();
        for (int i = 0; i < episodes.length(); i++) {
            episodeList.add(analyzeEpisode(episodes.getJSONObject(i)));
        }
        section.episodeList = episodeList;

        return section;
    }

    public static Bangumi.Episode analyzeEpisode(JSONObject jsonObject) throws JSONException {
        Bangumi.Episode episode = new Bangumi.Episode();
        episode.id = jsonObject.getLong("id");
        episode.aid = jsonObject.getLong("aid");
        episode.cid = jsonObject.getLong("cid");
        episode.cover = jsonObject.getString("cover");
        episode.badge = jsonObject.getString("badge");
        episode.title = jsonObject.getString("title");
        episode.title_long = jsonObject.getString("long_title");

        return episode;
    }
}

