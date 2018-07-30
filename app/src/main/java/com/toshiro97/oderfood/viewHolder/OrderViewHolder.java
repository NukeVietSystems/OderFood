package com.toshiro97.oderfood.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.toshiro97.oderfood.R;
import com.toshiro97.oderfood.interFace.ItemClickListener;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView tvOrderID,tvOrderStatus,tvOrderPhone,tvOrderAdress;
    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OrderViewHolder(View itemView) {
        super(itemView);
        tvOrderID = itemView.findViewById(R.id.order_id_text_view);
        tvOrderPhone = itemView.findViewById(R.id.order_phone_text_view);
        tvOrderStatus = itemView.findViewById(R.id.order_status_text_view);
        tvOrderAdress = itemView.findViewById(R.id.order_address_text_view);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
