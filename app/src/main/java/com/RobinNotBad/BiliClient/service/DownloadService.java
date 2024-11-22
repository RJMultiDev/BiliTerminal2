package com.RobinNotBad.BiliClient.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadService extends Service {
    public static float percent;
    public static String parent;
    public static String name;
    public static String name_short;
    public static boolean started;
    public static short count_finish;
    private SharedPreferences downloadPrefs;
    private Timer toastTimer;
    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if(name_short == null)return;
            MsgUtil.showMsg("下载进度："+ (percent * 100) + "%\n" + name_short);
        }
    };

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MutatingSharedPrefs")
    @Override
    public int onStartCommand(Intent serviceIntent, int flags, int startId) {
        if(started) {
            stopSelf();
            return super.onStartCommand(serviceIntent, flags, startId);
        }

        started = true;
        count_finish = 0;
        MsgUtil.showMsg("下载服务已启动");
        toastTimer = new Timer();
        toastTimer.schedule(timerTask,5000,5000);
        downloadPrefs = getSharedPreferences("download",MODE_PRIVATE);

        CenterThreadPool.run(()->{
            while (true) {
                Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
                if(set.size() == 0) {
                    MsgUtil.showMsg("下载列表set为空");
                    break;
                }

                String[] array = set.toArray(new String[0]);
                if (array.length == 0) {
                    MsgUtil.showMsg("下载列表array为空");
                    break;
                }

                int i = 0;
                for (; i < array.length; i++) {
                    if(!array[i].contains("\"state\":\"error\"")) break;  //遍历列表，如果失败就跳过
                }
                String section = array[i];
                Log.d("download",section);

                try {
                    JSONObject task = new JSONObject(section);

                    String type = task.getString("type");
                    long aid = task.getLong("aid");
                    long cid = task.getLong("cid");
                    int qn = task.getInt("qn");
                    name = ToolsUtil.stringToFile(task.getString("name"));

                    String url;
                    try{
                        url = PlayerApi.getVideo(aid, cid, qn, true).first;
                    } catch (JSONException e){
                        MsgUtil.showMsg("下载链接获取失败");
                        task.put("state","error");
                        set.remove(section);
                        set.add(task.toString());    //链接获取失败后标记为失败且不下载此项
                        downloadPrefs.edit().putStringSet("list",set).apply();
                        continue;
                    }

                    try {
                        switch (type) {
                            case "video_single":  //单集视频
                                name_short = name.substring(0, Math.min(6, name.length()))
                                        + (name.length() > 5 ? "..." : "");
                                MsgUtil.showMsg("开始下载：\n" + name_short);

                                File path_single = new File(FileUtil.getDownloadPath(this), name);
                                if (!path_single.exists()) path_single.mkdirs();

                                String url_dm_single = task.getString("url_dm");
                                downdanmu(url_dm_single, new File(path_single, "danmaku.xml"));

                                String url_cover_single = task.getString("url_cover");
                                download(url_cover_single, new File(path_single, "cover.png"));

                                download(url, new File(path_single, "video.mp4"));

                                MsgUtil.showMsg("下载成功：\n" + name_short);
                                break;
                            case "video_multi":  //多集视频
                                String parent = task.getString("parent");

                                name_short = parent.substring(0, Math.min(6, parent.length()))
                                        + (parent.length() > 5 ? "..." : "")
                                        + "-"
                                        + name.substring(0, Math.min(6, name.length()))
                                        + (name.length() > 5 ? "..." : "");
                                MsgUtil.showMsg("开始下载：\n" + name_short);

                                File path_parent = new File(FileUtil.getDownloadPath(this), parent);
                                if(!path_parent.exists()) path_parent.mkdirs();

                                String url_cover_multi = task.getString("url_cover");
                                File cover_multi = new File(path_parent, "cover.png");
                                if (!cover_multi.exists()) download(url_cover_multi, cover_multi);

                                File path_page = new File(path_parent, name);
                                if(!path_page.exists()) path_page.mkdirs();

                                String url_dm_page = task.getString("url_dm");
                                downdanmu(url_dm_page, new File(path_page, "danmaku.xml"));

                                download(url, new File(path_page, "video.mp4"));

                                MsgUtil.showMsg("下载成功：\n" + name_short);
                                count_finish++;
                                break;
                        }
                    } catch (RuntimeException e){
                        MsgUtil.showMsg("下载失败："+e.getMessage());
                        task.put("state","error");
                        set.remove(section);
                        set.add(task.toString());
                        downloadPrefs.edit().putStringSet("list",set).apply();
                    }

                    set.remove(section);
                    downloadPrefs.edit().putStringSet("list",set).apply();

                    InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
                    if (instance instanceof LocalListActivity && !instance.isDestroyed())
                        ((LocalListActivity) (instance)).refresh();
                } catch (JSONException e){
                    MsgUtil.err("下载项格式错误：",e);
                    set.remove(section);
                    downloadPrefs.edit().putStringSet("list",set).apply();
                } catch (IOException e) {
                    MsgUtil.showMsg("下载失败，网络错误\n请手动前往缓存页面重新下载");
                    stopSelf();
                }
            }
            if(count_finish != 0) MsgUtil.showMsg("全部下载完成");
            stopSelf();
        });

        return super.onStartCommand(serviceIntent, flags, startId);
    }

    private void download(String url, File file) throws IOException {
        Response response = NetWorkUtil.get(url);
        try {
            if (!file.exists()) file.createNewFile();
            else {
                file.delete();
                file.createNewFile();
            }
            InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int len;
            byte[] bytes = new byte[1024 * 10];
            long TotalFileSize = Objects.requireNonNull(response.body()).contentLength();
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
                long CompleteFileSize = file.length();
                percent = 1.0f * CompleteFileSize / TotalFileSize;
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件错误");
        }
    }


    private void downdanmu(String danmaku, File danmakuFile) throws IOException{
            Response response = NetWorkUtil.get(danmaku);
            BufferedSink bufferedSink = null;
            try {
                if (!danmakuFile.exists()) danmakuFile.createNewFile();
                else {
                    danmakuFile.delete();
                    danmakuFile.createNewFile();
                }
                Sink sink = Okio.sink(danmakuFile);
                byte[] decompressBytes = decompress(Objects.requireNonNull(response.body()).bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
                bufferedSink = Okio.buffer(sink);
                bufferedSink.write(decompressBytes);//将解压后数据写入文件（sink）中
                bufferedSink.close();
            } catch (IOException e) {
                throw new RuntimeException("文件错误");
            } finally {
                if (bufferedSink != null) {
                    bufferedSink.close();
                }
            }
    }


    @Override
    public void onDestroy() {
        started = false;
        if(toastTimer!=null) toastTimer.cancel();
        super.onDestroy();
    }

    public static byte[] decompress(byte[] data) {
        byte[] output;
        Inflater decompresser = new Inflater(true);//这个true是关键
        decompresser.reset();
        decompresser.setInput(data);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[2048];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        decompresser.end();
        return output;
    }




    //以下为外部调用方法
    @SuppressLint("MutatingSharedPrefs")
    public static void startDownload(String title, long aid, long cid, String danmaku, String cover, int qn){
        try {
            JSONObject task = new JSONObject();
            task.put("type", "video_single");
            task.put("aid", aid);
            task.put("cid", cid);
            task.put("qn", qn);
            task.put("name", title);
            task.put("parent", "");
            task.put("url_cover", cover);
            task.put("url_dm", danmaku);

            SharedPreferences downloadPrefs = BiliTerminal.context.getSharedPreferences("download", MODE_PRIVATE);
            Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
            set.add(task.toString());
            downloadPrefs.edit().putStringSet("list", set).apply();

            Log.d("download",set.toString());

            if(!started) BiliTerminal.context.startService(new Intent(BiliTerminal.context,DownloadService.class));
        } catch (Exception e){
            MsgUtil.err("启动下载时发生错误",e);
        }
    }
    @SuppressLint("MutatingSharedPrefs")
    public static void startDownload(String parent, String child, long aid, long cid, String danmaku, String cover, int qn){
        try {
            JSONObject task = new JSONObject();
            task.put("type", "video_multi");
            task.put("aid", aid);
            task.put("cid", cid);
            task.put("qn", qn);
            task.put("name", child);
            task.put("parent", parent);
            task.put("url_cover", cover);
            task.put("url_dm", danmaku);

            SharedPreferences downloadPrefs = BiliTerminal.context.getSharedPreferences("download", MODE_PRIVATE);
            Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
            set.add(task.toString());
            downloadPrefs.edit().putStringSet("list", set).apply();

            Log.d("download",set.toString());

            if(!started) BiliTerminal.context.startService(new Intent(BiliTerminal.context,DownloadService.class));
        } catch (Exception e){
            MsgUtil.err("启动下载时发生错误",e);
        }
    }
}