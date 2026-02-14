package com.v2v.eduguard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;

import java.util.ArrayList;
import java.util.Random;

public class ParentAttendanceFragment extends Fragment {

    private PieChart pieChart;
    private TextView tvAttendancePercent, tvSummary;

    public ParentAttendanceFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_parent_attendance,
                container,
                false);

        // ✅ ALWAYS bind views from inflated view
        pieChart = view.findViewById(R.id.pieChart);
        tvAttendancePercent = view.findViewById(R.id.tvAttendancePercent);
        tvSummary = view.findViewById(R.id.tvSummary);

        loadPieChart();

        return view;
    }

    private void loadPieChart(){

        // ⭐ For demo (Replace later with Firebase)
        Random rand = new Random();

        int presentDays = rand.nextInt(20) + 5;
        int absentDays = 30 - presentDays;

        int percent = (presentDays * 100) / 30;

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(presentDays, "Present"));
        entries.add(new PieEntry(absentDays, "Absent"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(
                Color.parseColor("#16A34A"), // Green
                Color.parseColor("#DC2626")  // Red
        );

        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);

        // ✅ Chart Styling (Important for Judges)
        pieChart.setData(data);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false); // cleaner UI
        pieChart.setCenterText(percent + "%");
        pieChart.setCenterTextSize(22f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(60f);

        pieChart.animateY(1000);

        // ✅ SAFE — no getView()
        tvAttendancePercent.setText(percent + "% Attendance");
        tvSummary.setText(
                presentDays + " Days Present • " +
                        absentDays + " Days Absent"
        );

        pieChart.invalidate(); // refresh chart
    }
}
