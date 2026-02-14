package com.v2v.eduguard;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class InsightsFragment extends Fragment {

    RecyclerView recycler;
    InsightsAdapter adapter;

    ArrayList<Student> studentList = new ArrayList<>();

    DatabaseReference studentsRef;

    String teacherId;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_insights,
                container,
                false);

        recycler = view.findViewById(R.id.insightsRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new InsightsAdapter(studentList);
        recycler.setAdapter(adapter);

        teacherId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");

        loadInsights();

        return view;
    }

    private void loadInsights(){

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

                            // DEMO AI
                            int risk = new Random().nextInt(100);
                            s.riskScore = risk;

                            studentList.add(s);
                        }

                        // 🔥 Sort Highest First
                        Collections.sort(studentList,
                                (a,b)-> b.riskScore - a.riskScore);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
