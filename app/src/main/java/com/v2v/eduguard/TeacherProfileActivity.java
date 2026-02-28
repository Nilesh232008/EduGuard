package com.v2v.eduguard;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class TeacherProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvPhone, tvClass;

    DatabaseReference teacherRef, classRef;
    String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvClass = findViewById(R.id.tvClass);

        teacherId = FirebaseAuth.getInstance().getUid();

        teacherRef = FirebaseDatabase.getInstance()
                .getReference("Teachers")
                .child(teacherId);

        loadTeacherData();
        loadTeacherClass();
    }

    private void loadTeacherData() {

        teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                tvName.setText("Name: " + snapshot.child("name").getValue(String.class));
                tvEmail.setText("Email: " + snapshot.child("email").getValue(String.class));
                tvPhone.setText("Phone: " + snapshot.child("phone").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadTeacherClass() {

        classRef = FirebaseDatabase.getInstance().getReference("Classes");

        classRef.orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String className = snap.child("className").getValue(String.class);
                            tvClass.setText("Class: " + className);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}