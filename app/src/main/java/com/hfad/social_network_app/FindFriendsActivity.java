package com.hfad.social_network_app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private ImageButton searchbutton;
    private EditText searchinputtext;
    private RecyclerView searchresultlist;
    private DatabaseReference allusersdatabaseref;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        mAuth = FirebaseAuth.getInstance();
        allusersdatabaseref= FirebaseDatabase.getInstance().getReference().child("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
        searchbutton=(ImageButton)findViewById(R.id.search_people_friends_button);
        searchinputtext=(EditText)findViewById(R.id.search_box_input);
        searchresultlist=(RecyclerView)findViewById(R.id.search_result_list);
        searchresultlist.setHasFixedSize(true);
        searchresultlist.setLayoutManager(new LinearLayoutManager(this));

    searchbutton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String searchboxinput=searchinputtext.getText().toString();
        SearchPeople(searchboxinput);
        }
    });
    }

    private void SearchPeople(String searchboxinput)
    {
        Toast.makeText(FindFriendsActivity.this,"Searching.....",Toast.LENGTH_LONG).show();

        Query searchfriendsquery = allusersdatabaseref.orderByChild("fullname").startAt(searchboxinput).endAt(searchboxinput + "\uf8ff");
        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchfriendsquery,FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(
                options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull FindFriends model) {
                holder.setFullname(model.getFullname());
                holder.setStatus(model.getStatus());
                holder.setProfileimage(getApplicationContext(),model.getProfileimage());

                holder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String  visit_user_id=getRef(position).getKey();

                        Intent profileintent =new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                        profileintent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileintent);
                    }
                });
            }



            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_user_display_layout,viewGroup,false);
                FindFriendsViewHolder viewHolder=new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        searchresultlist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mview;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
        }
        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView myimage =(CircleImageView)mview.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myimage) ;
        }
        public void setFullname(String fullname)
        {
            TextView myname=(TextView)mview.findViewById(R.id.all_users_profile_name);
            myname.setText(fullname);
        }
        public void setStatus(String status)
        {
            TextView mystatus=(TextView)mview.findViewById(R.id.all_users_status);
            mystatus.setText(status);
        }
    }
}
