package com.v2v.eduguard;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.HashMap;

public class AddClassActivity extends AppCompatActivity {

    EditText etClassName, etSection, etTeacherEmail;
    Button btnCreate;

    DatabaseReference teachersRef, classesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        etClassName = findViewById(R.id.etClassName);
        etSection = findViewById(R.id.etSection);
        etTeacherEmail = findViewById(R.id.etTeacherEmail);
        btnCreate = findViewById(R.id.btnCreateClass);

        teachersRef = FirebaseDatabase.getInstance().getReference("Teachers");
        classesRef = FirebaseDatabase.getInstance().getReference("Classes");

        btnCreate.setOnClickListener(v -> createClass());
    }

    private void createClass(){

        String className = etClassName.getText().toString().trim();
        String section = etSection.getText().toString().trim();
        String email = etTeacherEmail.getText().toString().trim();

        if(className.isEmpty() || section.isEmpty() || email.isEmpty()){
            Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Find Teacher UID by Email
        teachersRef.orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!snapshot.exists()){
                            Toast.makeText(AddClassActivity.this,
                                    "Teacher not found!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get teacher UID
                        String teacherId = snapshot.getChildren()
                                .iterator()
                                .next()
                                .getKey();

                        saveClass(className, section, email, teacherId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(AddClassActivity.this,
                                "Database Error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveClass(String className,
                           String section,
                           String email,
                           String teacherId){

        String classId = classesRef.push().getKey();

        HashMap<String,Object> classMap = new HashMap<>();
        classMap.put("className",className);
        classMap.put("section",section);
        classMap.put("teacherId",teacherId);
        classMap.put("teacherEmail",email);
        classMap.put("createdAt",System.currentTimeMillis());

        classesRef.child(classId)
                .setValue(classMap)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this,
                            "Class Created Successfully!",
                            Toast.LENGTH_SHORT).show();

                    finish();
                });
    }
}
