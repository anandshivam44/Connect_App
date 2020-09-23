package com.hncc.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class PeopleFragment extends Fragment {

    private RecyclerView peopleRecyclerView;
    private DatabaseReference usersRef;
    private View peopleFragmentView;

    private FirebaseAuth mAuth;
    private String currentUserID;

    public PeopleFragment() {
        //Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        peopleFragmentView = inflater.inflate(R.layout.fragment_people, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        peopleRecyclerView = (RecyclerView) peopleFragmentView.findViewById(R.id.peopleRecyclerView);
        peopleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return peopleFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<person> options =
                new FirebaseRecyclerOptions.Builder<person>()
                        .setQuery(usersRef, person.class)
                        .build();


        FirebaseRecyclerAdapter<person, PeopleViewHolder> adapter =
                new FirebaseRecyclerAdapter<person, PeopleViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final PeopleViewHolder holder, int position, @NonNull person model) {

                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(snapshot.exists()){

//                                    if (snapshot.hasChild("image")){
//                                        retImage[0] = snapshot.child("image").getValue().toString();
//                                        Picasso.get().load(retImage[0]).into(holder.eachPeopleProfileImage);
//                                    }

                                    final String retName = snapshot.child("name").getValue().toString();
                                    final String retFollowers = String.valueOf(snapshot.child("followers").getValue());
                                    // ret = retrieve

                                    holder.eachPeopleName.setText(retName);
                                    holder.eachPeopleFollower.setText("Followers: " + retFollowers);



                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent peopleProfileIntent = new Intent(getContext(), PeopleProfileActivity.class);
                                            peopleProfileIntent.putExtra("visit_user_id",usersIDs);
                                            startActivity(peopleProfileIntent);
                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public PeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_people, parent, false);
                        return new PeopleViewHolder(view);
                    }
                };
        peopleRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }




    public static class PeopleViewHolder extends RecyclerView.ViewHolder{

        //CircleImageView eachPeopleProfileImage;
        TextView eachPeopleName, eachPeopleFollower;

        public PeopleViewHolder(@NonNull View itemView) {
            super(itemView);

            //eachPeopleProfileImage = itemView.findViewById(R.id.each_people_image);
            eachPeopleName = itemView.findViewById(R.id.each_people_name_text_view);
            eachPeopleFollower = itemView.findViewById(R.id.each_people_followers_text_view);
        }
    }

}