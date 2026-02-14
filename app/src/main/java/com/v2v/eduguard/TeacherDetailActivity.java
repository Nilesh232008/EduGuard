package com.v2v.eduguard;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class TeacherDetailActivity extends AppCompatActivity {

    TextView tvName, tvClass, tvStudentCount,
            tvHighRisk, tvAttendanceAvg;

    DatabaseReference studentsRef;

    String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_detail);

        tvName = findViewById(R.id.tvTeacherName);
        tvClass = findViewById(R.id.tvClass);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        tvHighRisk = findViewById(R.id.tvHighRisk);
        tvAttendanceAvg = findViewById(R.id.tvAttendanceAvg);

        String name = getIntent().getStringExtra("name");
        className = getIntent().getStringExtra("className");

        tvName.setText(name);
        tvClass.setText("Class: " + className);

        studentsRef = FirebaseDatabase.getInstance()
                .getReference("Students");

        loadStats();
    }

    private void loadStats(){

        studentsRef.orderByChild("className")
                .equalTo(className)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int total = 0;
                        int highRisk = 0;

                        for(DataSnapshot ds : snapshot.getChildren()){

                            total++;

                            String risk = ds.child("riskLevel")
                                    .getValue(String.class);

                            if("HIGH".equals(risk)){
                                highRisk++;
                            }
                        }

                        tvStudentCount.setText("Students: " + total);
                        tvHighRisk.setText("High Risk: " + highRisk);

                        int avgAttendance =
                                total == 0 ? 0 :
                                        (int)((total-highRisk)*100.0/total);

                        tvAttendanceAvg.setText(
                                "Attendance Avg: " + avgAttendance + "%");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}

