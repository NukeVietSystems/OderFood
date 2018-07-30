package com.toshiro97.oderfood;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.toshiro97.oderfood.model.Request;
import com.toshiro97.oderfood.viewHolder.OrderViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OrderStatusActivity extends AppCompatActivity {

    @BindView(R.id.list_orders_recycler)
    RecyclerView listOrdersRecycler;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference request;

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

        setContentView(R.layout.activity_order_status);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        request = database.getReference("Requests");

        //initView
        listOrdersRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listOrdersRecycler.setLayoutManager(layoutManager);

        if (getIntent() == null){
            loadOrder(Common.currentUser.getPhone());
        }else {

            loadOrder(getIntent().getStringExtra("userPhone"));
        }
    }

    private void loadOrder(String phone) {
        Query getOrderByUser = request.orderByChild("phone")
                .equalTo(phone);
        FirebaseRecyclerOptions<Request> orderOptions = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(getOrderByUser,Request.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(orderOptions) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull Request model) {
                viewHolder.tvOrderID.setText(adapter.getRef(position).getKey());
                viewHolder.tvOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.tvOrderPhone.setText(model.getPhone());
                viewHolder.tvOrderAdress.setText(model.getAdress());
            }

            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.order_item_layout,parent,false);
                return new OrderViewHolder(itemView);
            }
        };
        adapter.startListening();
        listOrdersRecycler.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
