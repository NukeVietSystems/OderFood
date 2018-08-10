package com.toshiro97.oderfood.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.toshiro97.oderfood.R;
import com.toshiro97.oderfood.model.ChatMessenger;

import java.util.List;


class ChatSend extends RecyclerView.ViewHolder{
    public TextView tvSend;
    public ChatSend(View itemView) {
        super(itemView);
        tvSend = itemView.findViewById(R.id.tvSend);
    }
}

class ChatReceive extends RecyclerView.ViewHolder{
    public TextView tvReceive;
    public ImageView imgAvatarRecei;
    public ChatReceive(View itemView) {
        super(itemView);
        tvReceive = itemView.findViewById(R.id.tvReceive);
        imgAvatarRecei = itemView.findViewById(R.id.img_avatar_receive);
    }
}

public class ChatApdater extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    List<ChatMessenger> messengerList;
    Context context;

    public ChatApdater(List<ChatMessenger> messengerList) {
        this.messengerList = messengerList;
    }

    @Override
    public int getItemViewType(int position) {
        if (messengerList.get(position).isStaff())
            return 0;
        else
            return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        if (viewType == 0){
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.list_item_message_recv,parent,false);
            return new ChatReceive(view);
        }else {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.list_item_message_send,parent,false);
            return new ChatSend(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case 0:{
                ChatReceive viewHolder = (ChatReceive)holder;
                ChatMessenger chatMessenger = messengerList.get(position);
                viewHolder.setIsRecyclable(false);
                viewHolder.tvReceive.setText(chatMessenger.getMessenger());
                viewHolder.imgAvatarRecei.setImageResource(R.drawable.customer);
            }
                break;
            case 1:{
                ChatSend viewHolder = (ChatSend) holder;
                ChatMessenger chatMessenger = messengerList.get(position);
                viewHolder.setIsRecyclable(false);
                viewHolder.tvSend.setText(chatMessenger.getMessenger());
            }
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messengerList.size();
    }
}
