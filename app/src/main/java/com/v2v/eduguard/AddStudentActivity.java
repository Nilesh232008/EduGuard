package com.v2v.eduguard;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;

public class AddStudentActivity extends AppCompatActivity {

    EditText etName, etParentEmail;
    Spinner spinnerClass;
    Button btnAdd;

    FirebaseAuth mAuth;
    DatabaseReference usersRef, studentsRef, classesRef;

    String teacherId;

    ArrayList<String> classNames = new ArrayList<>();
    ArrayList<String> classIds = new ArrayList<>();

    String selectedClassId = "";
    String selectedClassName = "";

    private final String DEFAULT_PARENT_PASS = "Parent@123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        etName = findViewById(R.id.etStudentName);
        etParentEmail = findViewById(R.id.etParentEmail);
        spinnerClass = findViewById(R.id.spinnerClass);
        btnAdd = findViewById(R.id.btnAddStudent);

        mAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        classesRef = FirebaseDatabase.getInstance().getReference("Classes");

        FirebaseUser teacher = mAuth.getCurrentUser();

        if(teacher != null){
            teacherId = teacher.getUid();
        }

        loadTeacherClasses();

        btnAdd.setOnClickListener(v -> createParentAndStudent());
    }

    // 🔥 LOAD CLASSES INTO SPINNER
    private void loadTeacherClasses(){

        classesRef.orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        classNames.clear();
                        classIds.clear();

                        if(!snapshot.exists()){
                            Toast.makeText(AddStudentActivity.this,
                                    "No classes assigned!",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        for(DataSnapshot ds : snapshot.getChildren()){

                            String id = ds.getKey();

                            String name = ds.child("className")
                                    .getValue(String.class);

                            String section = ds.child("section")
                                    .getValue(String.class);

                            String displayName = name + " - " + section;

                            classNames.add(displayName);
                            classIds.add(id);
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(AddStudentActivity.this,
                                        android.R.layout.simple_spinner_dropdown_item,
                                        classNames);

                        spinnerClass.setAdapter(adapter);

                        spinnerClass.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {

                                    @Override
                                    public void onItemSelected(AdapterView<?> parent,
                                                               android.view.View view,
                                                               int position,
                                                               long id) {

                                        selectedClassId = classIds.get(position);
                                        selectedClassName = classNames.get(position);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) { }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(AddStudentActivity.this,
                                "Failed to load classes",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔥 CREATE PARENT
    private void createParentAndStudent(){

        String studentName = etName.getText().toString().trim();
        String parentEmail = etParentEmail.getText().toString().trim();

        if(studentName.isEmpty() || parentEmail.isEmpty()){
            Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(parentEmail, DEFAULT_PARENT_PASS)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        FirebaseUser parentUser = mAuth.getCurrentUser();

                        if(parentUser == null){
                            Toast.makeText(this,"Error creating parent",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String parentId = parentUser.getUid();

                        saveParentRole(parentId, parentEmail, studentName);

                    }else{

                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // 🔥 SAVE PARENT ROLE
    private void saveParentRole(String parentId,
                                String email,
                                String studentName){

        HashMap<String,Object> userMap = new HashMap<>();
        userMap.put("role","parent");
        userMap.put("email",email);

        usersRef.child(parentId)
                .setValue(userMap)
                .addOnSuccessListener(unused ->
                        createStudent(parentId, email, studentName));
    }

    // 🔥 CREATE STUDENT
    private void createStudent(String parentId,
                               String email,
                               String studentName){

        String studentId = studentsRef.push().getKey();

        HashMap<String,Object> studentMap = new HashMap<>();
        studentMap.put("name",studentName);
        studentMap.put("classId",selectedClassId);
        studentMap.put("className",selectedClassName);
        studentMap.put("teacherId",teacherId);
        studentMap.put("parentId",parentId);
        studentMap.put("parentEmail",email);
        studentMap.put("riskLevel","LOW");
        studentMap.put("createdAt",System.currentTimeMillis());

        studentsRef.child(studentId)
                .setValue(studentMap)
                .addOnSuccessListener(unused -> {

                    FirebaseAuth.getInstance().signOut(); // ⭐ prevents parent login

                    Toast.makeText(this,
                            "Student Added!\nParent Password: "+DEFAULT_PARENT_PASS,
                            Toast.LENGTH_LONG).show();

                    finish();
                });
    }
}
