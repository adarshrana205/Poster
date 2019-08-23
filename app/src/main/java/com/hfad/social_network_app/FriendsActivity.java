package com.hfad.social_network_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.nio.charset.spi.CharsetProvider;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myfriendlist;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mauth;
    private String online_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mauth = FirebaseAuth.getInstance();
        online_user_id = mauth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        myfriendlist = (RecyclerView) findViewById(R.id.friends_list);
        myfriendlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myfriendlist.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();


    }
    public void UpdateUserStatus(String state)
    {
        String savecurrentdate,savecurrentime;
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMMM dd,yyyy");
        savecurrentdate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        savecurrentime = currentTime.format(calFordDate.getTime());

        HashMap currentStateMap=new HashMap();
        currentStateMap.put("time",savecurrentime);
        currentStateMap.put("date",savecurrentdate);
        currentStateMap.put("type",state);

        UsersRef.child(online_user_id).child("userState").updateChildren(currentStateMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        UpdateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateUserStatus("offline");
    }

    private void DisplayAllFriends() {

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(FriendsRef, Friends.class)
                        .build();
        FirebaseRecyclerAdapter<Friends, friendsviewholder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, friendsviewholder>(options) {
            @NonNull
            @Override
            public friendsviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_user_display_layout, viewGroup, false);
                friendsviewholder viewHolder = new friendsviewholder(view);
                return viewHolder;
            }

            @Override
            protected void onBindViewHolder(@NonNull final friendsviewholder holder, int position, @NonNull Friends model) {
                holder.setDate(model.getDate());
                final String usersID = getRef(position).getKey();
                UsersRef.child(usersID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String username = dataSnapshot.child("fullname").getValue().toString()                            ;
                            final String profileimage = dataSnapshot.child("profileimage").getValue().toString();
final String type;
if(dataSnapshot.hasChild("userState"))
{
    type=dataSnapshot.child("userState").child("type").getValue().toString();
    if(type.equals("online"))
    {
        holder.onlinestatusview.setVisibility(View.VISIBLE);
    }
    else
    {
        holder.onlinestatusview.setVisibility(View.INVISIBLE);

    }
}
                            holder.setFullname(username);
                            holder.setProfileimage(getApplicationContext(),profileimage);
                            holder.mview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[]=new CharSequence[]
                                            {
                                                    username+" 's Profile",
                                                    "Send Message"
                                            };
                                    AlertDialog.Builder builder=new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select options");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            if(which==0)
                                            {
                                                Intent profileintent=new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                profileintent.putExtra("visit_user_id",usersID);
                                                startActivity(profileintent);
                                            }
                                            if(which==1)
                                            {

                                                Intent chatintent=new Intent(FriendsActivity.this,ChatActivity.class);
                                               chatintent.putExtra("visit_user_id",usersID);
                                                chatintent.putExtra("username",username);
                                                startActivity(chatintent);
                                            }
                                        }
                                    }) ;
                                    builder.show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        myfriendlist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class friendsviewholder extends RecyclerView.ViewHolder {
        View mview;
        ImageView onlinestatusview;

        public friendsviewholder(@NonNull View itemView) {

            super(itemView);
            mview = itemView;
            onlinestatusview=(ImageView)itemView.findViewById(R.id.all_user_online_icon);
        }

        public void setProfileimage(Context ctx, String profileimage) {
            CircleImageView myimage = (CircleImageView) mview.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myimage);
        }

        public void setFullname(String fullname) {
            TextView myname = (TextView) mview.findViewById(R.id.all_users_profile_name);
            myname.setText(fullname);
        }
        public void setDate(String date)  {
            TextView friendsdate= (TextView) mview.findViewById(R.id.all_users_status);
            friendsdate.setText("Friends since:"+ date);
        }

    }
}

