package com.v2v.eduguard;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.HashMap;

public class StudentDetailActivity extends AppCompatActivity {

    TextView tvName, tvClass;
    EditText etAssignments, etMarks, etBehavior;
    Switch switchFees;
    Button btnSave;

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
        btnSave = findViewById(R.id.btnSave);

        studentId = getIntent().getStringExtra("studentId");
        studentName = getIntent().getStringExtra("studentName");

        tvName.setText(studentName);

        studentRef = FirebaseDatabase.getInstance()
                .getReference("Students")
                .child(studentId);

        loadData();

        btnSave.setOnClickListener(v -> saveData());
    }

    private void loadData() {

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

                Object value = ds.child("feesPaid").getValue();

                boolean fees = false;

                if(value instanceof Boolean){
                    fees = (Boolean) value;
                }
                else if(value instanceof Long){
                    fees = ((Long) value) == 1;
                }

                switchFees.setChecked(fees);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveData() {

        int assignments = parseInt(etAssignments.getText().toString());
        float marks = parseFloat(etMarks.getText().toString());
        float behavior = parseFloat(etBehavior.getText().toString());
        boolean fees = switchFees.isChecked();

        Log.d("SAVE_DEBUG", "Assignments: " + assignments);
        Log.d("SAVE_DEBUG", "Marks: " + marks);
        Log.d("SAVE_DEBUG", "Behavior: " + behavior);
        Log.d("SAVE_DEBUG", "Fees: " + fees);

        HashMap<String, Object> map = new HashMap<>();
        map.put("assignments", assignments);
        map.put("marks", marks);
        map.put("behavior", behavior);
        map.put("feesPaid", fees);

        studentRef.updateChildren(map)
                .addOnSuccessListener(unused -> {
                    Log.d("SAVE_DEBUG", "Data Saved Successfully");
                    Toast.makeText(this, "Data Saved Successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("SAVE_DEBUG", "Save Failed: " + e.getMessage());
                    Toast.makeText(this, "Failed to Save Data", Toast.LENGTH_SHORT).show();
                });
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }

    private float parseFloat(String s) {
        try { return Float.parseFloat(s); }
        catch (Exception e) { return 0; }
    }
}