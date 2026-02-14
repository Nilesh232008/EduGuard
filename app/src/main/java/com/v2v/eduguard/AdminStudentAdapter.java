package com.v2v.eduguard;

import android.graphics.Color;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdminStudentAdapter
        extends RecyclerView.Adapter<AdminStudentAdapter.ViewHolder>{

    ArrayList<Student> list;

    public AdminStudentAdapter(ArrayList<Student> list){
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, risk, score;
        LinearLayout stripe;

        public ViewHolder(View itemView){
            super(itemView);

            name = itemView.findViewById(R.id.tvName);
            risk = itemView.findViewById(R.id.tvRisk);
            score = itemView.findViewById(R.id.tvScore);
            stripe = itemView.findViewById(R.id.riskStripe);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_admin_student,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Student s = list.get(position);

        holder.name.setText(s.name);
        holder.risk.setText("Risk: " + s.riskLevel);
        holder.score.setText("Risk Score: " + s.riskScore);

        // 🔥 COLOR BASED ON RISK
        switch (s.riskLevel){

            case "HIGH":
                holder.stripe.setBackgroundColor(
                        Color.parseColor("#FECACA")); // light red
                break;

            case "MEDIUM":
                holder.stripe.setBackgroundColor(
                        Color.parseColor("#FEF3C7")); // yellow
                break;

            default:
                holder.stripe.setBackgroundColor(
                        Color.parseColor("#DCFCE7")); // green
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

