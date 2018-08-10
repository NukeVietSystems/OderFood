package com.toshiro97.oderfood.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;

import com.toshiro97.oderfood.model.User;
import com.toshiro97.oderfood.remote.APIService;
import com.toshiro97.oderfood.remote.FCMRetrofitClient;
import com.toshiro97.oderfood.remote.IGoogleService;
import com.toshiro97.oderfood.remote.RetrofitGoogleAPI;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Common {


    public static String convertCodeToStatus(String status){
        if (status.equals("0"))return "Placed";
        else if (status.equals("1")) return "On my way";
        else return "Shipped";
    }
    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info != null){
                for(int i=0; i<info.length ; i++){
                    if(info[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static final String DELETE = "Delete";

    public static final String USER_KEY = "User";
    public static final String PASSWORD_KEY = "Password";
    public static final String INTENT_FOOD_ID = "FoodId";
    public static final String token_table = "Tokens";
    public static String PHONE_TEXT = "userPhone";
    public static User staffUser ;


    public static BigDecimal formatConcurrency(String amount, Locale locale) throws ParseException, java.text.ParseException {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        if(format instanceof DecimalFormat){
            ((DecimalFormat)format).setParseBigDecimal(true);
        }
        return (BigDecimal)format.parse(amount.replace("[^\\d.,]",""));
    }

    /* for Google API service**/
    public static final String googleAPIUrl = "https://maps.googleapis.com/";
    public static IGoogleService getGoogleMapsAPI(){
        return RetrofitGoogleAPI.getGoogleClient(googleAPIUrl).create(IGoogleService.class);
    }
    /**/
    /* for FCM service**/
    public static final String fcmUrl = "https://fcm.googleapis.com/";
    public static APIService getFCMService(){
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static User currentUser;
    /**/



}
