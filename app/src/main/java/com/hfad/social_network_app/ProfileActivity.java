package com.hfad.social_network_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
private Toolbar chattoolbar;
    private CircleImageView userProfImage;
    private TextView username,userProfname,userStatus,userCountry,userGender,userRelation,userDOB;
    private FirebaseAuth mauth;
    String currentuserid;
    private DatabaseReference profusersref,friendsref,postref;
    private Button myposts,myfriends;
    private int countfriends=0,countposts=0;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        chattoolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chattoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");
        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
   profusersref= FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserid);
   friendsref=FirebaseDatabase.getInstance().getReference().child("Friends");
   postref=FirebaseDatabase.getInstance().getReference().child("Posts");


        username=(TextView) findViewById(R.id.my_profile_user_name);
        userProfname=(TextView)findViewById(R.id.my_profile_full_name);
        userStatus=(TextView)findViewById(R.id.my_profile_status);
        userCountry=(TextView)findViewById(R.id.my_profile_country);
        userRelation=(TextView)findViewById(R.id.my_profile_relation);
        userDOB=(TextView)findViewById(R.id.my_profile_dob);
        userGender=(TextView)findViewById(R.id.my_profile_gender);
        userProfImage=(CircleImageView)findViewById(R.id.my_profile_pic);
        myfriends=(Button)findViewById(R.id.my_friends_button);
        myposts=(Button)findViewById(R.id.my_post_button);


        myposts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyPostsActivity();
            }
        });

        myfriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserToFriendsActivity();

            }
        });


        postref.orderByChild("uid").startAt(currentuserid).endAt(currentuserid +"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
countposts=(int)dataSnapshot.getChildrenCount();

myposts.setText((Integer.toString(countposts) + "  Posts"));
                }
                else
                {
myposts.setText(" 0 posts");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        friendsref.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
countfriends=(int)dataSnapshot.getChildrenCount();
myfriends.setText(Integer.toString(countfriends)+ "  Friends");
                }
                else
                {
                    myfriends.setText(" 0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        profusersref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String myProfileimage=dataSnapshot.child("profileimage").getValue().toString();
                    String myusername=dataSnapshot.child("username").getValue().toString();
                    String myprofilename=dataSnapshot.child("fullname").getValue().toString();
                    String myProfilestatus=dataSnapshot.child("status").getValue().toString();
                    String myDOB=dataSnapshot.child("dob").getValue().toString();
                    String myCountry=dataSnapshot.child("country").getValue().toString();
                    String myGender=dataSnapshot.child("gender").getValue().toString();
                    String myRelation=dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.with(ProfileActivity.this).load(myProfileimage).placeholder(R.drawable.profile).into(userProfImage);
                    username.setText("@" + myusername);
                    userCountry.setText("Country: " +  myCountry);
                    userDOB.setText("DOB: " + myDOB);
                    userGender.setText("Gender: " + myGender);
                    userProfname.setText(myprofilename);
                    userRelation.setText("Relationship Status: " + myRelation);
                    userStatus.setText(myProfilestatus);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void SendUserToFriendsActivity() {
        Intent friendsintent = new Intent(ProfileActivity.this,FriendsActivity.class);
        startActivity(friendsintent);
    }
    private void SendUserToMyPostsActivity() {
        Intent postintent = new Intent(ProfileActivity.this,MyPostActivity.class);
        startActivity(postintent);
    }
}
