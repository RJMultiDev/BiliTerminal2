package com.huanli233.biliterminal2.adapter.message;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.bean.Reply;
import com.huanli233.biliterminal2.util.Utils;

public class ReplyCardHolder extends RecyclerView.ViewHolder {
    final TextView content;
    final TextView tiptext;

    public ReplyCardHolder(@NonNull View itemView) {
        super(itemView);
        content = itemView.findViewById(R.id.content);
        tiptext = itemView.findViewById(R.id.tip);
    }

    public void showReplyCard(Reply replyInfo) {
        content.setText(Utils.htmlToString(replyInfo.message));
    }
}
