package com.toshiro97.oderfood.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.toshiro97.oderfood.R;
import com.toshiro97.oderfood.interFace.ItemClickListener;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView foodNameTextView, foodPriceTextView;
    public ImageView foodImageView, favoriteImageView, shareImageView, quickCartImageView;

    private ItemClickListener itemClickListener;



    public FoodViewHolder(View itemView) {
        super(itemView);

        foodNameTextView = itemView.findViewById(R.id.food_name_text_view);
        foodImageView = itemView.findViewById(R.id.food_image_view);
        favoriteImageView = itemView.findViewById(R.id.fav_image);
        shareImageView = itemView.findViewById(R.id.share_button_image_view);
        foodPriceTextView = itemView.findViewById(R.id.food_price_text_view);
        quickCartImageView = itemView.findViewById(R.id.fav_quick_cart_image_view);


        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v, getAdapterPosition(), false);

    }
}
