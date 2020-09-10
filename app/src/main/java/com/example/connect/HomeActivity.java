package com.example.connect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private ChipNavigationBar chipNavigationBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        mAuth = FirebaseAuth.getInstance();
        
        InitializeFields();

        chipNavigationBar.setItemSelected(R.id.home, true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        BottomMenu(); //for handling clicks on chipNavigationBar


    }

    private void BottomMenu() {

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i){
                    case R.id.home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.people:
                        fragment = new PeopleFragment();
                        break;
                    case R.id.post:
                        fragment = new PostFragment();
                        break;
                    case R.id.me:
                        fragment = new MeFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });

    }


    private void InitializeFields() {
        chipNavigationBar = findViewById(R.id.bottom_nav_menu);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            // that is, currentUser has no data,i.e, not created his account yet
            SendUserToLoginActivity();
        }else {
            if (!currentUser.isEmailVerified()) {
                Toast.makeText(this,"Please verify your E-mail first",Toast.LENGTH_LONG).show();
                mAuth.signOut();
                SendUserToLoginActivity();
            }
        }
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

}