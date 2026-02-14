package com.v2v.eduguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class FacultyFragment extends Fragment {

    RecyclerView recyclerView;
    FacultyAdapter adapter;
    ArrayList<Faculty> facultyList;
    FloatingActionButton fab;
    DatabaseReference facultyRef;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_faculty,
                container,
                false);

        recyclerView = view.findViewById(R.id.facultyRecycler);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext()));

        facultyList = new ArrayList<>();
        adapter = new FacultyAdapter(facultyList, faculty -> {

            Intent intent = new Intent(
                    getContext(),
                    TeacherDetailActivity.class);

            intent.putExtra("name", faculty.name);
            intent.putExtra("subject", faculty.subject);
            intent.putExtra("className", faculty.className);

            startActivity(intent);
        });


        recyclerView.setAdapter(adapter);

        facultyRef = FirebaseDatabase.getInstance()
                .getReference("Teachers");

        loadFaculty();

        fab = view.findViewById(R.id.fabAddTeacher);

        fab.setOnClickListener(v -> {

            startActivity(new Intent(
                    getContext(),
                    AddTeacherActivity.class));

        });

        return view;
    }

    private void loadFaculty(){

        facultyRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                facultyList.clear();

                for(DataSnapshot ds : snapshot.getChildren()){

                    Faculty faculty =
                            ds.getValue(Faculty.class);

                    facultyList.add(faculty);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

