package com.v2v.eduguard;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class HomeworkActivity extends AppCompatActivity {

    EditText etSubject, search;
    TextView tvDate;
    Button btnSave;

    RecyclerView recycler;
    HomeworkAdapter adapter;

    ArrayList<Student> studentList = new ArrayList<>();
    ArrayList<Student> filteredList = new ArrayList<>();

    DatabaseReference studentsRef, homeworkRef, statusRef;

    String teacherId;
    String classId = "";
    String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework);

        etSubject = findViewById(R.id.etSubject);
        tvDate = findViewById(R.id.tvDate);
        search = findViewById(R.id.searchStudent);
        btnSave = findViewById(R.id.btnSaveHomework);
        recycler = findViewById(R.id.homeworkRecycler);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HomeworkAdapter(filteredList);
        recycler.setAdapter(adapter);

        teacherId = FirebaseAuth.getInstance().getUid();

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        homeworkRef = FirebaseDatabase.getInstance().getReference("Homework");
        statusRef = FirebaseDatabase.getInstance().getReference("HomeworkStatus");

        pickDate();
        loadStudents();

        btnSave.setOnClickListener(v -> saveHomework());

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            public void onTextChanged(CharSequence s,int a,int b,int c){
                filter(s.toString());
            }
            public void afterTextChanged(Editable s){}
        });
    }

    private void pickDate(){

        tvDate.setOnClickListener(v -> {

            Calendar cal = Calendar.getInstance();

            new DatePickerDialog(this,
                    (view,year,month,day)->{

                        selectedDate = year+"-"+(month+1)+"-"+day;
                        tvDate.setText(selectedDate);

                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))
                    .show();
        });
    }

    private void loadStudents(){

        studentsRef.orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds : snapshot.getChildren()){

                            String id = ds.getKey();
                            String name = ds.child("name")
                                    .getValue(String.class);

                            classId = ds.child("classId")
                                    .getValue(String.class);

                            studentList.add(new Student(id,name));
                        }

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

    private void saveHomework(){

        String subject = etSubject.getText().toString().trim();

        if(subject.isEmpty() || selectedDate.isEmpty()){
            Toast.makeText(this,"Enter subject & date",Toast.LENGTH_SHORT).show();
            return;
        }

        String homeworkId = homeworkRef.child(classId)
                .push().getKey();

        HashMap<String,Object> hw = new HashMap<>();
        hw.put("subject",subject);
        hw.put("date",selectedDate);
        hw.put("createdBy",teacherId);

        homeworkRef.child(classId)
                .child(homeworkId)
                .setValue(hw);

        for(Student s : studentList){

            statusRef.child(homeworkId)
                    .child(s.id)
                    .setValue(s.present);
        }

        Toast.makeText(this,
                "Homework Saved!",
                Toast.LENGTH_SHORT).show();

        finish();
    }
}

