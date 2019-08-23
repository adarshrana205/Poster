package com.hfad.social_network_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
private Toolbar chattoolbar;
private ImageButton sendmessagebutton,sendimagebutton;
private EditText usermessageinput;
private RecyclerView usermessageslist;
private final List<Messages>messagesList=new ArrayList<>();
private LinearLayoutManager linearLayoutManager;
private MessagesAdapter messagesAdapter;
private String messagereceiverid,messagereceivername,messagesenderid;
private CircleImageView receiverprofileimage;
private TextView receivername,userlastseen;
private DatabaseReference rootref,UsersRef;
private FirebaseAuth mauth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mauth=FirebaseAuth.getInstance();
        messagesenderid=mauth.getCurrentUser().getUid();
        rootref= FirebaseDatabase.getInstance().getReference();
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        messagereceiverid=getIntent().getExtras().get("visit_user_id").toString();
        messagereceivername=getIntent().getExtras().get("username").toString();

        Initiallizefields();
        DisplayReceiverInfo();
        sendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        FetchMessages();
    }

    private void FetchMessages() {

        rootref.child("Messages").child(messagesenderid).child(messagereceiverid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists())
                {
                    Messages messages=dataSnapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendMessage() {

        UpdateUserStatus("online");
        String messagetext=usermessageinput.getText().toString();
        if(TextUtils.isEmpty(messagetext))
        {
            Toast.makeText(this, "Please write a message", Toast.LENGTH_SHORT).show();
        }
        else
        {
String message_sender_ref="Messages/"+ messagesenderid + "/" + messagereceiverid;
            String message_receiver_ref="Messages/"+ messagereceiverid + "/" + messagesenderid;


            DatabaseReference user_message_key=rootref.child("Messages").child(messagesenderid).child(messagereceiverid).push();
            String message_push_id=user_message_key.getKey();

            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            String saveCurrentTime = currentTime.format(calFordDate.getTime());


            HashMap messagetextbody=new HashMap();

            messagetextbody.put("message",messagetext);
            messagetextbody.put("time",saveCurrentTime);
            messagetextbody.put("date",saveCurrentDate);
            messagetextbody.put("type","text");
            messagetextbody.put("from",messagesenderid);

            HashMap messagebody=new HashMap();
messagebody.put(message_sender_ref+"/"+message_push_id ,  messagetextbody);
            messagebody.put(message_receiver_ref +"/"+message_push_id ,  messagetextbody);

            rootref.updateChildren(messagebody).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this,"Message Send successfuly",Toast.LENGTH_SHORT).show();
                        usermessageinput.setText("");
                    }
                    else
                    {
                        String message=task.getException().getMessage();
                        Toast.makeText(ChatActivity.this,"Error :"+message,Toast.LENGTH_SHORT).show();
                        usermessageinput.setText("");



                    }

                }
            });


         }
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

        UsersRef.child(messagesenderid).child("userState").updateChildren(currentStateMap);
    }

    private void DisplayReceiverInfo() {

        receivername.setText(messagereceivername);
        rootref.child("Users").child(messagereceiverid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    final String profileimage=dataSnapshot.child("profileimage").getValue().toString();

                    final String type=dataSnapshot.child("userState").child("type").getValue().toString();
                    final String lastdate=dataSnapshot.child("userState").child("date").getValue().toString();
                    final String lasttime=dataSnapshot.child("userState").child("time").getValue().toString();

                    if(type.equals("online"))
                    {
                        userlastseen.setText("online");
                    }
                    else
                    {
                        userlastseen.setText("last seen: "+ lasttime + " "+ lastdate);
                    }
                    Picasso.with(ChatActivity.this).load(profileimage).placeholder(R.drawable.profile).into(receiverprofileimage    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Initiallizefields() {

       chattoolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chattoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Messages");

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        userlastseen=(TextView)findViewById(R.id.custom_user_last_seen);

        receivername=(TextView)findViewById(R.id.custom_profile_name);
        receiverprofileimage=(CircleImageView)findViewById(R.id.custom_profile_image);

        sendimagebutton=(ImageButton)findViewById(R.id.send_image_file_button);
        sendmessagebutton=(ImageButton)findViewById(R.id.send_message_button);

        usermessageinput=(EditText)findViewById(R.id.input_message);

        messagesAdapter=new MessagesAdapter(messagesList);
        usermessageslist=(RecyclerView)findViewById(R.id.messages_list);

        linearLayoutManager=new LinearLayoutManager(this);
        usermessageslist.setHasFixedSize(true);
        usermessageslist.setLayoutManager(linearLayoutManager);
        usermessageslist.setAdapter(messagesAdapter);
        




    }
}
