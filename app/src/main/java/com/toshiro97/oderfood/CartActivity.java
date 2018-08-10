package com.toshiro97.oderfood;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.common.Config;
import com.toshiro97.oderfood.database.Database;
import com.toshiro97.oderfood.helper.RecyclerItemTouchHelper;
import com.toshiro97.oderfood.interFace.RecyclerItemTouchHelperListener;
import com.toshiro97.oderfood.model.DataMessage;
import com.toshiro97.oderfood.model.FCMResponse;
import com.toshiro97.oderfood.model.Order;
import com.toshiro97.oderfood.model.Request;
import com.toshiro97.oderfood.model.Token;
import com.toshiro97.oderfood.model.User;
import com.toshiro97.oderfood.remote.APIService;
import com.toshiro97.oderfood.remote.IGoogleService;
import com.toshiro97.oderfood.viewHolder.CartAdapter;
import com.toshiro97.oderfood.viewHolder.CartViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CartActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, RecyclerItemTouchHelperListener {
    private static final String TAG = "CartActivity";
    private static final int PAYPAL_REQUEST_CODE = 9999;
    private static final int LOCATION_REQUEST_CODE = 9998;
    private static final int PLAY_SERVICES_REQUEST = 9997;

    //Declare Google map Api Retrofit
    IGoogleService mGoogleMapService;
    APIService mService;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView totalPriceTextView;
    Button placeOrderButton;

    List<Order> orders = new ArrayList<>();
    CartAdapter adapter;

    Place shippingAddress;

    //Paypal payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) //use Sandbox because we test, changeit late for you
            .clientId(Config.PAYPAL_CLIENT_ID);

    String address, comment;

    //Location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;

    RelativeLayout rootLayout;

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

        setContentView(R.layout.activity_cart);

        rootLayout = findViewById(R.id.rootLayout);
        rootLayout.setBackgroundResource(R.drawable.background);

        //Init google api service
        mGoogleMapService = Common.getGoogleMapsAPI();

        //Init firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Initi Paypal
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        mService = Common.getFCMService();

        //Runtime permission
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        } else {
            if(checkPlayServices()){ //if have play services on device
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        //Init
        recyclerView = findViewById(R.id.list_cart_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        totalPriceTextView = findViewById(R.id.totalPrice_text_view);
        placeOrderButton = findViewById(R.id.place_order_button);

        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(orders.size() > 0) {
                    showAlertDialog();
                } else {
                    Toast.makeText(CartActivity.this, "Your orders is empty!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(Common.isConnectedToInternet(this)) {
            loadFoodList();
        } else {
            Toast.makeText(this, "Check your internet connection!!", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
            return;
        } else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {

                Log.d(TAG, "displayLocation: " + mLastLocation.getLatitude()+","+mLastLocation.getLongitude());

                final double latitude = mLastLocation.getLatitude();
                final double longitude = mLastLocation.getLongitude();


            } else {
                Log.d(TAG, "displayLocation: Cannot get your location");
            }
        }
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
//                        displayLocation();
                    }
                }
                break;
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        final LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

//        final MaterialEditText addressEditText = order_address_comment.findViewById(R.id.order_address_edit_text);
        final PlaceAutocompleteFragment addressEdit = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //Hide search icon before fragment
        addressEdit.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //Set hint for Autocomplete Edit Text
        ((EditText)addressEdit.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address");
        //Set Text size
        ((EditText)addressEdit.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);
        //get address from PlaceAutocomplete
        addressEdit.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.d(TAG, "onError: "+status.getStatusMessage());
            }
        });

        final MaterialEditText commentEditText = order_address_comment.findViewById(R.id.order_comment_edit_text);

        //Radio
        final RadioButton shipToAddressRadioButton = order_address_comment.findViewById(R.id.ship_to_address_radio_button);
        final RadioButton homeAddressRadioButton = order_address_comment.findViewById(R.id.home_address_radio_button);
        final RadioButton codRadioButton = order_address_comment.findViewById(R.id.cod_radio_button);
        final RadioButton paypalRadioButton = order_address_comment.findViewById(R.id.paypal_radio_button);
        final RadioButton appBalanceRadioButton = order_address_comment.findViewById(R.id.app_balance_radio_button);

        //Event Radio
        homeAddressRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Common.currentUser.getHomeAdress() != null || !TextUtils.isEmpty(Common.currentUser.getHomeAdress())){

                        address = Common.currentUser.getHomeAdress();
                        //Set this address to edit text
                        ((EditText)addressEdit.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);

                    }
                }
            }
        });
        shipToAddressRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Ship to this address features
                if(isChecked){
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=false",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
//                    mGoogleMapService.getAddressName("https://maps.googleapis.com/maps/api/geocode/json?latlng=-12.143023,-77.0093788&sensor=false")
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    //If fetchAPI ok
                                    try {
                                        Log.d(TAG, "onResponse: RESPONSE: " +response.body());
                                        JSONObject jsonObject = new JSONObject(response.body());
                                        JSONArray resultsArray = jsonObject.getJSONArray("results");
                                        JSONObject firstObject = resultsArray.getJSONObject(0);
                                        address = firstObject.getString("formatted_address");
                                        //Set this address to edit text
                                        ((EditText)addressEdit.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);

                                    } catch (NullPointerException e) { //atrapar el error y mostrarlo y q no crashee app.
                                        Log.d(TAG, "onResponse: NullPointerException: " + e.getMessage());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(CartActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }
            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Add check condition here
                //If user select address from Place Fragment, just use it
                //If user select Ship to this address, get Address from location and use it
                //If user select Home addres, get addres grom Profile and use it
                if(!shipToAddressRadioButton.isChecked() && !homeAddressRadioButton.isChecked()){
                    //If both radio is not selected ->
                    if(shippingAddress != null) {
                        address = shippingAddress.getAddress().toString();
                    } else {
                        Toast.makeText(CartActivity.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show();
                        //Fix crash fragment (Remove fragment)
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }
                }

                if(TextUtils.isEmpty(address)){
                    Toast.makeText(CartActivity.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show();
                    //Fix crash fragment (Remove fragment)
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }

                comment = commentEditText.getText().toString();

                //check payment
                if(!codRadioButton.isChecked() && !paypalRadioButton.isChecked() && !appBalanceRadioButton.isChecked()) {//If both Cod and appbalance and Paypal is not checked.

                    Toast.makeText(CartActivity.this, "Please select payment option", Toast.LENGTH_SHORT).show();
                    //Fix crash fragment (Remove fragment)
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                } else if (paypalRadioButton.isChecked()) {

                    String formatAmount = totalPriceTextView.getText().toString()
                            .replace("$", "")
                            .replace(",", "");

                    //Show Paypal to payment
                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount),
                            "USD",
                            "Order App",
                            PayPalPayment.PAYMENT_INTENT_SALE);
                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);

                } else if (codRadioButton.isChecked()) {

                    //Copy code from onActivityresult
                    //Create new Request
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            totalPriceTextView.getText().toString(),
                            comment,
                            "COD",
                            "Unpaid",
                            String.format("%s %s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),//Coordinates when user order.
                            orders
                    );

                    //Submit to Firebase
                    //We will using System.currentMilli to key
                    String order_number = String.valueOf(System.currentTimeMillis());
                    requests.child(order_number)
                            .setValue(request);
                    //Delete Cart
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                    sendNotificationOrder(order_number);
                    Toast.makeText(CartActivity.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                    finish();

                } else if(appBalanceRadioButton.isChecked()){

                    double amount = 0;
                    //firts, we will get total price from txtTotalprice
                    try {
                        amount = Common.formatConcurrency(totalPriceTextView.getText().toString(),Locale.US).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //After rerecive total price of this order, just comparte with user balance
                    if(Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount){

                        //Create new Request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                totalPriceTextView.getText().toString(),
                                comment,
                                "App Balance",
                                "Paid",
                                String.format("%s %s", mLastLocation.getLatitude(), mLastLocation.getLongitude()),//Coordinates when user order.
                                orders
                        );

                        //Submit to Firebase
                        //We will using System.currentMilli to key
                        final String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number)
                                .setValue(request);
                        //Delete Cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                        //Update balance
                        double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
                        Map<String,Object> update_balance = new HashMap<>();
                        update_balance.put("balance",balance);

                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(Common.currentUser.getPhone())
                                .updateChildren(update_balance)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            //Refresh user
                                            FirebaseDatabase.getInstance().getReference("Users")
                                                    .child(Common.currentUser.getPhone())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                            //Send order to server
                                                            sendNotificationOrder(order_number);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                });

                    } else {
                        Toast.makeText(CartActivity.this, "You balance not enough, please choose other payment.", Toast.LENGTH_SHORT).show();
                    }
                }

                //Remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });

        alertDialog.show();

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirmation != null){
                    try{
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);
                        String paymentState = jsonObject.getJSONObject("response").getString("state");//State from JSON

                        //Create new Request
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                totalPriceTextView.getText().toString(),
                                comment,
                                "Paypal",
                                paymentState,
                                String.format("%s %s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
                                orders
                        );

                        //Submit to Firebase
                        //We will using System.currentMilli to key
                        String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number)
                                .setValue(request);
                        //Delete Cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                        sendNotificationOrder(order_number);
                        Toast.makeText(CartActivity.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                        finish();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Payment canceled.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this, "Invalid payment.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("isServerToken").equalTo(true); //get all node with isServerToken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                    Token serverToken = postSnapshot.getValue(Token.class);


                    Map<String,String> dataSend = new HashMap<>();
                    dataSend.put("title", "Order Food");
                    dataSend.put("message", "You have new order: " +order_number);
                    DataMessage dataMessage = new DataMessage(serverToken.getToken(),dataSend);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<FCMResponse>() {
                                @Override
                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                                    //Only run when get result
                                    if(response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(CartActivity.this, "Thank you. Order placed.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(CartActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                    Log.d(TAG, "onFailure: " + t.getMessage());
                                }
                            });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadFoodList() {
        orders = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(orders,this);
        //adapter.notifyDataSetChanged(); //no needed because user not add items while he see his orders.
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total = 0;
        for(Order order: orders){
            total += (Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        }
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        totalPriceTextView.setText(fmt.format(total));
    }
    //Press ctl+O

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE)){
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int position) {
        //We will remove item at List<Order> by position
        orders.remove(position);
        //After that,, we will delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //And final, we will update new data from List<Order>  to SQLite
        for(Order item: orders){
            new Database(this).addToCart(item);
            //Refresh
            loadFoodList();
        }
        Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof CartViewHolder){

            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());

            //Update extTotal
            //Calculate total price
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for(Order item: orders){
                total += (Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
            }
            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            totalPriceTextView.setText(fmt.format(total));

            //Make snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //Update extTotal
                    //Calculate total price
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for(Order item: orders){
                        total += (Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    }
                    Locale locale = new Locale("en", "US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                    totalPriceTextView.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}

