package com.huanli233.biliterminal2.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.huanli233.biliterminal2.activity.article.ArticleInfoActivity;
import com.huanli233.biliterminal2.activity.dynamic.DynamicInfoActivity;
import com.huanli233.biliterminal2.activity.live.LiveInfoActivity;
import com.huanli233.biliterminal2.activity.video.info.VideoInfoActivity;
import com.huanli233.biliterminal2.api.DynamicApi;
import com.huanli233.biliterminal2.api.LiveApi;
import com.huanli233.biliterminal2.api.ReplyApi;
import com.huanli233.biliterminal2.api.UserInfoApi;
import com.huanli233.biliterminal2.model.ArticleInfo;
import com.huanli233.biliterminal2.model.ContentType;
import com.huanli233.biliterminal2.model.Dynamic;
import com.huanli233.biliterminal2.model.LiveInfo;
import com.huanli233.biliterminal2.model.LivePlayInfo;
import com.huanli233.biliterminal2.model.LiveRoom;

import java.util.concurrent.Future;

/**
 * @author silent碎月
 * @date 2024/11/03
 * 哔哩终端的中央上下文，所有不方便传的
 * 希望在任何地方都能拿得到，不想再额外建工具类的话
 * 扔这里就好，这里是屎山的集中地
 * 所有的工具类，也可以往这里扔一个实现
 */
public class TerminalContext {

    //要转发的东西的数据源
    private Object forwardContent;
    /**
     * 详情页以及对应数据对象的存储， 每进入一个页面，例如动态，动态点击进入视频， 视频下面有个专栏
     * 然后再返回，此时的逻辑就是像栈一样。
     */
    private final LruCache<String, Object> contentLruCache;

    private TerminalContext() {
        contentLruCache = new LruCache<>(10);
    }

    // ------------------------转发功能数据源上下文 start-------------------------------
    public void setForwardContent(Object forwardContent) {
        this.forwardContent = forwardContent;
    }

    public Object getForwardContent() {
        return forwardContent;
    }
    //-------------------------转发功能数据源上下文 end ----------------------------------

    // --------------------------详情页跳转功能 start  ----------------------------------
    // 视频详情页跳转
    public void enterVideoDetailPage(Context context, long aid) {
        enterVideoDetailPage(context, aid, null, "video", -1);
    }

    public void enterVideoDetailPage(Context context, String bvid) {
        enterVideoDetailPage(context, -1, bvid, "video", -1);
    }

    public void enterVideoDetailPage(Context context, long aid, String bvid) {
        enterVideoDetailPage(context, aid, bvid, null, -1);
    }

    public void enterVideoDetailPage(Context context, long aid, String bvid, String type) {
        enterVideoDetailPage(context, aid, bvid, type, -1);
    }

    public void enterVideoDetailPage(Context context, long aid, String bvid, String type, long seekReply) {
        Intent intent = new Intent(context, VideoInfoActivity.class);
        intent.putExtra("aid", aid);
        if (!TextUtils.isEmpty(bvid)) {
            intent.putExtra("bvid", bvid);
        }
        if (type != null) {
            intent.putExtra("type", type);
        }
        intent.putExtra("seekReply", seekReply);
        context.startActivity(intent);
    }

    public void enterArticleDetailPage(Context context, long cvid) {
        enterArticleDetailPage(context, cvid, -1);
    }

    public void enterArticleDetailPage(Context context, long cvid, long seekReply) {
        Intent intent = new Intent(context, ArticleInfoActivity.class);
        intent.putExtra("cvid", cvid);
        intent.putExtra("seekReply", seekReply);
        context.startActivity(intent);
    }

    // 动态详情页跳转
    public void enterDynamicDetailPage(Context context, long id) {
        enterDynamicDetailPage(context, id, 0, -1);
    }

    public void enterDynamicDetailPage(Context context, long id, int position) {
        enterDynamicDetailPage(context, id, position, -1);
    }

    public void enterDynamicDetailPage(Context context, long id, int position, long seekReply) {
        Intent intent = new Intent(context, DynamicInfoActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("id", id);
        intent.putExtra("seekReply", seekReply);
        context.startActivity(intent);
    }

    /*
     * 由于动态有可删除的特性，部分页面依赖动态页面activity的result实现页面更新，这里加入额外的一个兼容方法
     */
    public void enterDynamicDetailPageForResult(Activity activity, long id, int position, int requestId) {
        Intent intent = new Intent(activity, DynamicInfoActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("position", position);
        activity.startActivityForResult(intent, requestId);
    }

    private Result<Dynamic> fetchDynamic(long id, boolean saveToCache) {
        try {
            Dynamic dynamic = DynamicApi.getDynamic(id);
            if (saveToCache) {
                contentLruCache.put(ContentType.Dynamic.getTypeCode() + "_" + id, dynamic);
            }
            return Result.success(dynamic);
        } catch (Exception t) {
            return Result.failure(t);
        }
    }

    /**
     * 进行一个直播详情页的启动
     *
     * @param context Android上下文对象
     * @param roomId  直播房间号
     */
    public void enterLiveDetailPage(Context context, long roomId) {
        Intent intent = new Intent(context, LiveInfoActivity.class);
        intent.putExtra("room_id", roomId);
        context.startActivity(intent);
    }

    public LiveData<Result<Dynamic>> getDynamicById(long id) {
        String key = ContentType.Dynamic.getTypeCode() + "_" + id;
        Object obj = contentLruCache.get(key);
        if (!(obj instanceof Dynamic)) {
            return ThreadManager.supplyAsyncWithLiveData(() -> fetchDynamic(id, true).getOrThrow());
        } else {
            return new MutableLiveData<>(Result.success((Dynamic) obj));
        }
    }

    private static final class InstanceHolder {
        static final TerminalContext INSTANCE = new TerminalContext();
    }

    public static TerminalContext getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static class IllegalTerminalStateException extends Exception {
        private final String description;

        public IllegalTerminalStateException() {
            description = "";
        }

        public IllegalTerminalStateException(String description) {
            this.description = description;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        public String getMessage() {
            return this.description;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        @Override
        public String getLocalizedMessage() {
            return this.description;
        }
    }
}
