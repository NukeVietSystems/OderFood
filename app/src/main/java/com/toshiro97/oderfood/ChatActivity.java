package com.toshiro97.oderfood;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toshiro97.oderfood.adapter.ChatApdater;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.model.ChatMessenger;
import com.toshiro97.oderfood.model.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {


    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerChat)
    RecyclerView recyclerChat;
    RecyclerView.LayoutManager layoutManager;
    @BindView(R.id.edtMessenger)
    EditText edtMessenger;
    @BindView(R.id.btnSend)
    ImageView btnSend;

    FirebaseDatabase database;

    DatabaseReference referenceChat;
    List<ChatMessenger> chatMessengerList;
    ChatApdater chatApdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        database = FirebaseDatabase.getInstance();


        referenceChat = database.getReference("Chat").child(Common.staffUser.getPhone() + "-" + Common.currentUser.getPhone());
        chatMessengerList = new ArrayList<>();
        chatApdater = new ChatApdater(chatMessengerList);

        recyclerChat.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(chatApdater);
        getListChat();

    }

    @OnClick(R.id.btnSend)
    public void onViewClicked() {
        String messenger = edtMessenger.getText().toString();
        ChatMessenger chatMessengerSend = new ChatMessenger(messenger);
        chatMessengerSend.setStaff(false);
        referenceChat.push().setValue(chatMessengerSend);
        edtMessenger.setText("");
    }


    private void getListChat(){
        referenceChat.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatMessengerList.clear();
                ArrayList<ChatMessenger> chatMessengers = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatMessenger chatMessenger = snapshot.getValue(ChatMessenger.class);
                    chatMessengers.add(chatMessenger);
                }
                for (int i = chatMessengers.size() - 1; i >= 0; i--){
                    chatMessengerList.add(chatMessengers.get(i));
                }
                chatApdater.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
