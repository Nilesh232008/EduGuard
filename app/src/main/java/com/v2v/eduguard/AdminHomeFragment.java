package com.v2v.eduguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.*;

public class AdminHomeFragment extends Fragment {

    TextView tvStudents, tvFaculty, tvClasses, tvRisk;

    DatabaseReference studentsRef, facultyRef, classesRef, riskRef;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        tvStudents = view.findViewById(R.id.tvTotalStudents);
        tvFaculty = view.findViewById(R.id.tvTotalFaculty);
        tvClasses = view.findViewById(R.id.tvTotalClasses);
        tvRisk = view.findViewById(R.id.tvRiskAlerts);

        // Firebase references
        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        facultyRef = FirebaseDatabase.getInstance().getReference("Teachers");
        classesRef = FirebaseDatabase.getInstance().getReference("Classes");
        riskRef = FirebaseDatabase.getInstance().getReference("RiskAlerts");

        loadCounts();

        // Buttons
        view.findViewById(R.id.btnAddTeacher)
                .setOnClickListener(v ->
                        startActivity(new Intent(getActivity(), AddTeacherActivity.class)));

        view.findViewById(R.id.btnAddClass)
                .setOnClickListener(v ->
                        startActivity(new Intent(getActivity(), AddClassActivity.class)));

        view.findViewById(R.id.btnNotice)
                .setOnClickListener(v ->
                        startActivity(new Intent(getActivity(), NoticeActivity.class)));

        return view;
    }

    private void loadCounts(){

        studentsRef.addValueEventListener(countListener(tvStudents));
        facultyRef.addValueEventListener(countListener(tvFaculty));
        classesRef.addValueEventListener(countListener(tvClasses));
        riskRef.addValueEventListener(countListener(tvRisk));
    }

    private ValueEventListener countListener(TextView textView){
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long count = snapshot.getChildrenCount();
                textView.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };
    }
}

