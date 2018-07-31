package com.toshiro97.oderfood;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.toshiro97.mylibrary.utils.FButton;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.database.Database;
import com.toshiro97.oderfood.model.Food;
import com.toshiro97.oderfood.model.Order;
import com.toshiro97.oderfood.model.Rating;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetailActivity extends AppCompatActivity implements RatingDialogListener {

    @BindView(R.id.food_image_view)
    ImageView foodImageView;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.cart_button_fab)
    CounterFab cartButtonFab;
    @BindView(R.id.food_name_text_view)
    TextView foodNameTextView;
    @BindView(R.id.food_price_text_view)
    TextView foodPriceTextView;
    @BindView(R.id.number_button)
    ElegantNumberButton numberButton;
    @BindView(R.id.food_desc_text_view)
    TextView foodDescTextView;

    String foodId = "";
    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;
    Food currentFood;
    @BindView(R.id.rating_button_fab)
    FloatingActionButton ratingButtonFab;
    @BindView(R.id.rating_bar)
    RatingBar ratingBar;
    @BindView(R.id.show_comments_button)
    Button showCommentsButton;
    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/login_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_food_detail);
        ButterKnife.bind(this);

        //firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingTbl = database.getReference("Rating");

        //Initview
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

//        count item
        cartButtonFab.setCount(new Database(this).getCountCart());
        //get food id from intent
        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("FoodID");
            if (!foodId.isEmpty()) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    getDetailFood(foodId);
                    getRatingFood(foodId);
                } else {
                    Toast.makeText(this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    private void getRatingFood(String foodId) {
        Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0 ;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRateValue());
                    count ++;
                }
                if (count != 0){
                    float average = sum /count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(foodImageView);
                collapsingToolbar.setTitle(currentFood.getName());
                foodPriceTextView.setText(currentFood.getPrice());
                foodNameTextView.setText(currentFood.getName());
                foodDescTextView.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @OnClick({R.id.cart_button_fab, R.id.rating_button_fab})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cart_button_fab:
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount()
                ));

                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rating_button_fab:
                showRatingDialog();
                
                break;
        }
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very bad","Not good", "Quite ok", "Very good", "Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this foods")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetailActivity.this)
                .show();
    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        //get rating and upload to firebase
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()){
                    //remove old value
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //update value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }else {
                    //update value
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
                Toast.makeText(FoodDetailActivity.this, "Thank you for submit rating", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
