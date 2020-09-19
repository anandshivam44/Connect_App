package com.example.connect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class PostsActivity extends AppCompatActivity {

    private Toolbar MyPostToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        InitializeFields();
    }

    private void InitializeFields() {
        MyPostToolbar = findViewById(R.id.my_post_toolbar);
        setSupportActionBar(MyPostToolbar);
        getSupportActionBar().setTitle("My Posts");
    }
}