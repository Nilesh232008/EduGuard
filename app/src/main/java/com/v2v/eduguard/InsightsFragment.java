package com.v2v.eduguard;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
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

    String teacherId;

    // 🔥 CHANGE THIS EVERY TIME NGROK RESTARTS
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

        adapter = new InsightsAdapter(studentList);
        recycler.setAdapter(adapter);

        teacherId = FirebaseAuth.getInstance().getUid();

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
                            String name = ds.child("name").getValue(String.class);

                            float attendance = getFloat(ds,"attendance");
                            float marks = getFloat(ds,"marks");
                            float behavior = getFloat(ds,"behavior");
                            float fees = getBoolean(ds,"feesPaid") ? 1 : 0;

                            Student s = new Student(id,name);

                            callAPI(s, attendance, marks, behavior, fees);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
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
                         float fees){

        new Thread(() -> {

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
                json.put("assignments",80);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                Scanner sc = new Scanner(conn.getInputStream());
                String response = sc.useDelimiter("\\A").next();
                sc.close();

                JSONObject res = new JSONObject(response);

                int riskScore = res.getInt("risk_percent");
                String riskLevel = res.getString("risk");

                student.riskScore = riskScore;

                // 🔥 SAVE BACK
                studentsRef.child(student.id).child("riskScore")
                        .setValue(riskScore);

                studentsRef.child(student.id).child("riskLevel")
                        .setValue(riskLevel);

                requireActivity().runOnUiThread(() -> {

                    studentList.add(student);

                    Collections.sort(studentList,
                            (a,b)-> b.riskScore - a.riskScore);

                    adapter.notifyDataSetChanged();
                });

            }catch(Exception e){
                e.printStackTrace();
            }

        }).start();
    }
}