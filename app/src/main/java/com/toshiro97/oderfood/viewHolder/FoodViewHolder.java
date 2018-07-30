package com.toshiro97.oderfood.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.toshiro97.oderfood.R;
import com.toshiro97.oderfood.interFace.ItemClickListener;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView foodName,food_price;

    public ImageView foodImage,fav_image,share_image,add_cart;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView) {
        super(itemView);

        foodName = itemView.findViewById(R.id.food_name);
        foodImage = itemView.findViewById(R.id.food_image_view);
        fav_image = itemView.findViewById(R.id.fav);
        share_image = itemView.findViewById(R.id.share_image);
        food_price = itemView.findViewById(R.id.food_price);
        add_cart = itemView.findViewById(R.id.quick_cart);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
