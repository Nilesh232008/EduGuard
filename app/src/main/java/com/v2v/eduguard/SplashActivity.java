package com.v2v.eduguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    private final String ADMIN_EMAIL = "admin12@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        new android.os.Handler().postDelayed(() -> {
            checkUser();
        }, 1500);
    }

    private void checkUser(){

        SharedPreferences sp = getSharedPreferences("admin", MODE_PRIVATE);

        // ✅ Admin check
        if(sp.getBoolean("isAdmin", false)){
            startActivity(new Intent(this, AdminActivity.class));
            finish();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if(user == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // ✅ Check Role
        usersRef.child(user.getUid()).child("role")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if(!snapshot.exists()){
                        mAuth.signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                        return;
                    }

                    String role = snapshot.getValue(String.class);

                    if(role == null){
                        mAuth.signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                        return;
                    }
                    switch(role){

                        case "educator":
                            startActivity(new Intent(this, EducatorActivity.class));
                            break;

                        case "parent":
                            startActivity(new Intent(this, ParentActivity.class));
                            break;

                        case "admin":
                            startActivity(new Intent(this, AdminActivity.class));
                            break;

                        default:
                            mAuth.signOut();
                            startActivity(new Intent(this, LoginActivity.class));
                    }

                    finish();
                });
    }
}
