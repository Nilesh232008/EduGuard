package com.v2v.eduguard;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.database.*;

import java.util.*;

public class AdminStudentsFragment extends Fragment {

    RecyclerView recycler;
    AdminStudentAdapter adapter;
    ArrayList<Student> list;

    DatabaseReference studentsRef;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_admin_students,
                container,
                false);

        recycler = view.findViewById(R.id.adminStudentsRecycler);
        recycler.setLayoutManager(
                new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new AdminStudentAdapter(list);

        recycler.setAdapter(adapter);

        studentsRef = FirebaseDatabase.getInstance()
                .getReference("Students");

        loadStudents();

        return view;
    }

    private void loadStudents(){

        studentsRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();

                for(DataSnapshot ds : snapshot.getChildren()){

                    Student s = ds.getValue(Student.class);

                    if(s != null){
                        s.id = ds.getKey();
                        list.add(s);
                    }
                }

                // 🔥 SORT BY RISK SCORE (BEST METHOD)
                Collections.sort(list,
                        (a,b) -> b.riskScore - a.riskScore);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

