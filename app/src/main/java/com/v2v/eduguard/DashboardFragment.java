package com.v2v.eduguard;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.content.Intent;

import com.google.firebase.database.*;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    TextView tvStudents, tvPresent, tvCritical, tvHomework, tvAlert;

    DatabaseReference noticeRef;

    DatabaseReference studentsRef, attendanceRef, homeworkRef;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);

        tvStudents = view.findViewById(R.id.tvTotalStudents);
        tvPresent = view.findViewById(R.id.tvPresent);
        tvCritical = view.findViewById(R.id.tvCritical);
        tvHomework = view.findViewById(R.id.tvHomework);
        tvAlert = view.findViewById(R.id.tvAlert);

        view.findViewById(R.id.btnAddStudent)
                .setOnClickListener(v ->
                        startActivity(new Intent(getActivity(), AddStudentActivity.class)));

        view.findViewById(R.id.btnAttendance)
                .setOnClickListener(v ->
                        startActivity(new Intent(getActivity(), AttendanceActivity.class)));

        view.findViewById(R.id.btnHomework)
                .setOnClickListener(v ->
                        startActivity(new Intent(getActivity(), HomeworkActivity.class)));

        view.findViewById(R.id.btnLogout)
                .setOnClickListener(v -> showLogoutDialog());



        noticeRef = FirebaseDatabase.getInstance().getReference("Notices");

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance");
        homeworkRef = FirebaseDatabase.getInstance().getReference("Homework");

        loadStudentCount();
        loadCriticalStudents();
        loadTodayAttendance();
        loadHomeworkPending();
        loadLatestNotice();


        return view;
    }

    private void showLogoutDialog(){

        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes",(d,w)-> logout())
                .setNegativeButton("Cancel",null)
                .setCancelable(false)
                .show();
    }

    private void logout(){

        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getActivity(), LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }



    private void loadStudentCount(){

        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                tvStudents.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadLatestNotice(){

        noticeRef.orderByChild("createdAt")
                .limitToLast(1) // ⭐ fetch ONLY latest notice
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){

                            for(DataSnapshot ds : snapshot.getChildren()){

                                String title = ds.child("title")
                                        .getValue(String.class);

                                String message = ds.child("message")
                                        .getValue(String.class);

                                tvAlert.setText("📢 NEW NOTICE\n\n" + title + "\n" + message);
                            }

                        }else{

                            tvAlert.setText("No new notices");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        tvAlert.setText("Failed to load notice");
                    }
                });
    }

    private void loadCriticalStudents(){

        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int count = 0;

                for(DataSnapshot ds : snapshot.getChildren()){

                    String risk = ds.child("riskLevel")
                            .getValue(String.class);

                    if("HIGH".equals(risk)){
                        count++;
                    }
                }

                tvCritical.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void loadTodayAttendance(){

        String today = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date());

        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int presentCount = 0;

                for(DataSnapshot classSnap : snapshot.getChildren()){

                    DataSnapshot todaySnap = classSnap.child(today);

                    for(DataSnapshot student : todaySnap.getChildren()){

                        Boolean present = student.getValue(Boolean.class);

                        if(Boolean.TRUE.equals(present)){
                            presentCount++;
                        }
                    }
                }

                tvPresent.setText(String.valueOf(presentCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void loadHomeworkPending(){

        homeworkRef.orderByChild("status")
                .equalTo("pending")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        tvHomework.setText(String.valueOf(
                                snapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
}
