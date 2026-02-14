package com.v2v.eduguard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;

import com.google.firebase.database.*;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class ParentHomeworkFragment extends Fragment {

    PieChart pieChart;
    TextView tvPercent, tvNotice;

    DatabaseReference noticeRef;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_parent_homework,
                container,
                false);

        pieChart = view.findViewById(R.id.homeworkPieChart);
        tvPercent = view.findViewById(R.id.tvHomeworkPercent);
        tvNotice = view.findViewById(R.id.tvNotice);

        noticeRef = FirebaseDatabase.getInstance()
                .getReference("Notices");

        loadPieChart();
        loadLatestNotice();

        return view;
    }

    // 🔥 PIE CHART
    private void loadPieChart(){

        // Demo data (Perfect for hackathon)
        Random rand = new Random();

        int completed = rand.nextInt(15) + 5;
        int pending = 20 - completed;

        float percent = (completed * 100f) / 20f;

        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(completed, "Completed"));
        entries.add(new PieEntry(pending, "Pending"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(
                Color.parseColor("#16A34A"), // Green
                Color.parseColor("#DC2626")  // Red
        );

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(58f);
        pieChart.setCenterText((int)percent + "%");
        pieChart.setCenterTextSize(22f);
        pieChart.animateY(1200);

        tvPercent.setText((int)percent + "% Completed");

        // ⚠️ Danger Highlight
        if(percent < 50){
            pieChart.setCenterTextColor(Color.RED);
        }
    }

    // 🔥 LOAD ADMIN NOTICE
    private void loadLatestNotice(){

        noticeRef.orderByChild("createdAt")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){

                            for(DataSnapshot ds : snapshot.getChildren()){

                                String title = ds.child("title")
                                        .getValue(String.class);

                                String message = ds.child("message")
                                        .getValue(String.class);

                                tvNotice.setText(title + "\n\n" + message);
                            }

                        }else{
                            tvNotice.setText("No new notices");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}

