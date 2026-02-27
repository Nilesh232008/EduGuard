package com.v2v.eduguard;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class HomeworkActivity extends AppCompatActivity {

    EditText etSubject, etAssignmentNo, searchStudent;
    TextView tvDate;
    Button btnSave;

    RecyclerView recycler;
    HomeworkAdapter adapter;

    ArrayList<Student> studentList = new ArrayList<>();

    DatabaseReference studentsRef, homeworkRef;

    String selectedDate = "";
    String teacherId, classId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework);

        etSubject = findViewById(R.id.etSubject);
        etAssignmentNo = findViewById(R.id.etAssignmentNo);
        tvDate = findViewById(R.id.tvDate);
        btnSave = findViewById(R.id.btnSaveHomework);
        recycler = findViewById(R.id.homeworkRecycler);
        searchStudent = findViewById(R.id.searchStudent);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        teacherId = FirebaseAuth.getInstance().getUid();

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        homeworkRef = FirebaseDatabase.getInstance().getReference("Homework");

        loadTeacherClass();
        pickDate();

        btnSave.setOnClickListener(v -> saveHomework());
    }

    // 🔥 Get teacher class
    private void loadTeacherClass() {

        DatabaseReference classRef = FirebaseDatabase.getInstance().getReference("Classes");

        classRef.orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            classId = snap.getKey();
                        }

                        loadStudents();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // 🔥 Load students of class
    private void loadStudents() {

        studentsRef.orderByChild("classId")
                .equalTo(classId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        studentList.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Student s = snap.getValue(Student.class);
                            if (s != null) studentList.add(s);
                        }

                        adapter = new HomeworkAdapter(studentList);
                        recycler.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // 🔥 Date Picker
    private void pickDate() {
        tvDate.setOnClickListener(v -> {

            Calendar c = Calendar.getInstance();

            new DatePickerDialog(this,
                    (view, y, m, d) -> {
                        selectedDate = d + "/" + (m + 1) + "/" + y;
                        tvDate.setText(selectedDate);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    // 🔥 SAVE HOMEWORK (ASSIGNMENT)
    private void saveHomework() {

        String subject = etSubject.getText().toString().trim();
        String assignmentNo = etAssignmentNo.getText().toString().trim();

        if (subject.isEmpty() || assignmentNo.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (classId == null || classId.isEmpty()) {
            Toast.makeText(this, "Class not loaded yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentList == null || studentList.size() == 0) {
            Toast.makeText(this, "Students not loaded!", Toast.LENGTH_SHORT).show();
            return;
        }

        String hwId = homeworkRef.child(classId).push().getKey();

        if (hwId == null) {
            Toast.makeText(this, "Error generating ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> hwMap = new HashMap<>();
        hwMap.put("subject", subject);
        hwMap.put("assignmentNo", assignmentNo);
        hwMap.put("dueDate", selectedDate);
        hwMap.put("createdBy", teacherId);
        hwMap.put("createdAt", System.currentTimeMillis());

        homeworkRef.child(classId).child(hwId).setValue(hwMap);

        for (Student s : studentList) {

            if (s.id == null) continue; // safety

            HashMap<String, Object> studentMap = new HashMap<>();
            studentMap.put("name", s.name);
            studentMap.put("submitted", s.present);
            studentMap.put("submittedOn", s.present ? selectedDate : "");

            homeworkRef.child(classId)
                    .child(hwId)
                    .child("students")
                    .child(s.id)
                    .setValue(studentMap);
        }

        Toast.makeText(this, "Assignment Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}