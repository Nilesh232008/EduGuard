package com.v2v.eduguard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;

import androidx.fragment.app.Fragment;

import com.github.anastr.speedviewlib.SpeedView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.anastr.speedviewlib.SpeedView;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class ParentProfileFragment extends Fragment {

    TextView tvName, tvRiskReason, tvTips;
    PieChart attendancePie, homeworkPie;
    SpeedView speedView;

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
        speedView = view.findViewById(R.id.speedView);

        loadDemoInsights();

        return view;
    }

    private void loadDemoInsights(){

        tvName.setText("Rahul Sharma");

        Random rand = new Random();

        int attendance = rand.nextInt(100);
        int homework = rand.nextInt(100);

        setupPie(attendancePie, attendance, "Attendance");
        setupPie(homeworkPie, homework, "Homework");

        int risk = (100-attendance) + (100-homework);
        risk = risk/2;

        speedView.speedTo(risk);

        if(risk > 70){

            speedView.getIndicator().setColor(Color.RED);

            tvRiskReason.setText(
                    "Low attendance and incomplete homework detected.");

            tvTips.setText(
                    "• Ensure daily school attendance\n" +
                            "• Allocate fixed study hours\n" +
                            "• Stay connected with teachers");

        }
        else if(risk > 40){

            speedView.getIndicator().setColor(Color.YELLOW);

            tvRiskReason.setText(
                    "Academic consistency needs improvement.");

            tvTips.setText(
                    "• Encourage homework completion\n" +
                            "• Monitor progress weekly");

        }
        else{

            speedView.getIndicator().setColor(Color.GREEN);

            tvRiskReason.setText(
                    "Student is performing well.");

            tvTips.setText(
                    "• Maintain current routine\n" +
                            "• Encourage extracurricular learning");
        }
    }

    private void setupPie(PieChart chart, int percent, String label){

        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(percent, "Done"));
        entries.add(new PieEntry(100-percent, "Remaining"));

        PieDataSet set = new PieDataSet(entries, label);

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
