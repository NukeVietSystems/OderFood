package com.toshiro97.oderfood;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.database.Database;
import com.toshiro97.oderfood.interFace.ItemClickListener;
import com.toshiro97.oderfood.model.Favorite;
import com.toshiro97.oderfood.model.Food;
import com.toshiro97.oderfood.model.Order;
import com.toshiro97.oderfood.viewHolder.FoodViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodListActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference foodList;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    String categoryId = "";

    //Search functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    SwipeRefreshLayout swipeRefreshLayout;

    //Create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create Photo from Bitmap
            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if(ShareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(sharePhoto)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    //Press crtl+O
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Note: add this code before setContentView method
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.TTF")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_food_list);

        //INit facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Init firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        localDB = new Database(this);

        swipeRefreshLayout = findViewById(R.id.swipe_layout_food);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Get intent here
                if(getIntent() != null) {
                    categoryId = getIntent().getStringExtra("CategoryID");
                }
                if(!categoryId.isEmpty() && categoryId != null){
                    //check internet connection and load foods list
                    if(Common.isConnectedToInternet(getBaseContext())) {
                        loadListFood(categoryId);
                    }  else {
                        Toast.makeText(FoodListActivity.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //Get intentn here
                if(getIntent() != null){
                    categoryId = getIntent().getStringExtra("CategoryID");
                    if(!categoryId.isEmpty() && categoryId != null){
                        //check internet connection and load foods list
                        if(Common.isConnectedToInternet(getBaseContext())) {
                            loadListFood(categoryId);
                        }  else {
                            Toast.makeText(FoodListActivity.this, "Please check your connection!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Because search functionality need catefory so we need paste code here
                    //After getIntent categoryId
                    //After getIntent categoryId

                    //Search
                    materialSearchBar = findViewById(R.id.search_bar);
                    materialSearchBar.setHint("Enter your foods");
                    materialSearchBar.setSpeechMode(false);
                    loadSuggest(); //Write fuinction to load suggest grom Firebase
                    materialSearchBar.setCardViewElevation(10);
                    materialSearchBar.addTextChangeListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            //Then user type their text, we will change suggest list

                            List<String> suggest = new ArrayList<>();
                            for(String search : suggestList){
                                if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())){
                                    suggest.add(search);
                                }
                            }
                            materialSearchBar.setLastSuggestions(suggest);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                    materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                        @Override
                        public void onSearchStateChanged(boolean enabled) {
                            //When search bar is close
                            //Restore original suggest adapter
                            if(!enabled){
                                recyclerView.setAdapter(adapter);
                            }
                        }

                        @Override
                        public void onSearchConfirmed(CharSequence text) {
                            //When search finish
                            //Show result of search adapter
                            startSearch(text);
                        }

                        @Override
                        public void onButtonClicked(int buttonCode) {

                        }
                    });
                }
            }
        });

        //Load menu
        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);



    }

    private void startSearch(CharSequence text) {
        //Create query by name
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());
        //Create Options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName,Food.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.foodNameTextView.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.foodImageView);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener(){
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new activiyt
                        Intent intent = new Intent(FoodListActivity.this, FoodDetailActivity.class);
                        intent.putExtra("foodId", searchAdapter.getRef(position).getKey()); //send foodId to new activity
                        startActivity(intent);
                    }
                });
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };

        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter); //Set adapter for Recycler View is Search result.
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName()); //Add name of foods
                        }

                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {
        //Create query by category Id
        Query searchByCategory = foodList.orderByChild("menuId").equalTo(categoryId);
        //Create Options with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByCategory,Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {
                viewHolder.foodNameTextView.setText(model.getName());
                viewHolder.foodPriceTextView.setText(String.format("$ %s", model.getPrice()));
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(viewHolder.foodImageView);

                //Quick orders

                viewHolder.quickCartImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isFoodExists = new Database(getBaseContext()).checkIfFoodExists(adapter.getRef(position).getKey(),Common.currentUser.getPhone());

                        if(!isFoodExists) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()
                            ));

                        } else {

                            new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(position).getKey());
                        }
                        Toast.makeText(FoodListActivity.this, "Added to Cart.", Toast.LENGTH_SHORT).show();
                    }
                });

                //Add favorites
                if(localDB.isFavorite(adapter.getRef(position).getKey(), Common.currentUser.getPhone())){
                    viewHolder.favoriteImageView.setImageResource(R.drawable.ic_favorite_black_24dp); //poner corazon redondo.
                }

                //Click to Share
                viewHolder.shareImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(FoodListActivity.this, "Click work", Toast.LENGTH_SHORT).show();
                        Picasso.with(getBaseContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                //Click to change status of Favorite
                viewHolder.favoriteImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorite favorite = new Favorite();
                        favorite.setFoodId(adapter.getRef(position).getKey());
                        favorite.setFoodName(model.getName());
                        favorite.setFoodDescription(model.getDescription());
                        favorite.setFoodDiscount(model.getDiscount());
                        favorite.setFoodImage(model.getImage());
                        favorite.setFoodMenuId(model.getMenuId());
                        favorite.setUserPhone(Common.currentUser.getPhone());
                        favorite.setFoodPrice(model.getPrice());

                        if(!localDB.isFavorite(adapter.getRef(position).getKey(),Common.currentUser.getPhone())){
                            localDB.addToFavorites(favorite);
                            viewHolder.favoriteImageView.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodListActivity.this, model.getName()+" was added to favorites!", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                            viewHolder.favoriteImageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodListActivity.this, model.getName()+" was remove from favorites.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                viewHolder.setItemClickListener(new ItemClickListener(){
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new activiyt
                        Intent intent = new Intent(FoodListActivity.this, FoodDetailActivity.class);
                        intent.putExtra("FoodID", adapter.getRef(position).getKey()); //send foodId to new activity
                        startActivity(intent);
                    }
                });
            }


            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        if(searchAdapter != null) {
            searchAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Fix click back on Food detail and get no item in Food list
        if(adapter != null){
            adapter.startListening();
        }
    }
}

