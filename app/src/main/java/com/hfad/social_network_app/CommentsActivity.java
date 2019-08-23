package com.hfad.social_network_app;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {


    private ImageButton postcommentbutton;
    private RecyclerView commentslist;
    private EditText commentinputtext;
    private String post_key;
    private DatabaseReference UsersRef,PostsRef  ;
    private String current_user_id;
    private FirebaseAuth mauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        post_key=getIntent().getExtras().get("PostKey").toString();
        mauth=FirebaseAuth.getInstance();
        current_user_id=mauth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_key).child("Comments");
        commentslist=(RecyclerView)findViewById(R.id.comments_list);
        commentslist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentslist.setLayoutManager(linearLayoutManager);

        commentinputtext=(EditText)findViewById(R.id.comment_input);
        postcommentbutton=(ImageButton)findViewById(R.id.post_comment_button);

        postcommentbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
         if(dataSnapshot.exists())
         {
             String username=dataSnapshot.child("username").getValue().toString();


                validatecomment(username);
                commentinputtext.setText("");
         }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(PostsRef, Comments.class)
                        .build();
        FirebaseRecyclerAdapter<Comments,CommentsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                holder.setUsername(model.getUsername());
                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout,viewGroup,false);
               CommentsViewHolder viewHolder=new CommentsViewHolder(view);
                return viewHolder;
            }
        };
        commentslist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
public static class CommentsViewHolder extends RecyclerView.ViewHolder
{
    View mView;
    public CommentsViewHolder(@NonNull View itemView) {

        super(itemView);
        mView=itemView;
    }
    public void setUsername(String username)
    {
        TextView myusername=(TextView)mView.findViewById(R.id.comment_username);
        myusername.setText("@"+ username+" ");
    }
    public void setComment(String comment)
    {
        TextView mycomment=(TextView)mView.findViewById(R.id.comment_text);
        mycomment.setText(comment);
    }
    public void setTime(String time)
    {
        TextView mytime=(TextView)mView.findViewById(R.id.comment_time);
        mytime.setText(" Time: "+time);
    }
    public void setDate(String date)
    {
        TextView mydate=(TextView)mView.findViewById(R.id.comment_date);
        mydate.setText("   Date: "+date);
    }
}
    private void validatecomment(String username) {
        String commenttext=commentinputtext.getText().toString();
        if(TextUtils.isEmpty(commenttext))
        {
            Toast.makeText(this,"Please enter the comment",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());
        final String random_key=current_user_id+saveCurrentDate+saveCurrentTime;
            HashMap commentsmap=new HashMap();
            commentsmap.put("uid",current_user_id);
            commentsmap.put("comment",commenttext);
            commentsmap.put("date",saveCurrentDate);
            commentsmap.put("time",saveCurrentTime);
            commentsmap.put("username",username);
PostsRef.child(random_key).updateChildren(commentsmap).addOnCompleteListener(new OnCompleteListener() {
    @Override
    public void onComplete(@NonNull Task task) {
        if(task.isSuccessful())
        {
            Toast.makeText(CommentsActivity.this,"you have commented successfully",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(CommentsActivity.this,"rror occured",Toast.LENGTH_SHORT).show();
        }

    }
});
        }

    }
}
