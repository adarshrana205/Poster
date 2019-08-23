package com.hfad.social_network_app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private RecyclerView mypostslist;
    private FirebaseAuth mauth;
    private DatabaseReference PostsRef,UsersRef,LikesRef;
    private String currentuserid;
    Boolean LikeChecker=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
PostsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mtoolbar=(Toolbar)findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");
        mypostslist=(RecyclerView)findViewById(R.id.my_all_posts_list);

mypostslist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mypostslist.setLayoutManager(linearLayoutManager);


        DisplaymyallPosts();
    }

    private void DisplaymyallPosts() {
        Query mypostsquery=PostsRef.orderByChild("uid").startAt(currentuserid).endAt(currentuserid +"\uf8ff");
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(mypostsquery, Posts.class)
                        .build();

        FirebaseRecyclerAdapter<Posts,MyPostViewHolder>firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Posts, MyPostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyPostViewHolder holder, int position, @NonNull Posts model) {

                final String PostKey=getRef(position).getKey();
                holder.setFullname(model.getFullname());
              holder.setTime(model.getTime());
              holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setProfileimage(getApplicationContext(), model.getProfileimage());
                holder.setPostimage(getApplicationContext(), model.getPostimage());

                holder.setlikebuttonstatus(PostKey);
                holder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickpostIntent = new Intent(MyPostActivity.this,ClickpostActivity.class);
                        clickpostIntent.putExtra(  "PostKey",PostKey);
                        startActivity(clickpostIntent);
                    }
                });
               holder.commentpostbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(MyPostActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra(  "PostKey",PostKey);
                        startActivity(commentsIntent);
                    }
                });
                holder.likepostbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker=true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(LikeChecker.equals(true))
                                {
                                    if(dataSnapshot.child(PostKey).hasChild(currentuserid))
                                    {
                                        LikesRef.child(PostKey).child(currentuserid).removeValue();
                                        LikeChecker=false;
                                    }
                                    else
                                    {
                                        LikesRef.child(PostKey).child(currentuserid).setValue(true);
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

            @NonNull
            @Override
            public MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_posts_layout,viewGroup,false);
                MyPostViewHolder viewHolder=new MyPostViewHolder(view);
                return viewHolder;
            }
        };
       mypostslist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }



    public  static class MyPostViewHolder extends RecyclerView.ViewHolder
    {
View mview;
        View mView;
        ImageButton likepostbutton,commentpostbutton;
        TextView displaylikes;
        String currentuserid;
        DatabaseReference LikesRef;
        public MyPostViewHolder(@NonNull View itemView) {
            super(itemView);

            mview=itemView;
            likepostbutton=(ImageButton)mview.findViewById(R.id.like_button);
            commentpostbutton=(ImageButton)mview.findViewById(R.id.comment_button);
            displaylikes=(TextView)mview.findViewById(R.id.display_no_of_likes);
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
            TextView username = (TextView) mview.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mview.findViewById(R.id.post_user_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }
        public void setTime(String time)
        {
            TextView PostTime = (TextView)mview.findViewById(R.id.post_time);
            PostTime.setText(" " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView)mview.findViewById(R.id.post_date);
            PostDate.setText(" " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mview.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1,  String postimage)
        {
            ImageView PostImage = (ImageView)mview.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }
    }
}
