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
            "Authorization:key=AAAAj9MZU6I:APA91bEc0F6EGdjE_fD3SEwpxRHe5o1ddiYg3JZOGk3d1unHxHwrlJXMLsrKmZd1MjxTdTZ6JVaYNGfDa-9EL_T9gnn2mk8gDl9MjTZ8vpxRBH8QtAaYcPvVPMHgr7ezH5hpgQTVHMSqMf6w0DB9IhREx2HXBhrPig"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendNotification(@Body DataMessage body);
}
