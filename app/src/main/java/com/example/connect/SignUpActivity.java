package com.example.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, nameInput;
    private TextView email_text,password_text,name_text;
    private Button signUpButton;
    private CheckBox checkbox;

    private int followers = 0, posts = 0, following = 0;

    private AVLoadingIndicatorView avi;

    private static final int GalleryPic = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        InitializeFields();

        avi.hide();

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });


        
    }



    private void CreateNewAccount() {

        final String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        final String username = nameInput.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please fill all of the above fields",Toast.LENGTH_SHORT).show();
        }else{
            email_text.setVisibility(View.GONE);
            emailInput.setVisibility(View.GONE);
            emailInput.setText("");
            password_text.setVisibility(View.GONE);
            passwordInput.setVisibility(View.GONE);
            passwordInput.setText("");
            checkbox.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);
            name_text.setVisibility(View.GONE);
            nameInput.setVisibility(View.GONE);
            avi.setVisibility(View.VISIBLE);
            avi.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();

                                HashMap<String, Object> profileMap = new HashMap<>();
                                profileMap.put("email", email);
                                profileMap.put("followers", followers);
                                profileMap.put("following", following);
                                profileMap.put("about", "Hi there! I am using Connect.");
                                profileMap.put("posts", posts);
                                profileMap.put("uid", currentUserID);
                                profileMap.put("name", username);

                                RootRef.child("users").child(currentUserID).updateChildren(profileMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    //SendUserToMainActivity();
                                                    Toast.makeText(SignUpActivity.this, "Account created successfully...", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    String message = task.getException().toString();
                                                    Toast.makeText(SignUpActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        email_text.setVisibility(View.VISIBLE);
                                        emailInput.setVisibility(View.VISIBLE);
                                        emailInput.setText("");
                                        password_text.setVisibility(View.VISIBLE);
                                        passwordInput.setVisibility(View.VISIBLE);
                                        passwordInput.setText("");
                                        checkbox.setVisibility(View.VISIBLE);
                                        signUpButton.setVisibility(View.VISIBLE);
                                        name_text.setVisibility(View.VISIBLE);
                                        nameInput.setVisibility(View.VISIBLE);
                                        avi.hide();
                                        avi.setVisibility(View.GONE);

                                        if(task.isSuccessful()){
                                            SendUserToLoginActivity();
                                            Toast.makeText(SignUpActivity.this,"An E-mail has been sent to you. Please click the link given in it to verify your E-mail address.", Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });


                            }else {
                                email_text.setVisibility(View.VISIBLE);
                                emailInput.setVisibility(View.VISIBLE);
                                emailInput.setText("");
                                password_text.setVisibility(View.VISIBLE);
                                passwordInput.setVisibility(View.VISIBLE);
                                passwordInput.setText("");
                                checkbox.setVisibility(View.VISIBLE);
                                signUpButton.setVisibility(View.VISIBLE);
                                name_text.setVisibility(View.VISIBLE);
                                nameInput.setVisibility(View.VISIBLE);
                                avi.hide();
                                avi.setVisibility(View.GONE);
                                String message = task.getException().toString();
                                Toast.makeText(SignUpActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }


                        }
                    });


        }

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void InitializeFields() {

        emailInput = (EditText) findViewById(R.id.user_email_input);
        passwordInput = (EditText) findViewById(R.id.user_password_input);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        checkbox = (CheckBox) findViewById(R.id.check_box);
        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        email_text = (TextView) findViewById(R.id.email_text);
        password_text = (TextView) findViewById(R.id.password_text);
        name_text = (TextView) findViewById(R.id.name_text);
        nameInput = (EditText) findViewById(R.id.user_name_input);
    }

}