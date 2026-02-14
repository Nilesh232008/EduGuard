package com.v2v.eduguard;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class StudentsFragment extends Fragment {

    RecyclerView recycler;
    EditText search;

    StudentsAdapter adapter;

    ArrayList<Student> studentList = new ArrayList<>();
    ArrayList<Student> filteredList = new ArrayList<>();

    DatabaseReference studentsRef, attendanceRef, homeworkRef;

    String teacherId;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_students,
                container,false);

        recycler = view.findViewById(R.id.studentsRecycler);
        search = view.findViewById(R.id.searchStudent);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StudentsAdapter(filteredList);
        recycler.setAdapter(adapter);

        teacherId = FirebaseAuth.getInstance().getUid();

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance");
        homeworkRef = FirebaseDatabase.getInstance().getReference("HomeworkStatus");

        loadStudents();

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            public void onTextChanged(CharSequence s,int a,int b,int c){
                filter(s.toString());
            }
            public void afterTextChanged(Editable s){}
        });

        return view;
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
                            String name = ds.child("name")
                                    .getValue(String.class);

                            Student s = new Student(id,name);

                            calculateRisk(s);

                            studentList.add(s);
                        }

                        // 🔥 SORT BY RISK
                        Collections.sort(studentList,
                                (a,b)-> b.riskScore - a.riskScore);

                        filteredList.clear();
                        filteredList.addAll(studentList);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // 🔥 SIMPLE RISK ENGINE
    private void calculateRisk(Student s){

        int score = new Random().nextInt(100); // demo risk

        s.riskScore = score;

        if(score >= 70)
            s.riskLevel = "HIGH";
        else if(score >= 40)
            s.riskLevel = "MEDIUM";
        else
            s.riskLevel = "LOW";
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
}
