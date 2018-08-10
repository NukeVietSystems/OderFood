package com.toshiro97.oderfood.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.model.Token;


public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if(Common.currentUser != null) {
            updateToken(refreshedToken); //When have refresh tokem we need update to our Realtime database
        }

    }

    private void updateToken(String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_table);

        Token token = new Token(refreshedToken,false); //false because this token send from Clien app
        tokens.child(Common.currentUser.getPhone()).setValue(token);

    }
}
