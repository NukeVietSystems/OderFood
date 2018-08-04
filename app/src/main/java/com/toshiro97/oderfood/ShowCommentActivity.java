package com.toshiro97.oderfood;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.model.Rating;
import com.toshiro97.oderfood.viewHolder.ShowCommentViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowCommentActivity extends AppCompatActivity {
    private static final String TAG = "ShowCommentActivity";

    @BindView(R.id.recycer_comment)
    RecyclerView recycerComment;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTable;

    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;

    String foodId = "";
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.TTF")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_show_comment);
        setContentView(R.layout.activity_show_comment);
        ButterKnife.bind(this);


        //Init firebase
        database = FirebaseDatabase.getInstance();
        ratingTable = database.getReference("Rating");

        layoutManager = new LinearLayoutManager(this);
        recycerComment.setLayoutManager(layoutManager);

        //Swipe Layout
        mSwipeRefreshLayout = findViewById(R.id.swipe_layout_comment);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Get intentn here
                if (getIntent() != null) {
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                }
                if (!foodId.isEmpty() && foodId != null) {
                    //create request query
                    Query query = ratingTable.orderByChild("foodId").equalTo(foodId);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder holder, int position, @NonNull Rating model) {
                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.commentTextView.setText(model.getComment());
                            holder.userPhoneTextView.setText(model.getUserPhone());
                        }

                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item_layout, parent, false);
                            return new ShowCommentViewHolder(view);
                        }
                    };

                    loadComment(foodId);
                }
            }
        });

        //Thread to load comment on first launch
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

                //Get intentn here
                if (getIntent() != null) {
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                }
                if (!foodId.isEmpty() && foodId != null) {
                    //create request query
                    Query query = ratingTable.orderByChild("foodId").equalTo(foodId);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder holder, int position, @NonNull Rating model) {
                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.commentTextView.setText(model.getComment());
                            holder.userPhoneTextView.setText(model.getUserPhone());
                        }

                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item_layout, parent, false);
                            return new ShowCommentViewHolder(view);
                        }
                    };

                    loadComment(foodId);
                }
            }
        });
    }

    private void loadComment(String foodId) {
        adapter.startListening();
        recycerComment.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null){
            adapter.stopListening();
        }
    }
}
