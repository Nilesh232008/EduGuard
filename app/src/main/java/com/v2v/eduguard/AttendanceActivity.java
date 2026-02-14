package com.v2v.eduguard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceActivity extends AppCompatActivity {

    RecyclerView recycler;
    EditText search;
    Button btnMarkAll, btnSave;

    AttendanceAdapter adapter;

    ArrayList<Student> studentList = new ArrayList<>();
    ArrayList<Student> filteredList = new ArrayList<>();

    DatabaseReference studentsRef, attendanceRef;

    String teacherId;
    String classId = ""; // assume single class teacher

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_attendance);

        recycler = findViewById(R.id.attendanceRecycler);
        search = findViewById(R.id.searchStudent);
        btnMarkAll = findViewById(R.id.btnMarkAll);
        btnSave = findViewById(R.id.btnSaveAttendance);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AttendanceAdapter(filteredList);
        recycler.setAdapter(adapter);

        teacherId = FirebaseAuth.getInstance().getUid();

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance");

        loadStudents();

        btnMarkAll.setOnClickListener(v -> {

            for(Student s : filteredList){
                s.present = true;
            }

            adapter.notifyDataSetChanged();
        });

        btnSave.setOnClickListener(v -> {

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Confirm Attendance")
                    .setMessage("Save today's attendance?")
                    .setPositiveButton("Save",(d,w)-> saveAttendance())
                    .setNegativeButton("Cancel",null)
                    .show();
        });


        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            public void onTextChanged(CharSequence s,int a,int b,int c){
                filter(s.toString());
            }
            public void afterTextChanged(Editable s){}
        });
    }

    // 🔥 LOAD TEACHER STUDENTS
    private void loadStudents(){

        studentsRef.orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        studentList.clear();

                        for(DataSnapshot ds : snapshot.getChildren()){

                            String id = ds.getKey();
                            String name = ds.child("name")
                                    .getValue(String.class);

                            classId = ds.child("classId")
                                    .getValue(String.class);

                            studentList.add(new Student(id,name));
                        }

                        filteredList.clear();
                        filteredList.addAll(studentList);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void filter(String text){

        filteredList.clear();

        for(Student s : studentList){

            if(s.name.toLowerCase()
                    .contains(text.toLowerCase())){

                filteredList.add(s);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // 🔥 CALL THIS WHEN SAVING (Add a Save Button later)
    private void saveAttendance(){

        if(classId == null || classId.isEmpty()){
            Toast.makeText(this,"Class not found!",Toast.LENGTH_SHORT).show();
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date());

        DatabaseReference todayRef =
                attendanceRef.child(classId).child(today);

        for(Student s : studentList){

            todayRef.child(s.id).setValue(s.present);
        }

        Toast.makeText(this,
                "Attendance Saved Successfully!",
                Toast.LENGTH_SHORT).show();

        finish(); // optional but recommended
    }

}

