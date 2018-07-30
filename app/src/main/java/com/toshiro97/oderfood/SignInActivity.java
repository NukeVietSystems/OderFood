package com.toshiro97.oderfood;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;
import com.toshiro97.oderfood.common.Common;
import com.toshiro97.oderfood.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignInActivity extends AppCompatActivity {

    @BindView(R.id.edtPhone)
    EditText edtPhone;
    @BindView(R.id.edtPassword)
    EditText edtPassword;
    @BindView(R.id.btnSignIn)
    Button btnSignIn;

    DatabaseReference table_User;
    FirebaseDatabase database;
    @BindView(R.id.ckbRemember)
    CheckBox ckbRemember;
    @BindView(R.id.txtForgotPass)
    TextView txtForgotPass;

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

        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        database = FirebaseDatabase.getInstance();
        table_User = database.getReference("User");

        txtForgotPass.setPaintFlags(txtForgotPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

//        init Paper

        Paper.init(this);
    }


    @OnClick({R.id.txtForgotPass, R.id.btnSignIn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txtForgotPass:
                showForgotPassDialog();


                break;
            case R.id.btnSignIn:
                if (Common.isConnectedToInternet(getBaseContext())) {
                    //save user password
                    if (ckbRemember.isChecked()) {
                        Paper.book().write(Common.USER_KEY, edtPhone.getText().toString());
                        Paper.book().write(Common.PASSWORD_KEY, edtPassword.getText().toString());
                    }

                    final ProgressDialog mDialog = new ProgressDialog(this);
                    mDialog.setMessage("Please waiting...");
                    mDialog.show();
                    table_User.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // check nếu user đã tồn tại
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {

                                //Lây thông tin user
                                mDialog.dismiss();
                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                user.setPhone(edtPhone.getText().toString());
                                if (user.getPassword().equals(edtPassword.getText().toString())) {
                                    Toast.makeText(SignInActivity.this, "Sign in Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    Common.currentUser = user;
                                    startActivity(intent);
                                    finish();

                                    table_User.removeEventListener(this);
                                } else {
                                    Toast.makeText(SignInActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignInActivity.this, "User not exist in Database", Toast.LENGTH_SHORT).show();
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
                break;
        }
    }

    private void showForgotPassDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter your secure code");

        LayoutInflater inflater = this.getLayoutInflater();
        View forgotPassView = inflater.inflate(R.layout.forgot_password_layout,null);

        builder.setView(forgotPassView);
        builder.setIcon(R.drawable.ic_security_black_24dp);
        final EditText edPhone = forgotPassView.findViewById(R.id.edtPhone);
        final EditText edSecureCode = forgotPassView.findViewById(R.id.edtSecureCode);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                table_User.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(edPhone.getText().toString()).getValue(User.class);
                        if (user.getSecureCode().equals(edSecureCode.getText().toString())){
                            Toast.makeText(SignInActivity.this, "Your Password "+ user.getPassword(), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(SignInActivity.this, "Wrong secure code !", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
