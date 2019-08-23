package com.hfad.social_network_app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.util.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {


    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mtoolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,PostsRef,LikesRef;
    String currentUserID;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;
    Boolean LikeChecker=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
            currentUserID = currentUser.getUid();
        }
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Home");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        AddNewPostButton = (ImageButton) findViewById(R.id.new_post_button);


        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        postList = (RecyclerView) findViewById(R.id.all_user_posts_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    } else {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });
        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });
        DisplayAllUsersPosts();
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

         UsersRef.child(currentUserID).child("userState").updateChildren(currentStateMap);
     }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void DisplayAllUsersPosts() {

        Query sortpostindescendingorder=PostsRef.orderByChild("counter");
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(sortpostindescendingorder, Posts.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(
                options

        ) {
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                PostsViewHolder viewHolder=new PostsViewHolder(view);
                return viewHolder; }

            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder viewHolder, int position,  Posts model) {
               final String PostKey=getRef(position).getKey();
                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(), model.getPostimage());
                viewHolder.setlikebuttonstatus(PostKey);
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickpostIntent = new Intent(MainActivity.this,ClickpostActivity.class);
                         clickpostIntent.putExtra(  "PostKey",PostKey);
                        startActivity(clickpostIntent);
                    }
                });
                viewHolder.commentpostbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra(  "PostKey",PostKey);
                        startActivity(commentsIntent);
                    }
                });

                viewHolder.likepostbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
LikeChecker=true;
LikesRef.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
       if(LikeChecker.equals(true))
       {
           if(dataSnapshot.child(PostKey).hasChild(currentUserID))
           {
               LikesRef.child(PostKey).child(currentUserID).removeValue();
               LikeChecker=false;
           }
           else
           {
               LikesRef.child(PostKey).child(currentUserID).setValue(true);
               LikeChecker=false;
           }
       }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});
                    }
                });
            }



        };
        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        UpdateUserStatus("online");
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton likepostbutton,commentpostbutton;
        TextView displaylikes;
        String currentuserid;
        DatabaseReference LikesRef;

        public PostsViewHolder(View itemView)
        {

            super(itemView);
            mView = itemView;
            likepostbutton=(ImageButton)mView.findViewById(R.id.like_button);
            commentpostbutton=(ImageButton)mView.findViewById(R.id.comment_button);
            displaylikes=(TextView)mView.findViewById(R.id.display_no_of_likes);
            LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
            currentuserid=FirebaseAuth.getInstance().getCurrentUser().getUid();

        }
        public void setlikebuttonstatus(final String Postkey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(Postkey).hasChild(currentuserid))
                    {

                        int countLikes = (int) dataSnapshot.child(Postkey).getChildrenCount();
                        likepostbutton.setImageResource(R.drawable.like);
                        displaylikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }
                    else
                    {
                        int countLikes = (int) dataSnapshot.child(Postkey).getChildrenCount();
                        likepostbutton.setImageResource(R.drawable.dislike);
                        displaylikes.setText((Integer.toString(countLikes)+(" Likes")));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_user_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText(" " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText(" " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1,  String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence() {
        final String current_user_id=mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id))

                {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




    private boolean UserMenuSelector(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_post:
               SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                SendUserToFriendsActivity();
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                SendUserFindFriendsActivity();
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                SendUserToFriendsActivity();
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                UpdateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
return false;
    }
    private void SendUserToFriendsActivity() {
        Intent friendsintent = new Intent(MainActivity.this,FriendsActivity.class);
        startActivity(friendsintent);
    }
    private void SendUserFindFriendsActivity() {
        Intent findfriends = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findfriends);
    }

    private void SendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);

    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

}
