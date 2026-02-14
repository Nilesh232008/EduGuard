package com.v2v.eduguard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Random;

public class ParentHomeFragment extends Fragment {

    TextView tvStudentName, tvStudentClass, tvRiskBadge;
    ProgressBar progressAttendance, progressHomework;
    TextView tvAttendancePercent, tvHomeworkPercent;

    DatabaseReference studentsRef;

    String parentId;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_parent_home,
                container,
                false);

        view.findViewById(R.id.btnLogout)
                .setOnClickListener(v -> showLogoutDialog());

        tvStudentName = view.findViewById(R.id.tvStudentName);
        tvStudentClass = view.findViewById(R.id.tvStudentClass);
        tvRiskBadge = view.findViewById(R.id.tvRiskBadge);

        progressAttendance = view.findViewById(R.id.progressAttendance);
        progressHomework = view.findViewById(R.id.progressHomework);

        tvAttendancePercent = view.findViewById(R.id.tvAttendancePercent);
        tvHomeworkPercent = view.findViewById(R.id.tvHomeworkPercent);

        parentId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        studentsRef = FirebaseDatabase.getInstance()
                .getReference("Students");

        loadStudent();

        return view;
    }

    private void showLogoutDialog(){

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes",(d,w)-> logout())
                .setNegativeButton("Cancel",null)
                .show();
    }

    private void logout(){

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getActivity(),
                LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }


    private void loadStudent(){

        studentsRef.orderByChild("parentId")
                .equalTo(parentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds : snapshot.getChildren()){

                            String name = ds.child("name")
                                    .getValue(String.class);

                            String className = ds.child("className")
                                    .getValue(String.class);

                            tvStudentName.setText(name);
                            tvStudentClass.setText("Class: " + className);

                            generateInsights();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // 🔥 DEMO INSIGHTS (Hackathon Friendly)
    private void generateInsights(){

        Random rand = new Random();

        int attendance = rand.nextInt(101);
        int homework = rand.nextInt(101);

        progressAttendance.setProgress(attendance);
        progressHomework.setProgress(homework);

        tvAttendancePercent.setText(attendance + "%");
        tvHomeworkPercent.setText(homework + "%");

        int riskScore = (100 - attendance) + (100 - homework);

        if(riskScore >= 100){

            tvRiskBadge.setText("⚠️ HIGH RISK");
            tvRiskBadge.setBackgroundColor(Color.RED);
            tvRiskBadge.setTextColor(Color.WHITE);

        }
        else if(riskScore >= 60){

            tvRiskBadge.setText("MEDIUM RISK");
            tvRiskBadge.setBackgroundColor(Color.YELLOW);
            tvRiskBadge.setTextColor(Color.BLACK);

        }
        else{

            tvRiskBadge.setText("LOW RISK");
            tvRiskBadge.setBackgroundColor(Color.GREEN);
            tvRiskBadge.setTextColor(Color.WHITE);
        }
    }
}
