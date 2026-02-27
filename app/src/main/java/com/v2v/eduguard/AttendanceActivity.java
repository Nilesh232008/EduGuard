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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity extends AppCompatActivity {

    RecyclerView recycler;
    EditText search;
    Button btnMarkAll, btnSave;

    AttendanceAdapter adapter;

    ArrayList<Student> studentList = new ArrayList<>();
    ArrayList<Student> filteredList = new ArrayList<>();

    DatabaseReference studentsRef, attendanceRef;

    String teacherId;
    String classId = "";

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

        btnSave.setOnClickListener(v -> saveAttendance());

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            public void onTextChanged(CharSequence s,int a,int b,int c){
                filter(s.toString());
            }
            public void afterTextChanged(Editable s){}
        });
    }

    private void loadStudents(){

        studentsRef.orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        studentList.clear();

                        for(DataSnapshot ds : snapshot.getChildren()){

                            String id = ds.getKey();
                            String name = ds.child("name").getValue(String.class);

                            classId = ds.child("classId").getValue(String.class);

                            studentList.add(new Student(id,name));
                        }

                        filteredList.clear();
                        filteredList.addAll(studentList);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void filter(String text){

        filteredList.clear();

        for(Student s : studentList){
            if(s.name.toLowerCase().contains(text.toLowerCase())){
                filteredList.add(s);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // 🔥 SAVE ATTENDANCE
    private void saveAttendance(){

        String today = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date());

        DatabaseReference todayRef =
                attendanceRef.child(classId).child(today);

        for(Student s : studentList){
            todayRef.child(s.id).setValue(s.present);
        }

        updateAttendancePercentage();

        Toast.makeText(this,
                "Attendance Saved!",
                Toast.LENGTH_SHORT).show();

        finish();
    }

    // 🔥 UPDATE % + CALL ML
    private void updateAttendancePercentage(){

        studentsRef.orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds : snapshot.getChildren()){

                            String studentId = ds.getKey();

                            attendanceRef.child(classId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snap) {

                                            int totalDays = 0;
                                            int presentDays = 0;

                                            for(DataSnapshot day : snap.getChildren()){

                                                if(day.hasChild(studentId)){
                                                    totalDays++;

                                                    Boolean present =
                                                            day.child(studentId)
                                                                    .getValue(Boolean.class);

                                                    if(Boolean.TRUE.equals(present)){
                                                        presentDays++;
                                                    }
                                                }
                                            }

                                            int percentage = totalDays == 0 ? 0 :
                                                    (presentDays * 100 / totalDays);

                                            // 🔥 SAVE % IN FIREBASE
                                            studentsRef.child(studentId)
                                                    .child("attendance")
                                                    .setValue(percentage);

                                            // 🔥 CALL ML API
                                            callML(studentId, percentage);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // 🔥 ML API FUNCTION (PUT HERE ONLY)
    private void callML(String studentId, float attendance){

        ApiService api = RetrofitClient
                .getClient()
                .create(ApiService.class);

        // default values (you can improve later)
        StudentData data = new StudentData(
                attendance,
                50f,   // marks
                1f,    // behavior
                1f,    // fees
                0f     // assignments
        );

        api.getRisk(data).enqueue(new Callback<RiskResponse>() {
            @Override
            public void onResponse(Call<RiskResponse> call,
                                   Response<RiskResponse> response) {

                if(response.isSuccessful() && response.body()!=null){

                    int risk = response.body().getRisk();
                    String level = response.body().getLevel();

                    studentsRef.child(studentId).child("riskScore").setValue(risk);
                    studentsRef.child(studentId).child("riskLevel").setValue(level);
                }
            }

            @Override
            public void onFailure(Call<RiskResponse> call, Throwable t) {
                Toast.makeText(AttendanceActivity.this,
                        "ML Error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}