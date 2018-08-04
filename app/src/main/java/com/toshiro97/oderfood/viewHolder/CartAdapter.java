package com.toshiro97.oderfood.viewHolder;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;
import com.toshiro97.oderfood.CartActivity;
import com.toshiro97.oderfood.R;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.database.Database;
import com.toshiro97.oderfood.interFace.ItemClickListener;
import com.toshiro97.oderfood.model.Order;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{
    private List<Order> orderList = new ArrayList<>();
    private CartActivity cartActivity;

    public CartAdapter(List<Order> orderList, CartActivity cartActivity) {
        this.orderList = orderList;
        this.cartActivity = cartActivity;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cartActivity);
        View itemView = inflater.inflate(R.layout.cart_item_layout,parent,false);
        return new CartViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(CartViewHolder holder, final int position) {
        Picasso.with(cartActivity.getBaseContext()).load(orderList.get(position).getImage()).resize(70,70).into(holder.imageCart);

        holder.btnQuantity.setNumber(orderList.get(position).getQuantity());

        holder.btnQuantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = orderList.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cartActivity).updateCart(order);

                //update total
                //calculate total price
                int total = 0;
                List<Order> orders = new Database(cartActivity).getCarts();
                for (Order item : orders)
                    total += (Integer.parseInt(order.getPrice())) * Integer.parseInt(order.getQuantity());
                Locale locale = new Locale("en", "US");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                cartActivity.totalPriceTextView.setText(fmt.format(total));
            }
        });

        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(orderList.get(position).getPrice()))*(Integer.parseInt(orderList.get(position).getQuantity()));
        holder.tvPrice.setText(fmt.format(price));
        holder.tvCartName.setText(orderList.get(position).getProductName());

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener ,View.OnCreateContextMenuListener{
    public TextView tvCartName,tvPrice;
    public ImageView imageCart;
    public ElegantNumberButton btnQuantity;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CartViewHolder(View itemView) {
        super(itemView);
        tvCartName = itemView.findViewById(R.id.cart_item_name_text_view);
        tvPrice = itemView.findViewById(R.id.cart_item_price_text_view);
        imageCart = itemView.findViewById(R.id.cart_image);
        btnQuantity = itemView.findViewById(R.id.btn_quatity);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");
        menu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}
