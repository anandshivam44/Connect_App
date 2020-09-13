package com.example.connect;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class MeFragment extends Fragment {

    private FirebaseAuth mAuth;
    public View meFragmentView;
    private CircleImageView myProfileImage;
    public TextView logout, settings, myUsername, myAbout, myFollowersNo, myFollowingNo, myPostsNo;
    private RelativeLayout postsRelativeLayout,followingRelativeLayout;
    private DatabaseReference usersRef;
    private String currentUserId;

    public MeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        meFragmentView = inflater.inflate(R.layout.fragment_me, container, false);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        InitializeFields();

        UpdateProfile();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                SendUserToLoginActivity();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        postsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent postsIntent = new Intent(getContext(), PostsActivity.class);
                startActivity(postsIntent);
            }
        });

        followingRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent followingIntent = new Intent(getContext(), FollowingActivity.class);
                startActivity(followingIntent);
            }
        });

        myProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if((snapshot.exists()) && (snapshot.hasChild("image"))){
                            String ProfileImageUrl = snapshot.child("image").getValue().toString();

                            Intent imageViewerIntent = new Intent(getContext(), ImageViewerActivity.class);
                            imageViewerIntent.putExtra("imageUrl", ProfileImageUrl);
                            startActivity(imageViewerIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        return meFragmentView;
    }


    private void UpdateProfile() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){
                    String retrieveProfileImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(retrieveProfileImage).into(myProfileImage);
                }
                if(snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    myUsername.setText(name);
                    String about = snapshot.child("about").getValue().toString();
                    myAbout.setText(about);
                    String followers = snapshot.child("followers").getValue().toString();
                    myFollowersNo.setText(followers);
                    String following = snapshot.child("following").getValue().toString();
                    myFollowingNo.setText(following);
                    String posts = snapshot.child("posts").getValue().toString();
                    myPostsNo.setText(posts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(getContext(), LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeFields() {
        logout = meFragmentView.findViewById(R.id.log_out);
        settings = meFragmentView.findViewById(R.id.settings);
        myUsername = meFragmentView.findViewById(R.id.user_name);
        myAbout = meFragmentView.findViewById(R.id.user_about);
        myFollowersNo = meFragmentView.findViewById(R.id.followers_no_text_view);
        myFollowingNo = meFragmentView.findViewById(R.id.following_no_text_view);
        myPostsNo = meFragmentView.findViewById(R.id.posts_no_text_view);

        followingRelativeLayout = meFragmentView.findViewById(R.id.following_relative_layout);
        postsRelativeLayout = meFragmentView.findViewById(R.id.posts_relative_layout);

        myProfileImage = meFragmentView.findViewById(R.id.user_image);
    }
}