package com.toshiro97.oderfood;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.edtName)
    EditText edtName;
    @BindView(R.id.edtPhone)
    EditText edtPhone;
    @BindView(R.id.edtPassword)
    EditText edtPassword;
    @BindView(R.id.btnSignUp)
    Button btnSignUp;

    DatabaseReference table_User;
    FirebaseDatabase database;
    @BindView(R.id.edtSecureCode)
    EditText edtSecureCode;

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

        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        table_User = database.getReference("User");
    }

    @OnClick(R.id.btnSignUp)
    public void onViewClicked() {
        if (Common.isConnectedToInternet(getBaseContext())) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Please waiting...");
            mDialog.show();

            table_User.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                        mDialog.dismiss();
                        Toast.makeText(SignUpActivity.this, "Phone number already register", Toast.LENGTH_SHORT).show();
                    } else {
                        mDialog.dismiss();
                        User user = new User(edtName.getText().toString(), edtPassword.getText().toString(),edtSecureCode.getText().toString());
                        table_User.child(edtPhone.getText().toString()).setValue(user);
                        Toast.makeText(SignUpActivity.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(this, "Please check your connection !!!", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
