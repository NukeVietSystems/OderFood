package com.toshiro97.oderfood.remote;

import com.toshiro97.oderfood.model.DataMessage;
import com.toshiro97.oderfood.model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;


public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA30rZRkk:APA91bF5-JNmO89v0jdX_Ynq3QqtEe9Hvcp2nBsqQVPVv_6NQb4kkb3H5xKoB1668jjzeK9e28WmVW0wxHsAe7m5oL0H9xGMJGfHn7kJYmcnpWZF0YPCkZfrQ8Ax0gAFKe-CDHW3c2xo"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendNotification(@Body DataMessage body);
}
