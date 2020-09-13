package com.example.connect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.wang.avi.AVLoadingIndicatorView;


public class LoginActivity extends AppCompatActivity {

    private TextView signUp, lets_connect, email_text, password_text, sign_up_label, or_text_label;
    private CheckBox checkBoxLogin;
    private EditText passwordInput, emailInput;
    private Button signInButton;
    private View separator1,separator2;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");

        InitializeFields();

        checkBoxLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    // show password
                    passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // hide password
                    passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserSignIn();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        avi.hide();
    }

    private void InitializeFields() {
        signUp = (TextView) findViewById(R.id.sign_up);
        lets_connect = (TextView) findViewById(R.id.lets_connect);
        email_text = (TextView) findViewById(R.id.email_text);
        password_text = (TextView) findViewById(R.id.password_text);
        sign_up_label = (TextView) findViewById(R.id.sign_up_label);

        or_text_label = (TextView) findViewById(R.id.or_text_label);
        separator1 = (View) findViewById(R.id.separator1);
        separator2 = (View) findViewById(R.id.separator2);


        checkBoxLogin = (CheckBox) findViewById(R.id.check_box_login);

        emailInput = (EditText) findViewById(R.id.user_email_input);
        passwordInput = (EditText) findViewById(R.id.user_password_input);

        signInButton = (Button) findViewById(R.id.sign_in_button);

        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
    }




    private void AllowUserSignIn() {


        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please fill e-mail and password",Toast.LENGTH_SHORT).show();
        }else {
            or_text_label.setVisibility(View.GONE);
            separator1.setVisibility(View.GONE);
            separator2.setVisibility(View.GONE);
            signUp.setVisibility(View.GONE);
            sign_up_label.setVisibility(View.GONE);
            email_text.setVisibility(View.GONE);
            emailInput.setVisibility(View.GONE);
            emailInput.setText("");
            password_text.setVisibility(View.GONE);
            passwordInput.setVisibility(View.GONE);
            passwordInput.setText("");
            checkBoxLogin.setVisibility(View.GONE);
            signInButton.setVisibility(View.GONE);
            avi.show();
            lets_connect.setText("Signing in...");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {

                                final String currentUserID = mAuth.getCurrentUser().getUid();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        String DeviceToken = instanceIdResult.getToken();
                                        Log.e("newToken",DeviceToken);

                                        UsersRef.child(currentUserID).child("device_token")
                                                .setValue(DeviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){


                                                            if(mAuth.getCurrentUser().isEmailVerified()){
                                                                SendUserToHomeActivity();
                                                                Toast.makeText(LoginActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }else{
                                                                Toast.makeText(LoginActivity.this, "Please verify your E-mail address first.", Toast.LENGTH_LONG).show();
                                                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            Toast.makeText(LoginActivity.this, "Verification E-mail sent successfully...", Toast.LENGTH_SHORT).show();
                                                                        }else{
                                                                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }
                                                                });
                                                            }


                                                        }
                                                    }
                                                });

                                    }
                                });





                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                            or_text_label.setVisibility(View.VISIBLE);
                            separator1.setVisibility(View.VISIBLE);
                            separator2.setVisibility(View.VISIBLE);
                            signUp.setVisibility(View.VISIBLE);
                            sign_up_label.setVisibility(View.VISIBLE);
                            email_text.setVisibility(View.VISIBLE);
                            emailInput.setVisibility(View.VISIBLE);
                            emailInput.setText("");
                            password_text.setVisibility(View.VISIBLE);
                            passwordInput.setVisibility(View.VISIBLE);
                            passwordInput.setText("");
                            checkBoxLogin.setVisibility(View.VISIBLE);
                            signInButton.setVisibility(View.VISIBLE);
                            avi.hide();
                            lets_connect.setText("Let's Connect");
                        }
                    });
        }


    }


    private void SendUserToHomeActivity() {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }

}