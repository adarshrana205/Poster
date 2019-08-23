package com.hfad.social_network_app;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private CircleImageView userProfImage;
    private TextView username,userProfname,userStatus,userCountry,userGender,userRelation,userDOB;
private Button sendbutton,declinebutton;
    private DatabaseReference FriendRequestRef,Usersref,FriendsRef;
    private FirebaseAuth mauth;
    private String senderuserid,receiveruserid;
    private String current_state;
    private  String saveCurrentDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        mauth=FirebaseAuth.getInstance();
        senderuserid=mauth.getCurrentUser().getUid();
        receiveruserid=getIntent().getExtras().get("visit_user_id").toString();
        Usersref= FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendRequestRef=FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        InitializeFields();

        Usersref.child(receiveruserid).addValueEventListener(new ValueEventListener() {
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

                    Picasso.with(PersonProfileActivity.this).load(myProfileimage).placeholder(R.drawable.profile).into(userProfImage);
                    username.setText("@" + myusername);
                    userCountry.setText("Country: " +  myCountry);
                    userDOB.setText("DOB: " + myDOB);
                    userGender.setText("Gender: " + myGender);
                    userProfname.setText(myprofilename);
                    userRelation.setText("Relationship Status: " + myRelation);
                    userStatus.setText(myProfilestatus);

                    MaintenanceofButton();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
declinebutton.setVisibility(View.INVISIBLE);
declinebutton.setEnabled(false);
if(!senderuserid.equals(receiveruserid))
{
sendbutton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        sendbutton.setEnabled(false);
        if(current_state.equals("not_friends"))
        {
            SendFriendRequestToPerson();
        }
        if(current_state.equals("request_sent"))
        {
            CancelFriendRequest();
        }
        if(current_state.equals("request_received"))
        {
            AcceptFriendRequest();
        }
        if(current_state.equals("friends"))
        {
            UnfriendAnExistingFriend();
        }
    }
});
}
else
{
    declinebutton.setVisibility(View.INVISIBLE);
    sendbutton.setVisibility(View.INVISIBLE);
}
    }

    private void UnfriendAnExistingFriend() {
        FriendsRef.child(senderuserid).child(receiveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    FriendsRef.child(receiveruserid).child(senderuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendbutton.setEnabled(true);
                                current_state="not_friends";
                                sendbutton.setText("Send Friend Request");
                                declinebutton.setVisibility(View.INVISIBLE);
                                declinebutton.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });

    }

    private void AcceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
     saveCurrentDate = currentDate.format(calFordDate.getTime());

     FriendsRef.child(senderuserid).child(receiveruserid).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
         @Override
         public void onComplete(@NonNull Task<Void> task) {
             if(task.isSuccessful())
             {
                 FriendsRef.child(receiveruserid).child(senderuserid).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         if(task.isSuccessful())
                         {
                             FriendRequestRef.child(senderuserid).child(receiveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if(task.isSuccessful())
                                     {
                                         FriendRequestRef.child(receiveruserid).child(senderuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {
                                                 if(task.isSuccessful())
                                                 {
                                                     sendbutton.setEnabled(true);
                                                     current_state="friends";
                                                     sendbutton.setText("Unfriend");
                                                     declinebutton.setVisibility(View.INVISIBLE);
                                                     declinebutton.setEnabled(false);

                                                 }
                                             }
                                         });
                                     }
                                 }
                             });
                         }
                     }
                 });

             }
         }
     });

           }

    private void CancelFriendRequest() {
        FriendRequestRef.child(senderuserid).child(receiveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    FriendRequestRef.child(receiveruserid).child(senderuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendbutton.setEnabled(true);
                                current_state="not_friends";
                                sendbutton.setText("Send Friend Request");
                                declinebutton.setVisibility(View.INVISIBLE);
                                declinebutton.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });

    }

    private void MaintenanceofButton() {
        FriendRequestRef.child(senderuserid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiveruserid))
                {
                    String request_type=dataSnapshot.child(receiveruserid).child("request_type").getValue().toString();
                    if(request_type.equals("sent"))
                    {
                        current_state="request_sent";
                        sendbutton.setText("Cancel Friend Request");

                        declinebutton.setVisibility(View.INVISIBLE);
                        declinebutton.setEnabled(false);
                    }
                    else if(request_type.equals("received"))
                    {
                        current_state="request_received";
                        sendbutton.setText("Accept Friend Request");
                        declinebutton.setVisibility(View.VISIBLE);
                        declinebutton.setEnabled(true);
                        declinebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest(); 
                            }
                        });
                    }
                }
                else
                {
                    FriendsRef.child(senderuserid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiveruserid))
                            {
                                current_state="friends";
                                sendbutton.setText("Unfriend");
                                declinebutton.setVisibility(View.INVISIBLE);
                                declinebutton.setEnabled(false);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendFriendRequestToPerson() {
        FriendRequestRef.child(senderuserid).child(receiveruserid).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
           if(task.isSuccessful())
           {
               FriendRequestRef.child(receiveruserid).child(senderuserid).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                  if(task.isSuccessful())
                  {
                      sendbutton.setEnabled(true);
                      current_state="request_send";
                      sendbutton.setText("Cancel Friend Request");
                      declinebutton.setVisibility(View.INVISIBLE);
                      declinebutton.setEnabled(false);

                  }
                   }
               });
           }
            }
        });
    }

    private void InitializeFields() {


        username=(TextView) findViewById(R.id.person_user_name);

        userProfname=(TextView)findViewById(R.id.person_full_name);


        userStatus=(TextView)findViewById(R.id.person_profile_status);
        userCountry=(TextView)findViewById(R.id.person_country);
        userRelation=(TextView)findViewById(R.id.person_relation);
        userDOB=(TextView)findViewById(R.id.person_dob);
        userGender=(TextView)findViewById(R.id.person_gender);
        userProfImage=(CircleImageView)findViewById(R.id.person_profile_pic);
        sendbutton=(Button)findViewById(R.id.person_send_friendrequest_button);
        declinebutton=(Button)findViewById(R.id.person_decline_friendrequest_button);
        current_state="not_friends";
    }
}
