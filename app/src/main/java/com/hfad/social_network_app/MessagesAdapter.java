package com.hfad.social_network_app;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> usermessageslist;
    private FirebaseAuth mauth;
    private DatabaseReference    usersdatabaseref;


    public MessagesAdapter(List<Messages>usermessageslist)

    {
        this.usermessageslist=usermessageslist;
    }

    public class MessageViewHolder extends  RecyclerView.ViewHolder{

        public TextView sendermessagetext,receivermessagetext;
        public CircleImageView receiverprofileimage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sendermessagetext=(TextView)itemView.findViewById(R.id.sender_message_text);
            receivermessagetext=(TextView)itemView.findViewById(R.id.receiver_message_text);
            receiverprofileimage=(CircleImageView)itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout_of_user,viewGroup,false);

mauth=FirebaseAuth.getInstance();
return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messagesenderid=mauth.getCurrentUser().getUid();
        Messages messages=usermessageslist.get(i);
        String fromuserid=messages.getFrom();
        String frommessagetype=messages.getType();

        usersdatabaseref= FirebaseDatabase.getInstance().getReference().child("Users").child(fromuserid);
        usersdatabaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String image=dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.with(messageViewHolder.receiverprofileimage.getContext()).load(image).placeholder(R.drawable.profile).into(messageViewHolder.receiverprofileimage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(frommessagetype.equals("text"))
        {
            messageViewHolder.receivermessagetext.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverprofileimage.setVisibility(View.INVISIBLE);
                if(fromuserid.equals(messagesenderid))
                {
messageViewHolder.sendermessagetext.setBackgroundResource(R.drawable.seender_message_text_background);
messageViewHolder.sendermessagetext.setTextColor(Color.WHITE);
messageViewHolder.sendermessagetext.setGravity(Gravity.LEFT);
messageViewHolder.sendermessagetext.setText(messages.getMessage());
                }
                else
                {
                    messageViewHolder.sendermessagetext.setVisibility(View.INVISIBLE);
                    messageViewHolder.receivermessagetext.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverprofileimage.setVisibility(View.VISIBLE);

                        messageViewHolder.receivermessagetext.setBackgroundResource(R.drawable.receiver_message_text_background);
                        messageViewHolder.receivermessagetext.setTextColor(Color.WHITE);
                        messageViewHolder.receivermessagetext.setGravity(Gravity.LEFT);
                        messageViewHolder.receivermessagetext.setText(messages.getMessage());

                }

        }
    }

    @Override
    public int getItemCount() {
        return usermessageslist.size();
    }
}
