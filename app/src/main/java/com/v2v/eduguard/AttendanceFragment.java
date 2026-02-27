package com.v2v.eduguard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceFragment extends Fragment {

    RecyclerView recycler;
    EditText search;
    Button btnMarkAll, btnSave;

    AttendanceAdapter adapter;

    ArrayList<Student> studentList = new ArrayList<>();
    ArrayList<Student> filteredList = new ArrayList<>();

    DatabaseReference studentsRef, attendanceRef;

    String teacherId;
    String classId = "";
    String today;

    boolean alreadyMarked = false; // 🔥 important

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_attendance, container, false);

        recycler = view.findViewById(R.id.attendanceRecycler);
        search = view.findViewById(R.id.searchStudent);
        btnMarkAll = view.findViewById(R.id.btnMarkAll);
        btnSave = view.findViewById(R.id.btnSaveAttendance);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AttendanceAdapter(filteredList);
        recycler.setAdapter(adapter);

        teacherId = FirebaseAuth.getInstance().getUid();

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance");

        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        loadStudents();

        btnMarkAll.setOnClickListener(v -> {
            if(alreadyMarked){
                Toast.makeText(getContext(),"Already marked today",Toast.LENGTH_SHORT).show();
                return;
            }

            for(Student s : filteredList){
                s.present = true;
            }
            adapter.notifyDataSetChanged();
        });

        btnSave.setOnClickListener(v -> {

            if(alreadyMarked){
                Toast.makeText(getContext(),"Attendance already saved for today",Toast.LENGTH_SHORT).show();
                return;
            }

            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Confirm")
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

        return view;
    }

    // 🔥 LOAD STUDENTS
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

                        checkTodayAttendance(); // 🔥 IMPORTANT
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // 🔥 CHECK IF ALREADY MARKED
    private void checkTodayAttendance(){

        attendanceRef.child(classId).child(today)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                            alreadyMarked = true;

                            // load previous values
                            for(Student s : studentList){

                                Boolean present = snapshot.child(s.id)
                                        .getValue(Boolean.class);

                                s.present = Boolean.TRUE.equals(present);
                            }
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
            if(s.name.toLowerCase().contains(text.toLowerCase())){
                filteredList.add(s);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // 🔥 SAVE ATTENDANCE
    private void saveAttendance(){

        DatabaseReference todayRef =
                attendanceRef.child(classId).child(today);

        for(Student s : studentList){
            todayRef.child(s.id).setValue(s.present);
        }

        updateAttendancePercentage();

        Toast.makeText(getContext(),"Saved!",Toast.LENGTH_SHORT).show();

        alreadyMarked = true;
    }

    // 🔥 CALCULATE %
    private void updateAttendancePercentage(){

        for(Student s : studentList){

            String studentId = s.id;

            attendanceRef.child(classId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {

                            int total = 0;
                            int present = 0;

                            for(DataSnapshot day : snap.getChildren()){

                                if(day.hasChild(studentId)){
                                    total++;

                                    Boolean val = day.child(studentId)
                                            .getValue(Boolean.class);

                                    if(Boolean.TRUE.equals(val)){
                                        present++;
                                    }
                                }
                            }

                            int percent = total == 0 ? 0 :
                                    (present * 100 / total);

                            studentsRef.child(studentId)
                                    .child("attendance")
                                    .setValue(percent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }
}