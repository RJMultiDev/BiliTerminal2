package com.huanli233.biliterminal2.adapter.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.video.series.UserSeriesActivity;
import com.huanli233.biliterminal2.bean.VideoCardKt;
import com.huanli233.biliterminal2.util.TerminalContext;

import java.util.List;

//用户视频列表专用Adapter

public class UserVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Context context;
    final long mid;
    final List<VideoCardKt> videoCardList;

    public UserVideoAdapter(Context context, long mid, List<VideoCardKt> videoCardList) {
        this.context = context;
        this.mid = mid;
        this.videoCardList = videoCardList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.cell_goto, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        } else {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false);
            return new VideoCardHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            TextView textView = holder.itemView.findViewById(R.id.text);
            textView.setText("视频系列");
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, UserSeriesActivity.class);
                intent.putExtra("mid", mid);
                context.startActivity(intent);
            });
        } else {
            int realPosition = position - 1;
            VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
            VideoCardKt videoCard = videoCardList.get(realPosition);
            videoCardHolder.bindData(videoCard, context);    //此函数在VideoCardHolder里

            holder.itemView.setOnClickListener(view -> TerminalContext.getInstance().enterVideoDetailPage(context, videoCard.getAid(), videoCard.getBvid()));
        }
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof DynamicHolder) ((DynamicHolder) holder).extraCard.removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return videoCardList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? 0 : 1);
    }
}
