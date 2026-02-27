package com.v2v.eduguard;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.HashMap;

public class StudentDetailActivity extends AppCompatActivity {

    TextView tvName, tvClass;

    EditText etAssignments, etMarks, etBehavior;
    Switch switchFees;

    DatabaseReference studentRef;

    String studentId, studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        tvName = findViewById(R.id.tvName);
        tvClass = findViewById(R.id.tvClass);

        etAssignments = findViewById(R.id.etAssignments);
        etMarks = findViewById(R.id.etMarks);
        etBehavior = findViewById(R.id.etBehavior);
        switchFees = findViewById(R.id.switchFees);

        studentId = getIntent().getStringExtra("studentId");
        studentName = getIntent().getStringExtra("studentName");

        tvName.setText(studentName);

        studentRef = FirebaseDatabase.getInstance()
                .getReference("Students")
                .child(studentId);

        loadData();
        setupAutoSave();
    }

    private void loadData(){

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {

                tvClass.setText(ds.child("className").getValue(String.class));

                Integer assignments = ds.child("assignments").getValue(Integer.class);
                etAssignments.setText(String.valueOf(assignments != null ? assignments : 0));

                Float marks = ds.child("marks").getValue(Float.class);
                etMarks.setText(String.valueOf(marks != null ? marks : 0));

                Float behavior = ds.child("behavior").getValue(Float.class);
                etBehavior.setText(String.valueOf(behavior != null ? behavior : 1));

                Boolean fees = ds.child("feesPaid").getValue(Boolean.class);
                switchFees.setChecked(fees != null && fees);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 🔥 AUTO SAVE WHEN USER LEAVES FIELD
    private void setupAutoSave(){

        View.OnFocusChangeListener listener = (v, hasFocus) -> {
            if(!hasFocus){
                saveData();
            }
        };

        etAssignments.setOnFocusChangeListener(listener);
        etMarks.setOnFocusChangeListener(listener);
        etBehavior.setOnFocusChangeListener(listener);

        switchFees.setOnCheckedChangeListener((btn,checked) -> saveData());
    }

    private void saveData(){

        int assignments = parseInt(etAssignments.getText().toString());
        float marks = parseFloat(etMarks.getText().toString());
        float behavior = parseFloat(etBehavior.getText().toString());
        boolean fees = switchFees.isChecked();

        HashMap<String,Object> map = new HashMap<>();
        map.put("assignments", assignments);
        map.put("marks", marks);
        map.put("behavior", behavior);
        map.put("feesPaid", fees);

        studentRef.updateChildren(map);
    }

    private int parseInt(String s){
        try { return Integer.parseInt(s); }
        catch (Exception e){ return 0; }
    }

    private float parseFloat(String s){
        try { return Float.parseFloat(s); }
        catch (Exception e){ return 0; }
    }
}