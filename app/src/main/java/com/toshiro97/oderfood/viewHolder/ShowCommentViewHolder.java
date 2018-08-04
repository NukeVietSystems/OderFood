package com.toshiro97.oderfood.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.toshiro97.oderfood.R;


public class ShowCommentViewHolder extends RecyclerView.ViewHolder {

    public TextView userPhoneTextView, commentTextView;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(View itemView) {
        super(itemView);

        userPhoneTextView = itemView.findViewById(R.id.user_phone_text_view);
        commentTextView = itemView.findViewById(R.id.comment_text_view);
        ratingBar = itemView.findViewById(R.id.comment_rating_bar);

    }

}
