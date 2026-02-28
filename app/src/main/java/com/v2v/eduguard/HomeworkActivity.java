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

    Spinner spinnerClass;

    ArrayList<String> classList = new ArrayList<>();
    ArrayList<String> classIdList = new ArrayList<>();

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

        spinnerClass = findViewById(R.id.spinnerClass);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        teacherId = FirebaseAuth.getInstance().getUid();

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        homeworkRef = FirebaseDatabase.getInstance().getReference("Homework");

        loadTeacherClasses();
        pickDate();

        btnSave.setOnClickListener(v -> saveHomework());
    }

    // 🔥 Get teacher class
    private void loadTeacherClasses() {

        DatabaseReference classRef = FirebaseDatabase.getInstance().getReference("Classes");

        classRef.orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        classList.clear();
                        classIdList.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            String id = snap.getKey();
                            String name = snap.child("className").getValue(String.class);

                            if (id != null && name != null) {
                                classIdList.add(id);
                                classList.add(name);
                            }
                        }

                        ArrayAdapter<String> adapterSpinner =
                                new ArrayAdapter<>(HomeworkActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        classList);

                        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerClass.setAdapter(adapterSpinner);

                        if (!classIdList.isEmpty()) {
                            classId = classIdList.get(0);
                            loadStudents();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {

                classId = classIdList.get(position);
                loadStudents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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

        // 🔥 MAIN HOMEWORK DATA
        HashMap<String, Object> hwMap = new HashMap<>();
        hwMap.put("subject", subject);
        hwMap.put("assignmentNo", assignmentNo);
        hwMap.put("dueDate", selectedDate);
        hwMap.put("classId", classId);
        hwMap.put("createdBy", teacherId);
        hwMap.put("createdAt", System.currentTimeMillis());

        // ✅ SAVE MAIN HOMEWORK
        homeworkRef.child(classId).child(hwId).setValue(hwMap);

        // 🔥 SAVE STUDENT-WISE DATA + UPDATE ASSIGNMENT COUNT
        for (Student s : studentList) {

            if (s.id == null) continue;

            HashMap<String, Object> studentMap = new HashMap<>();
            studentMap.put("name", s.name);
            studentMap.put("submitted", s.homeworkSubmitted);
            studentMap.put("submittedOn", s.homeworkSubmitted ? selectedDate : "");

            // Save inside Homework -> Class -> HW -> Students
            homeworkRef.child(classId)
                    .child(hwId)
                    .child("students")
                    .child(s.id)
                    .setValue(studentMap);

            // 🔥 If submitted → increase assignment count in Students table
            if (s.homeworkSubmitted) {

                DatabaseReference studentRef = studentsRef.child(s.id);

                studentRef.child("assignments").get()
                        .addOnSuccessListener(snapshot -> {

                            int current = 0;
                            if (snapshot.exists()) {
                                Integer value = snapshot.getValue(Integer.class);
                                if (value != null) current = value;
                            }

                            studentRef.child("assignments").setValue(current + 1);
                        });
            }
        }

        Toast.makeText(this, "Assignment Assigned Successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}