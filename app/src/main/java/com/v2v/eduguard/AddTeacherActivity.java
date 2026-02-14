package com.v2v.eduguard;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.HashMap;

public class AddTeacherActivity extends AppCompatActivity {

    EditText etName, etEmail, etPhone;
    Button btnCreate;

    FirebaseAuth mAuth;
    DatabaseReference usersRef, teachersRef;

    private final String DEFAULT_PASS = "Teach@123"; // ⭐ default password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        btnCreate = findViewById(R.id.btnCreateTeacher);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        teachersRef = FirebaseDatabase.getInstance().getReference("Teachers");

        btnCreate.setOnClickListener(v -> createTeacher());
    }

    private void createTeacher(){

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if(name.isEmpty() || email.isEmpty() || phone.isEmpty()){
            Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Create Firebase Auth User
        mAuth.createUserWithEmailAndPassword(email, DEFAULT_PASS)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user == null){
                            Toast.makeText(this,"Error creating user",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String uid = user.getUid();

                        saveTeacherToDatabase(uid, name, email, phone);

                    }else{
                        Toast.makeText(this,
                                "Error: "+task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveTeacherToDatabase(String uid,
                                       String name,
                                       String email,
                                       String phone){

        // Teacher Data
        HashMap<String,Object> teacherMap = new HashMap<>();
        teacherMap.put("name",name);
        teacherMap.put("email",email);
        teacherMap.put("phone",phone);
        teacherMap.put("createdAt",System.currentTimeMillis());

        teachersRef.child(uid).setValue(teacherMap);

        // Role Assignment
        HashMap<String,Object> userMap = new HashMap<>();
        userMap.put("role","educator");
        userMap.put("email",email);

        usersRef.child(uid).setValue(userMap)
                .addOnSuccessListener(unused -> {

                    FirebaseAuth.getInstance().signOut();

                    Toast.makeText(this,
                            "Teacher Created!\nDefault Password: "+DEFAULT_PASS,
                            Toast.LENGTH_LONG).show();

                    finish();
                });
    }
}
