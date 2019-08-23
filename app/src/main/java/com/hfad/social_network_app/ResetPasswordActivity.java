package com.hfad.social_network_app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private Button resetpasswordbutton;
private EditText resetemailinput;
private Toolbar mtoolbar;
private FirebaseAuth mauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

mauth=FirebaseAuth.getInstance();
    mtoolbar=(Toolbar)findViewById(R.id.forget_password_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");
        resetpasswordbutton=(Button)findViewById(R.id.reset_password_email_button);
        resetemailinput=(EditText)findViewById(R.id.reset_password_email);



        resetpasswordbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail=resetemailinput.getText().toString();
                if(TextUtils.isEmpty(useremail))
                {
                    Toast.makeText(ResetPasswordActivity.this,"Please write your valid email address",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mauth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful())
                       {
                           Toast.makeText(ResetPasswordActivity.this,"Please check your email account",Toast.LENGTH_SHORT).show();
                           startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                       }
                       else
                       {
                           String message=task.getException().getMessage();
                           Toast.makeText(ResetPasswordActivity.this,"Error occured :" +message,Toast.LENGTH_SHORT).show();
                       }
                        }
                    });
                }
            }
        });
    }
}
