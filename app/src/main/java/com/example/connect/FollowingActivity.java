package com.example.connect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowingActivity extends AppCompatActivity {

    private Toolbar followingsToolbar;
    private RecyclerView followingsRecyclerView;
    private ArrayList<String> myFollowingIds;
    private String peopleProfileUserId;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        Intent intent = getIntent();
        peopleProfileUserId = intent.getExtras().get("following_user_id").toString();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        InitializeFields();

        InitializeArrayList(peopleProfileUserId);

    }





    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<person> options =
                new FirebaseRecyclerOptions.Builder<person>()
                        .setQuery(usersRef, person.class)
                        .build();


        FirebaseRecyclerAdapter<person, followingPeopleViewHolder> adapter =
                new FirebaseRecyclerAdapter<person, followingPeopleViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final followingPeopleViewHolder holder, int position, @NonNull person model) {

                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.exists() && myFollowingIds.contains(snapshot.child("uid").getValue().toString())){

                                    if (snapshot.hasChild("image")){
                                        retImage[0] = snapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.eachPeopleProfileImage);
                                    }

                                    final String retName = snapshot.child("name").getValue().toString();
                                    final String retFollowers = String.valueOf(snapshot.child("followers").getValue());
                                    // ret = retrieve

                                    holder.eachPeopleName.setText(retName);
                                    holder.eachPeopleFollower.setText("Followers: " + retFollowers);



                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent peopleProfileIntent = new Intent(FollowingActivity.this, PeopleProfileActivity.class);
                                            peopleProfileIntent.putExtra("visit_user_id",usersIDs);
                                            startActivity(peopleProfileIntent);
                                        }
                                    });

                                }else {
                                    holder.eachPeopleLayout.setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public followingPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_people, parent, false);
                        return new followingPeopleViewHolder(view);
                    }
                };
        followingsRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }




    public static class followingPeopleViewHolder extends RecyclerView.ViewHolder{

        CircleImageView eachPeopleProfileImage;
        TextView eachPeopleName, eachPeopleFollower;
        ConstraintLayout eachPeopleLayout;

        public followingPeopleViewHolder(@NonNull View itemView) {
            super(itemView);

            eachPeopleProfileImage = itemView.findViewById(R.id.each_people_image);
            eachPeopleName = itemView.findViewById(R.id.each_people_name_text_view);
            eachPeopleFollower = itemView.findViewById(R.id.each_people_followers_text_view);
            eachPeopleLayout = itemView.findViewById(R.id.each_people_constraint_layout);
        }
    }






    private void InitializeArrayList(String peopleProfileUserId) {

        myFollowingIds = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(peopleProfileUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("followingIds")){
                    DataSnapshot idsSnapshot = snapshot.child("followingIds");
                    Iterable<DataSnapshot> followingIdsChildren = idsSnapshot.getChildren();
                    for(DataSnapshot id : followingIdsChildren){
                        String data = id.getValue().toString();
                        myFollowingIds.add(data);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {

        followingsToolbar = findViewById(R.id.followings_toolbar);
        setSupportActionBar(followingsToolbar);
        getSupportActionBar().setTitle("Followings");

        followingsRecyclerView = findViewById(R.id.followingsRecyclerView);
        //we have set layout manager in activity_posts.xml

    }
}