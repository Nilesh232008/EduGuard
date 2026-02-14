package com.v2v.eduguard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;

    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    private final String ADMIN_EMAIL = "admin12@gmail.com";
    private final String ADMIN_PASS = "12345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        checkAutoLogin();

        loginBtn.setOnClickListener(v -> loginUser());
    }

    // ✅ AUTO LOGIN CHECK
    private void checkAutoLogin(){

        SharedPreferences sp = getSharedPreferences("admin", MODE_PRIVATE);

        // Admin persistence
        if(sp.getBoolean("isAdmin", false)){
            startActivity(new Intent(this, AdminActivity.class));
            finish();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            redirectUser(user.getUid());
        }
    }

    // ✅ LOGIN FUNCTION
    private void loginUser(){

        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();

        if(userEmail.isEmpty() || userPass.isEmpty()){
            Toast.makeText(this, "Enter Email & Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 ADMIN LOGIN (HARDCODE)
        if(userEmail.equals(ADMIN_EMAIL) && userPass.equals(ADMIN_PASS)){

            SharedPreferences sp = getSharedPreferences("admin", MODE_PRIVATE);
            sp.edit().putBoolean("isAdmin", true).apply();

            startActivity(new Intent(this, AdminActivity.class));
            finish();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Logging in...");
        dialog.show();

        // 🔥 FIREBASE LOGIN
        mAuth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        String uid = mAuth.getCurrentUser().getUid();
                        redirectUser(uid);
                        dialog.dismiss();

                    }else{
                        Toast.makeText(this,
                                "Login Failed!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✅ ROLE REDIRECTION
    private void redirectUser(String uid){

        usersRef.child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!snapshot.exists()){
                            Toast.makeText(LoginActivity.this,
                                    "User role not assigned!",
                                    Toast.LENGTH_SHORT).show();

                            FirebaseAuth.getInstance().signOut();
                            return;
                        }

                        String role = snapshot.getValue(String.class);

                        if(role == null){
                            Toast.makeText(LoginActivity.this,
                                    "Invalid role!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        switch (role){

                            case "admin":
                                startActivity(new Intent(LoginActivity.this,
                                        AdminActivity.class));
                                break;

                            case "educator":
                                startActivity(new Intent(LoginActivity.this,
                                        EducatorActivity.class));
                                break;

                            case "parent":
                                startActivity(new Intent(LoginActivity.this,
                                        ParentActivity.class));
                                break;

                            default:
                                Toast.makeText(LoginActivity.this,
                                        "Unknown role!",
                                        Toast.LENGTH_SHORT).show();
                                return;
                        }

                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(LoginActivity.this,
                                "Database Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


