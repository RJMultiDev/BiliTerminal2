package com.huanli233.biliterminal2.adapter.video;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.video.series.SeriesInfoActivity;
import com.huanli233.biliterminal2.bean.Series;
import com.huanli233.biliterminal2.bean.VideoCardKt;

import java.util.List;

public class SeriesCardAdapter extends RecyclerView.Adapter<VideoCardHolder> {
    final Context context;
    final List<Series> seasonList;

    public SeriesCardAdapter(Context context, List<Series> seasonList) {
        this.context = context;
        this.seasonList = seasonList;
    }

    @NonNull
    @Override
    public VideoCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false);
        return new VideoCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoCardHolder holder, int position) {
        Series series = seasonList.get(position);
        VideoCardKt videoCard = VideoCardKt.of(series.title, series.intro, series.total, series.cover, 0, "", "series");
        holder.bindData(videoCard, context);    //此函数在VideoCardHolder里

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, SeriesInfoActivity.class);
            intent.putExtra("type", series.type);
            intent.putExtra("mid", series.mid);
            intent.putExtra("sid", series.id);
            intent.putExtra("name", series.title);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return seasonList.size();
    }
}
