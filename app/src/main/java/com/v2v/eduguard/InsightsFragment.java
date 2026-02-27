package com.v2v.eduguard;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class InsightsFragment extends Fragment {

    RecyclerView recycler;
    InsightsAdapter adapter;

    ArrayList<Student> studentList = new ArrayList<>();

    DatabaseReference studentsRef;
    ValueEventListener studentsListener;

    String teacherId;

    // 🔥 UPDATE THIS WHEN NGROK CHANGES
    String API_URL = "https://difficultly-unsignified-farrah.ngrok-free.dev/predict";

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

        adapter = new InsightsAdapter(getContext(), studentList);
        recycler.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            teacherId = user.getUid();
        }else{
            Toast.makeText(getContext(),"User not logged in",Toast.LENGTH_SHORT).show();
            return view;
        }

        studentsRef = FirebaseDatabase.getInstance().getReference("Students");

        loadInsights();

        return view;
    }

    private void loadInsights(){

        studentsListener = studentsRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                studentList.clear(); // 🔥 clear old list properly

                for(DataSnapshot ds : snapshot.getChildren()){

                    String teacherIdDb = ds.child("teacherId").getValue(String.class);

                    if(teacherIdDb == null || !teacherIdDb.equals(teacherId)){
                        continue;
                    }

                    String id = ds.getKey();
                    String name = ds.child("name").getValue(String.class);

                    float attendance = getFloat(ds,"attendance");
                    float marks = getFloat(ds,"marks");
                    float behavior = getFloat(ds,"behavior");
                    float fees = getBoolean(ds,"feesPaid") ? 1 : 0;

                    float assignmentScore = 100;

                    Student s = new Student(id, name);

                    callAPI(s, attendance, marks, behavior, fees, assignmentScore);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.e("FIREBASE_ERROR", error.getMessage());
            }
        });
    }

    private float getFloat(DataSnapshot ds, String key){
        Float val = ds.child(key).getValue(Float.class);
        return val != null ? val : 0;
    }

    private boolean getBoolean(DataSnapshot ds, String key){
        Boolean val = ds.child(key).getValue(Boolean.class);
        return val != null && val;
    }

    private void callAPI(Student student,
                         float attendance,
                         float marks,
                         float behavior,
                         float fees,
                         float assignments){

        new Thread(() -> {

            int riskScore = 50;
            String riskLevel = "MEDIUM";

            try{

                URL url = new URL(API_URL);
                HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("attendance",attendance);
                json.put("marks",marks);
                json.put("behavior",behavior);
                json.put("fees",fees);
                json.put("assignments",assignments);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("API_DEBUG","Response Code: " + responseCode);

                if(responseCode == 200){

                    Scanner sc = new Scanner(conn.getInputStream());
                    String response = sc.useDelimiter("\\A").next();
                    sc.close();

                    JSONObject res = new JSONObject(response);

                    riskScore = res.getInt("risk_percent");
                    riskLevel = res.getString("risk");
                }

            }catch(Exception e){
                Log.e("API_ERROR", e.getMessage());
            }

            int finalRiskScore = riskScore;
            String finalRiskLevel = riskLevel;

            requireActivity().runOnUiThread(() -> {

                // 🔥 UPDATE FIREBASE
                studentsRef.child(student.id).child("riskScore")
                        .setValue(finalRiskScore);

                studentsRef.child(student.id).child("riskLevel")
                        .setValue(finalRiskLevel);

                // 🔥 UPDATE UI
                student.riskScore = finalRiskScore;
                student.riskLevel = finalRiskLevel;

                studentList.add(student);

                Collections.sort(studentList,
                        (a,b)-> b.riskScore - a.riskScore);

                adapter.notifyDataSetChanged();
            });

        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(studentsListener != null){
            studentsRef.removeEventListener(studentsListener);
        }
    }
}