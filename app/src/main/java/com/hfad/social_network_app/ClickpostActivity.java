package com.hfad.social_network_app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickpostActivity extends AppCompatActivity {
private ImageView PostImage;
private TextView PostDescription;
private Button DeletePostbutton,EditPostButton;
private Toolbar mtoolbar;
private String PostKey,currentuserid,databaseuserid,description,image;
private DatabaseReference ClickPostRef;
private FirebaseAuth mauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clickpost);

        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        PostKey=getIntent().getExtras().get("PostKey").toString();
        ClickPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);


        PostImage=(ImageView)findViewById(R.id.click_post_image);

        PostDescription=(TextView)findViewById(R.id.click_post_description);

        DeletePostbutton=(Button)findViewById(R.id.delete_post_button);
        EditPostButton=(Button)findViewById(R.id.edit_post_button);

        DeletePostbutton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if(dataSnapshot.exists())
              {
                  description=dataSnapshot.child("description").getValue().toString();
                  image=dataSnapshot.child("postimage").getValue().toString();
                  databaseuserid=dataSnapshot.child("uid").getValue().toString();
                  PostDescription.setText(description);
                  Picasso.with(ClickpostActivity.this).load(image).into(PostImage);
                  if(currentuserid.equals(databaseuserid))
                  {
                      DeletePostbutton.setVisibility(View.VISIBLE);
                      EditPostButton.setVisibility(View.VISIBLE);

                  }
                  EditPostButton.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          EditCurrentPost(description);
                      }
                  });
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
DeletePostbutton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        DeleteCurrentPost();
    }
});
    }

    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder=new AlertDialog.Builder(ClickpostActivity.this);
        builder.setTitle("Edit Post:");
        final EditText inputfield=new EditText(ClickpostActivity.this);
        inputfield.setText(description);
        builder.setView(inputfield);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
ClickPostRef.child("description").setValue(inputfield.getText().toString());
Toast.makeText(ClickpostActivity.this,"Post updated successfully",Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); }
        });
        Dialog dialog=builder.create();

                dialog.show();
           dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeleteCurrentPost() {
    ClickPostRef.removeValue();
    SendUserToMainActivity();
        Toast.makeText(this,"The post has been deleted",Toast.LENGTH_SHORT).show();

    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickpostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

}
