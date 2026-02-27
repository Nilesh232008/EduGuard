package com.v2v.eduguard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class InsightsAdapter extends RecyclerView.Adapter<InsightsAdapter.ViewHolder>{

    ArrayList<Student> list;
    Context context;

    public InsightsAdapter(Context context, ArrayList<Student> list){
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, risk;
        ProgressBar progress;

        public ViewHolder(View v){
            super(v);

            name = v.findViewById(R.id.tvName);
            risk = v.findViewById(R.id.tvRisk);
            progress = v.findViewById(R.id.riskProgress);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_insight_student,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Student s = list.get(position);

        holder.name.setText(s.name);
        holder.risk.setText(s.riskScore + "% Risk (" + s.riskLevel + ")");
        holder.progress.setProgress(s.riskScore);

        if(s.riskScore >= 70){
            holder.risk.setTextColor(Color.RED);
        }
        else if(s.riskScore >= 40){
            holder.risk.setTextColor(Color.parseColor("#FFA500"));
        }
        else{
            holder.risk.setTextColor(Color.parseColor("#16A34A"));
        }

        // 🔥 OPEN DETAIL PAGE
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, StudentDetailActivity.class);
            intent.putExtra("studentId", s.id);
            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}