package com.huanli233.biliterminal2.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.activity.base.BaseActivity;
import com.huanli233.biliterminal2.listener.OnItemClickListener;
import com.huanli233.biliterminal2.listener.OnItemLongClickListener;
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomLinearManager;

import java.util.ArrayList;
import java.util.List;

public class ListChooseActivity extends BaseActivity {

    List<String> items;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simple_list);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.top_bar).setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        Intent intent = getIntent();
        if (intent.getStringExtra("title") != null) {
            ((TextView) findViewById(R.id.page_name)).setText(intent.getStringExtra("title"));
        } else {
            ((TextView) findViewById(R.id.page_name)).setText("请选择");
        }
        if (intent.getSerializableExtra("items") == null) {
            finish();
        } else {
            this.items = (List<String>) intent.getSerializableExtra("items");
        }

        Adapter adapter = new Adapter(this);
        adapter.setNameList(items);

        adapter.setOnItemClickListener((position -> {
            setResult(RESULT_OK, new Intent().putExtra("item", items.get(position)));
            finish();
        }));

        recyclerView.setLayoutManager(new CustomLinearManager(this));
        recyclerView.setAdapter(adapter);
    }

    static class Adapter extends RecyclerView.Adapter<com.huanli233.biliterminal2.adapter.QualityChooseAdapter.Holder> {

        Context context;

        List<String> nameList = new ArrayList<>();

        OnItemClickListener onItemClickListener;
        OnItemLongClickListener onItemLongClickListener;

        public Adapter(Context context) {
            this.context = context;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setNameList(List<String> newList) {
            this.nameList = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public com.huanli233.biliterminal2.adapter.QualityChooseAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_choose, parent, false);
            return new com.huanli233.biliterminal2.adapter.QualityChooseAdapter.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull com.huanli233.biliterminal2.adapter.QualityChooseAdapter.Holder holder, int position) {

            holder.folder_name.setText(nameList.get(position));

            holder.itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) onItemClickListener.onItemClick(position);
            });

            holder.itemView.setOnLongClickListener(view -> {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(position);
                    return true;
                } else return false;
            });

        }

        @Override
        public int getItemCount() {
            return nameList.size();
        }
    }
}