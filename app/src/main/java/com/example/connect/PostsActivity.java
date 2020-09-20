package com.example.connect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsActivity extends AppCompatActivity {

    private Toolbar MyPostToolbar;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String whosePostsUserId;
    private DatabaseReference postsRef;
    private RecyclerView myPostsRecyclerView;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        whosePostsUserId = intent.getExtras().get("userId").toString();

        InitializeFields();







        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");



        FirebaseRecyclerOptions<feedPost> options =
                new FirebaseRecyclerOptions.Builder<feedPost>()
                        .setQuery(postsRef, feedPost.class)
                        .build();


        FirebaseRecyclerAdapter<feedPost, MyPostsViewHolder> adapter =
                new FirebaseRecyclerAdapter<feedPost, MyPostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MyPostsViewHolder holder, int position, @NonNull feedPost model) {

                        final String postPushIds = getRef(position).getKey();


                        postsRef.child(postPushIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot snapshot) {

                                if (snapshot.exists() && snapshot.child("uid").getValue().toString().equals(whosePostsUserId)) {


                                    if (snapshot.hasChild("postMessageImage")) {
                                        String postMessageImage = snapshot.child("postMessageImage").getValue().toString();
                                        Picasso.get().load(postMessageImage).into(holder.myPostMessageImage);
                                    }else{
                                        holder.myPostMessageImage.setVisibility(View.GONE);
                                    }

                                    final String retName = snapshot.child("userName").getValue().toString();
                                    final String retProfileImage = snapshot.child("postProfileImage").getValue().toString();
                                    final String retDateAndTime = snapshot.child("dateAndTime").getValue().toString();
                                    final String retText = snapshot.child("postMessageText").getValue().toString();
                                    final String retLikes = snapshot.child("likes").getValue().toString();
                                    final String retPostPushID = snapshot.child("postPushID").getValue().toString();
                                    // ret = retrieve


                                    holder.myPostPeopleName.setText(retName);
                                    holder.myPostDateAndTime.setText(retDateAndTime);
                                    holder.myPostMessageText.setText(retText);
                                    holder.myPostLikes.setText(retLikes + " Likes");


                                    if(!retProfileImage.equals("no_img")){
                                        Picasso.get().load(retProfileImage).into(holder.myPostProfileImage);
                                    }

                                    if(whosePostsUserId.equals(currentUserId)) {
                                        holder.myPostDelete.setVisibility(View.VISIBLE);
                                        holder.myPostDelete.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                builder.setMessage("Delete this post ?")
                                                        .setCancelable(true)
                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                DeletePost(retPostPushID);
                                                            }
                                                        })
                                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        });
                                                builder.show();
                                            }
                                        });
                                    }



                                }else {
                                    holder.myPostConstraintLayout.setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        //}

                    }

                    @NonNull
                    @Override
                    public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_feed, parent, false);
                        return new MyPostsViewHolder(view);
                    }
                };
        myPostsRecyclerView.setAdapter(adapter);
        adapter.startListening();







    }

    private void DeletePost(String retPostPushID) {
        final int[] postsNumber = new int[1];
        postsRef.child(retPostPushID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                FirebaseDatabase.getInstance().getReference().child("users")
                        .child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postsNumber[0] = Integer.parseInt(snapshot.child("posts").getValue().toString());
                        postsNumber[0] = postsNumber[0] - 1;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                FirebaseDatabase.getInstance().getReference().child("users")
                        .child(currentUserId).child("posts").setValue(String.valueOf(postsNumber[0]));

            }
        });
    }

    private void InitializeFields() {
        MyPostToolbar = findViewById(R.id.my_post_toolbar);
        setSupportActionBar(MyPostToolbar);
        getSupportActionBar().setTitle("My Posts");

        myPostsRecyclerView = findViewById(R.id.myPostsRecyclerView);
        //we have set layout manager in activity_posts.xml

        builder = new AlertDialog.Builder(PostsActivity.this, R.style.AlertDialogTheme);
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder{

        CircleImageView myPostProfileImage;
        ImageView myPostMessageImage;
        TextView myPostPeopleName, myPostDateAndTime, myPostMessageText, myPostLikes, myPostDelete;
        ConstraintLayout myPostConstraintLayout;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);

            myPostProfileImage = itemView.findViewById(R.id.each_post_image);
            myPostMessageImage = itemView.findViewById(R.id.feed_image);
            myPostPeopleName = itemView.findViewById(R.id.each_post_name_text_view);
            myPostDateAndTime = itemView.findViewById(R.id.each_post_date_and_time_text_view);
            myPostMessageText = itemView.findViewById(R.id.feed_text);
            myPostLikes = itemView.findViewById(R.id.feed_likes);
            myPostDelete = itemView.findViewById(R.id.feed_delete);
            myPostConstraintLayout = itemView.findViewById(R.id.each_feed_parent_constraint_layout);
        }
    }

}