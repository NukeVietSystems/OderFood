package com.toshiro97.oderfood;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.common.Config;
import com.toshiro97.oderfood.database.Database;
import com.toshiro97.oderfood.model.Order;
import com.toshiro97.oderfood.model.Request;
import com.toshiro97.oderfood.viewHolder.CartAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CartActivity extends AppCompatActivity {

    private static final int PAYPAL_REQUEST_CODE = 111;
    //Paypal payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) // sandbox with test
            .clientId(Config.PAYPAL_CLIENT_ID);
    @BindView(R.id.list_cart_recycler)
    RecyclerView listCartRecycler;
    @BindView(R.id.totalPrice_text_view)
    public TextView totalPriceTextView;
    @BindView(R.id.place_order_button)
    Button placeOrderButton;
    @BindView(R.id.rootLayout)
    RelativeLayout rootLayout;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;
    String address, comment;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/login_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);

        //init paypal
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //initView
        listCartRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listCartRecycler.setLayoutManager(layoutManager);

        loadListCard();
    }

    private void loadListCard() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        listCartRecycler.setAdapter(adapter);


        //calculate total price
        int total = 0;
        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice())) * Integer.parseInt(order.getQuantity());
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        totalPriceTextView.setText(fmt.format(total));

    }

    @OnClick(R.id.place_order_button)
    public void onViewClicked() {
        if (cart.size() > 0) {
            showAlertDialog();
        } else {
            Toast.makeText(this, "Your cart is empty !!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("One more step !");
        alertDialog.setMessage("Enter your address: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View orderAdressComment = inflater.inflate(R.layout.order_address_comment, null);

        final EditText edtAddress = orderAdressComment.findViewById(R.id.edtAddress);
        final EditText edtComment = orderAdressComment.findViewById(R.id.edtComment);

        alertDialog.setView(orderAdressComment);

        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //show paypal

                address = edtAddress.getText().toString();
                comment = edtComment.getText().toString();

                String formatAmout = totalPriceTextView.getText().toString()
                        .replace("$", "")
                        .replace(",", "");

                PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmout),
                        "USD",
                        "Order Food",
                        PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                startActivityForResult(intent, PAYPAL_REQUEST_CODE);
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                totalPriceTextView.getText().toString(),
                                "0",
                                comment,
                                jsonObject.getJSONObject("response").getString("state"),
                                cart
                        );
//                      submit to firebase

                        requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);
//                      delete cart

                        new Database(getBaseContext()).cleanCart();
                        Toast.makeText(CartActivity.this, "Thank you, waiting our !", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (requestCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "Payment cancel !!!", Toast.LENGTH_SHORT).show();
            }
            else if (requestCode == PaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this, "Invalid payment", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE)) {
            deleteCart(item.getOrder());
        }

        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);
        //delete in sqlite
        new Database(this).cleanCart();
        //update
        for (Order item : cart) {
            new Database(this).addToCart(item);
        }
        loadListCard();
    }
}
