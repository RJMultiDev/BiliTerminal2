package com.RobinNotBad.BiliClient.adapter;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.BiliClient;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.PrivateMsgAdapter;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.PrivateMessage;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Cache;
import org.json.JSONException;

public class PrivateMsgAdapter extends RecyclerView.Adapter<PrivateMsgAdapter.ViewHolder>{
    private ArrayList<PrivateMessage> mPrivateMsgList=new ArrayList<PrivateMessage>();
    private long selfUid = -1;
    private Context context;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView nameTv,textContentTv,tipTv,playTimesTv,upNameTv,videoTitleTv;
        MaterialCardView textContentCard,videoCard;
        ImageView picMsg,videoCover;
        LinearLayout root;
        
        public ViewHolder(View view){
            super(view);
            root = (LinearLayout)view.findViewById(R.id.msg_layout);
            nameTv = (TextView)view.findViewById(R.id.msg_name);
            textContentTv = (TextView)view.findViewById(R.id.msg_text_content);
            tipTv = (TextView)view.findViewById(R.id.msg_type_tip_text);
            playTimesTv = (TextView)view.findViewById(R.id.listPlayTimes);
            upNameTv = (TextView)view.findViewById(R.id.listUpName);
            videoTitleTv = (TextView)view.findViewById(R.id.listVideoTitle);
            textContentCard = (MaterialCardView)view.findViewById(R.id.msg_type_text_card);
            videoCard = (MaterialCardView)view.findViewById(R.id.cardView);
            picMsg = (ImageView)view.findViewById(R.id.msg_type_pic);
            videoCover = (ImageView)view.findViewById(R.id.listCover);
        }
    }
    public PrivateMsgAdapter(ArrayList<PrivateMessage> msgList,Context context){
        mPrivateMsgList=msgList;
        this.context = context;
    }
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_private_msg,parent,false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        PrivateMessage msg = mPrivateMsgList.get(position);
        try {
            holder.nameTv.setText(msg.name);
            if(selfUid ==-1) {
            	selfUid=SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,-1);
            }
            if(msg.uid==selfUid) {
            	holder.root.setGravity(Gravity.END);
                holder.textContentCard.setCardBackgroundColor(context.getResources().getColor(R.color.pink));
                holder.textContentCard.setStrokeWidth(0);
            }else{
                holder.root.setGravity(Gravity.START);
                holder.textContentCard.setCardBackgroundColor(Color.parseColor("#78242424"));
                holder.textContentCard.setStrokeWidth(1);
            }
            switch (msg.type) {
                case PrivateMessage.TYPE_TEXT:
                    holder.textContentCard.setVisibility(View.VISIBLE);
                    holder.textContentTv.setText(msg.content.getString("content"));
                    holder.tipTv.setVisibility(View.GONE);
                    holder.picMsg.setVisibility(View.GONE);
                    holder.nameTv.setVisibility(View.VISIBLE);
                    holder.videoCard.setVisibility(View.GONE);
                    break;
                case PrivateMessage.TYPE_PIC:
                    holder.picMsg.setVisibility(View.VISIBLE);
                    holder.tipTv.setVisibility(View.GONE);
                    holder.nameTv.setVisibility(View.VISIBLE);
                    holder.textContentCard.setVisibility(View.GONE);
                    holder.videoCard.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(msg.content.getString("url"))
                            .into(holder.picMsg);
                    break;
                case PrivateMessage.TYPE_TIP:
                    holder.tipTv.setVisibility(View.VISIBLE);
                    holder.nameTv.setVisibility(View.GONE);
                    holder.picMsg.setVisibility(View.GONE);
                    holder.videoCard.setVisibility(View.GONE);
                    holder.textContentCard.setVisibility(View.GONE);
                    holder.tipTv.setText(msg.name + "撤回了一条消息");
                    break;
                case PrivateMessage.TYPE_VIDEO:
                    holder.videoCard.setVisibility(View.VISIBLE);
                    holder.nameTv.setVisibility(View.VISIBLE);
                    holder.picMsg.setVisibility(View.GONE);
                    holder.textContentCard.setVisibility(View.GONE);
                    holder.tipTv.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(msg.content.getString("thumb"))
                            .into(holder.videoCover);
                    holder.upNameTv.setText(msg.content.getString("author"));
                    holder.videoTitleTv.setText(msg.content.getString("title"));
                    break;
                default:
                    holder.textContentCard.setVisibility(View.VISIBLE);
                    holder.textContentTv.setText("暂时无法显示该消息");
                    holder.tipTv.setVisibility(View.GONE);
                    holder.picMsg.setVisibility(View.GONE);
                    holder.nameTv.setVisibility(View.VISIBLE);
                    holder.videoCard.setVisibility(View.GONE);
            }
            /*
            if(msg.type==PrivateMessage.TYPE_TEXT){
                
            }if(msg.type==PrivateMessage.TYPE_PIC){
                
            }if(msg.type==PrivateMessage.TYPE_TIP){
                
            }if(msg.type==PrivateMessage.TYPE_VIDEO){
                
            }
            */
        } catch (JSONException err) {
            Log.e(PrivateMessage.class.getName(), err.toString());
        } 
    }
    @Override 
    public int getItemCount(){
        return mPrivateMsgList.size();
    }
}
