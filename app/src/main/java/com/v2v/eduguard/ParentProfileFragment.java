package com.v2v.eduguard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.XAxis;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class ParentProfileFragment extends Fragment {

    TextView tvName, tvRiskReason, tvTips;
    PieChart attendancePie, homeworkPie;
    LineChart riskLineChart;

    DatabaseReference studentRef;
    String studentId;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_parent_profile,
                container,
                false);

        tvName = view.findViewById(R.id.tvStudentName);
        tvRiskReason = view.findViewById(R.id.tvRiskReason);
        tvTips = view.findViewById(R.id.tvTips);

        attendancePie = view.findViewById(R.id.attendancePie);
        homeworkPie = view.findViewById(R.id.homeworkPie);
        riskLineChart = view.findViewById(R.id.riskLineChart);

        studentId = FirebaseAuth.getInstance().getUid();

        studentRef = FirebaseDatabase.getInstance()
                .getReference("Students")
                .child(studentId);

        loadStudentData();

        return view;
    }

    private void loadStudentData(){

        studentRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!snapshot.exists()) return;

                        String name = snapshot.child("name")
                                .getValue(String.class);

                        Integer attendance =
                                snapshot.child("attendance")
                                        .getValue(Integer.class);

                        Integer assignments =
                                snapshot.child("assignments")
                                        .getValue(Integer.class);

                        if(name != null)
                            tvName.setText(name);

                        int attendancePercent =
                                attendance != null ? attendance : 0;

                        int homeworkPercent =
                                assignments != null ? assignments : 0;

                        setupPie(attendancePie,
                                attendancePercent,
                                "Attendance");

                        setupPie(homeworkPie,
                                homeworkPercent,
                                "Homework");

                        int risk = (100 - attendancePercent)
                                + (100 - homeworkPercent);

                        risk = risk / 2;

                        setupRiskLineChart(risk);

                        setRiskInsights(risk);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupRiskLineChart(int currentRisk){

        ArrayList<Entry> entries = new ArrayList<>();

        entries.add(new Entry(0, new Random().nextInt(101)));
        entries.add(new Entry(1, new Random().nextInt(101)));
        entries.add(new Entry(2, new Random().nextInt(101)));
        entries.add(new Entry(3, currentRisk));

        LineDataSet dataSet =
                new LineDataSet(entries, "Risk %");

        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(3f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FFCDD2"));

        LineData lineData = new LineData(dataSet);

        riskLineChart.setData(lineData);
        riskLineChart.getDescription().setEnabled(false);
        riskLineChart.getAxisRight().setEnabled(false);

        XAxis xAxis = riskLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        riskLineChart.animateX(1000);
        riskLineChart.invalidate();
    }
//    private void setupRiskLineChart(int currentRisk){
//
//        ArrayList<Entry> entries = new ArrayList<>();
//
//        entries.add(new Entry(0, currentRisk - 20));
//        entries.add(new Entry(1, currentRisk - 10));
//        entries.add(new Entry(2, currentRisk));
//
//        LineDataSet dataSet =
//                new LineDataSet(entries, "Risk %");
//
//        dataSet.setColor(Color.RED);
//        dataSet.setCircleColor(Color.RED);
//        dataSet.setLineWidth(3f);
//        dataSet.setDrawFilled(true);
//        dataSet.setFillColor(Color.parseColor("#FFCDD2"));
//
//        LineData lineData = new LineData(dataSet);
//
//        riskLineChart.setData(lineData);
//        riskLineChart.getDescription().setEnabled(false);
//        riskLineChart.getAxisRight().setEnabled(false);
//
//        XAxis xAxis = riskLineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//
//        riskLineChart.animateX(1000);
//        riskLineChart.invalidate();
//    }

    private void setRiskInsights(int risk){

        if(risk > 70){

            tvRiskReason.setText(
                    "Low attendance and incomplete homework detected.");

            tvTips.setText(
                    "• Ensure daily school attendance\n" +
                            "• Allocate fixed study hours\n" +
                            "• Stay connected with teachers");
        }
        else if(risk > 40){

            tvRiskReason.setText(
                    "Academic consistency needs improvement.");

            tvTips.setText(
                    "• Encourage homework completion\n" +
                            "• Monitor progress weekly");
        }
        else{

            tvRiskReason.setText(
                    "Student is performing well.");

            tvTips.setText(
                    "• Maintain current routine\n" +
                            "• Encourage extracurricular learning");
        }
    }

    private void setupPie(PieChart chart,
                          int percent,
                          String label){

        ArrayList<PieEntry> entries =
                new ArrayList<>();

        entries.add(new PieEntry(percent, "Done"));
        entries.add(new PieEntry(100 - percent, "Remaining"));

        PieDataSet set =
                new PieDataSet(entries, label);

        set.setColors(
                Color.parseColor("#16A34A"),
                Color.parseColor("#DC2626")
        );

        set.setValueTextColor(Color.WHITE);

        PieData data = new PieData(set);

        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.setCenterText(percent + "%");
        chart.setCenterTextSize(18f);
        chart.animateY(1000);
    }
}