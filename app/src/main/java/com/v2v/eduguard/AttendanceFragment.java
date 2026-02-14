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

    // ✅ THIS replaces onCreate()
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_attendance,
                container,
                false);

        recycler = view.findViewById(R.id.attendanceRecycler);
        search = view.findViewById(R.id.searchStudent);
        btnMarkAll = view.findViewById(R.id.btnMarkAll);
        btnSave = view.findViewById(R.id.btnSaveAttendance);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AttendanceAdapter(filteredList);
        recycler.setAdapter(adapter);

        teacherId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

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

            new android.app.AlertDialog.Builder(getContext())
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

            if(s.name.toLowerCase().contains(text.toLowerCase())){
                filteredList.add(s);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // 🔥 SAVE ATTENDANCE
    private void saveAttendance(){

        if(classId == null || classId.isEmpty()){
            Toast.makeText(getContext(),
                    "Class not found!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date());

        DatabaseReference todayRef =
                attendanceRef.child(classId).child(today);

        for(Student s : studentList){
            todayRef.child(s.id).setValue(s.present);
        }

        Toast.makeText(getContext(),
                "Attendance Saved Successfully!",
                Toast.LENGTH_SHORT).show();

        // ✅ Go back to dashboard
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }
}
