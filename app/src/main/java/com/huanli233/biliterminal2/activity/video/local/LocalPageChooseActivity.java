package com.huanli233.biliterminal2.activity.video.local;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.BiliTerminal;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.activity.base.InstanceActivity;
import com.huanli233.biliterminal2.adapter.video.PageChooseAdapter;
import com.huanli233.biliterminal2.api.PlayerApi;
import com.huanli233.biliterminal2.ui.widget.recycler.CustomLinearManager;
import com.huanli233.biliterminal2.util.FileUtil;
import com.huanli233.biliterminal2.util.MsgUtil;

import java.io.File;
import java.util.ArrayList;

//分页视频选集
//2023-07-17

public class LocalPageChooseActivity extends BaseActivity {

    private int longClickPosition = -1;
    private boolean deleted = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.top).setOnClickListener(view -> finish());

        TextView textView = findViewById(R.id.pageName);
        textView.setText("请选择分页");

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        ArrayList<String> pageList = intent.getStringArrayListExtra("pageList");
        ArrayList<String> videoFileList = intent.getStringArrayListExtra("videoFileList");
        ArrayList<String> danmakuFileList = intent.getStringArrayListExtra("danmakuFileList");

        PageChooseAdapter adapter = new PageChooseAdapter(this, pageList);
        adapter.setOnItemClickListener(position -> {
            try {
                Intent player = PlayerApi.jumpToPlayer(LocalPageChooseActivity.this, videoFileList.get(position), danmakuFileList.get(position), "", pageList.get(position), true, 0, "", 0, 0, 0, false);
                startActivity(player);
            } catch (ActivityNotFoundException e) {
                MsgUtil.showMsg("没有找到播放器，请检查是否安装");
            } catch (Exception e) {
                MsgUtil.error(e);
            }
        });
        adapter.setOnItemLongClickListener(position -> {
            if (longClickPosition == position) {
                File workPath = FileUtil.getDownloadPath();
                File videoPath = new File(workPath, title);
                File pagePath = new File(videoPath, pageList.get(position));

                FileUtil.deleteFolder(pagePath);
                pageList.remove(position);
                videoFileList.remove(position);
                danmakuFileList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(0, pageList.size() - position);

                if (pageList.isEmpty()) {
                    FileUtil.deleteFolder(videoPath);
                }

                MsgUtil.showMsg("删除成功");
                longClickPosition = -1;

                deleted = true;
            } else {
                longClickPosition = position;
                MsgUtil.showMsg("再次长按删除");
            }
        });

        recyclerView.setLayoutManager(new CustomLinearManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
        if (deleted && instance instanceof LocalListActivity && !instance.isDestroyed())
            ((LocalListActivity) (instance)).refresh();
        super.onDestroy();
    }
}