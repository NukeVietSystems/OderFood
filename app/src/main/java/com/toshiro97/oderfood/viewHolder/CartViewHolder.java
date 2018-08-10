package com.toshiro97.oderfood.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.toshiro97.oderfood.R;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.interFace.ItemClickListener;

public class CartViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener{

    public TextView cartNameTextView, cartPriceTextView;
    public ElegantNumberButton quantityCartButton;
    public ImageView cartImageView;

    public RelativeLayout backgroundView;
    public LinearLayout foregroundView;


    private ItemClickListener itemClickListener;

    public void setCartNameTextView(TextView cartNameTextView) {
        this.cartNameTextView = cartNameTextView;
    }


    public CartViewHolder(View itemView) {
        super(itemView);
        cartNameTextView = itemView.findViewById(R.id.cart_item_name_text_view);
        cartPriceTextView = itemView.findViewById(R.id.cart_item_price_text_view);
        quantityCartButton = itemView.findViewById(R.id.quantity_cart_button);
        cartImageView = itemView.findViewById(R.id.cart_image);
        backgroundView = itemView.findViewById(R.id.background_view_relative_layout);
        foregroundView = itemView.findViewById(R.id.foreground_view_linear_layout);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select action");
        menu.add(0,0,getAdapterPosition(), Common.DELETE);
    }
}
