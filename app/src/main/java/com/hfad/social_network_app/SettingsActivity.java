package com.hfad.social_network_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private CircleImageView userProfImage;
    private EditText username,userProfname,userStatus,userCountry,userGender,userRelation,userDOB;
    private Button UpdateSettingsAccountButton;
private Toolbar mtoolbar;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    String currentuserid;
    private FirebaseAuth mauth;
    private DatabaseReference Settingsusersref;
    final static int Gallery_Pick=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        Settingsusersref= FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserid);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        username=(EditText)findViewById(R.id.settings_username);
        userProfname=(EditText)findViewById(R.id.settings_profile_full_name);
        loadingBar = new ProgressDialog(this);

        userStatus=(EditText)findViewById(R.id.settings_status);
        userCountry=(EditText)findViewById(R.id.settings_country);
        userRelation=(EditText)findViewById(R.id.settings_relationhip_status);
        userDOB=(EditText)findViewById(R.id.settings_dob);
        userGender=(EditText)findViewById(R.id.settings_gender);
        userProfImage=(CircleImageView)findViewById(R.id.settings_profile_image);
        UpdateSettingsAccountButton=(Button)findViewById(R.id.update_account_settings_button);

Settingsusersref.addValueEventListener(new ValueEventListener() {
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

            Picasso.with(SettingsActivity.this).load(myProfileimage).placeholder(R.drawable.profile).into(userProfImage);
            username.setText(myusername);
            userCountry.setText(myCountry);
            userDOB.setText(myDOB);
            userGender.setText(myGender);
            userProfname.setText(myprofilename);
            userRelation.setText(myRelation);
            userStatus.setText(myProfilestatus);


        }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});
UpdateSettingsAccountButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        ValidateAccountInfo();
    }
});
userProfImage.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }
});


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentuserid   + ".jpg");

                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downUri = task.getResult();
                            Toast.makeText(SettingsActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = downUri.toString();
                            Settingsusersref.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SettingsActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void ValidateAccountInfo() {
        String username1=username.getText().toString();
        String profilename1=userProfname.getText().toString();
        String status1=userStatus.getText().toString();
        String dob1=userDOB.getText().toString();
        String country1=userCountry.getText().toString();
        String gender1=userGender.getText().toString();
        String relation1=userRelation.getText().toString();
        if(TextUtils.isEmpty(username1))
            Toast.makeText(this,"Please write your username",Toast.LENGTH_SHORT).show();
        else  if(TextUtils.isEmpty(profilename1))
            Toast.makeText(this,"Please write your Profile name",Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(dob1))
            Toast.makeText(this,"Please write your date of birth",Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(gender1))
            Toast.makeText(this,"Please write your gender",Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(status1))
            Toast.makeText(this,"Please write your status",Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(country1))
            Toast.makeText(this,"Please write your country name",Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(relation1))
            Toast.makeText(this,"Please write your relationship status",Toast.LENGTH_SHORT).show();
            else {

            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we updating your profile image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccoountInformation(username1, profilename1, status1, dob1, country1, gender1, relation1);
        }

    }

    private void UpdateAccoountInformation(String username1,String profilename1,String status1,String dob1,String country1,String gender1,String relation1) {
        HashMap userMap=new HashMap();
        userMap.put("username",username1);
        userMap.put("fullname",profilename1);
        userMap.put("dob",dob1);
        userMap.put("status",status1);
        userMap.put("gender",gender1);
        userMap.put("relationshipstatus",relation1);
        userMap.put("country",country1);
        Settingsusersref.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this,"Account settings updated successfully",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else
                {
                    Toast.makeText(SettingsActivity.this,"Error occured while updating account",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
