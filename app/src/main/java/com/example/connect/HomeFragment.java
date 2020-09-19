package com.example.connect;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {

    private RecyclerView homeRecyclerView;
    private DatabaseReference postsRef;
    private View homeFragmentView;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private ArrayList<String> myFollowingIds;

    public HomeFragment() {
        //Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        homeFragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");

        InitializeArrayList();

        homeRecyclerView = homeFragmentView.findViewById(R.id.homeRecyclerView);





            FirebaseRecyclerOptions<feedPost> options =
                    new FirebaseRecyclerOptions.Builder<feedPost>()
                            .setQuery(postsRef, feedPost.class)
                            .build();


            FirebaseRecyclerAdapter<feedPost, HomeFragment.HomeViewHolder> adapter =
                    new FirebaseRecyclerAdapter<feedPost, HomeFragment.HomeViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final HomeFragment.HomeViewHolder holder, int position, @NonNull feedPost model) {

                            final String postPushIds = getRef(position).getKey();
                            //final String[] retImage = {"default_image"};

                            //myFollowingIds.contains()

                                postsRef.child(postPushIds).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot snapshot) {

                                        if (snapshot.exists() && myFollowingIds.contains(snapshot.child("uid").getValue().toString())) {


                                            if (snapshot.hasChild("postMessageImage")) {
                                                String postMessageImage = snapshot.child("postMessageImage").getValue().toString();
                                                Picasso.get().load(postMessageImage).into(holder.eachPostMessageImage);
                                            }else{
                                                holder.eachPostMessageImage.setVisibility(View.GONE);
                                            }

                                            final String retName = snapshot.child("userName").getValue().toString();
                                            final String retProfileImage = snapshot.child("postProfileImage").getValue().toString();
                                            final String retDateAndTime = snapshot.child("dateAndTime").getValue().toString();
                                            final String retText = snapshot.child("postMessageText").getValue().toString();
                                            final String retLikes = snapshot.child("likes").getValue().toString();
                                            final String retPostPushID = snapshot.child("postPushID").getValue().toString();
                                            // ret = retrieve

                                            final ArrayList<String> likedByList = new ArrayList<>();
                                            postsRef.child(postPushIds).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.hasChild("likedBy")){
                                                        DataSnapshot idsSnapshot = snapshot.child("likedBy");
                                                        Iterable<DataSnapshot> followingIdsChildren = idsSnapshot.getChildren();
                                                        for(DataSnapshot id : followingIdsChildren){
                                                            String data = id.getValue().toString();
                                                            likedByList.add(data);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                            holder.eachPostPeopleName.setText(retName);
                                            holder.eachPostDateAndTime.setText(retDateAndTime);
                                            holder.eachPostMessageText.setText(retText);
                                            holder.eachPostLikes.setText(retLikes + "❤");


                                            if(!retProfileImage.equals("no_img")){
                                                Picasso.get().load(retProfileImage).into(holder.eachPostProfileImage);
                                            }


                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    if(likedByList.contains(currentUserID)){
                                                        postsRef.child(retPostPushID).child("likes")
                                                                .setValue(String.valueOf( Integer.parseInt(retLikes)-1 ))
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        postsRef.child(retPostPushID).child("likedBy").child(currentUserID).removeValue();
                                                                    }
                                                                });

                                                        //holder.eachPostLikes.setText(String.valueOf( Integer.parseInt(retLikes)-1 ) + "❤");

                                                        likedByList.remove(currentUserID);
                                                    }else {
                                                        postsRef.child(retPostPushID).child("likes")
                                                                .setValue(String.valueOf( Integer.parseInt(retLikes)+1 ))
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        postsRef.child(retPostPushID).child("likedBy").child(currentUserID).setValue(currentUserID);
                                                                    }
                                                                });

                                                        //holder.eachPostLikes.setText(String.valueOf( Integer.parseInt(retLikes)+1 ) + "❤");

                                                        likedByList.add(currentUserID);
                                                    }
                                                }
                                            });



                                        }else {
                                            holder.eachPostConstraintLayout.setVisibility(View.GONE);
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
                        public HomeFragment.HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_feed, parent, false);
                            return new HomeFragment.HomeViewHolder(view);
                        }
                    };
            homeRecyclerView.setAdapter(adapter);
            adapter.startListening();

        //}


        return homeFragmentView;
    }

    private void InitializeArrayList() {
        myFollowingIds = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(currentUserID).addValueEventListener(new ValueEventListener() {
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


    public static class HomeViewHolder extends RecyclerView.ViewHolder{

        CircleImageView eachPostProfileImage;
        ImageView eachPostMessageImage;
        TextView eachPostPeopleName, eachPostDateAndTime, eachPostMessageText, eachPostLikes;
        ConstraintLayout eachPostConstraintLayout;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);

            eachPostProfileImage = itemView.findViewById(R.id.each_post_image);
            eachPostMessageImage = itemView.findViewById(R.id.feed_image);
            eachPostPeopleName = itemView.findViewById(R.id.each_post_name_text_view);
            eachPostDateAndTime = itemView.findViewById(R.id.each_post_date_and_time_text_view);
            eachPostMessageText = itemView.findViewById(R.id.feed_text);
            eachPostLikes = itemView.findViewById(R.id.feed_likes);
            eachPostConstraintLayout = itemView.findViewById(R.id.each_feed_parent_constraint_layout);
        }
    }

}