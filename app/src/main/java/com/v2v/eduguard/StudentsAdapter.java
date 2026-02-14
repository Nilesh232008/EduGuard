package com.v2v.eduguard;

import android.graphics.Color;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StudentsAdapter
        extends RecyclerView.Adapter<StudentsAdapter.ViewHolder>{

    ArrayList<Student> list;

    public StudentsAdapter(ArrayList<Student> list){
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, risk;

        public ViewHolder(View v){
            super(v);

            name = v.findViewById(R.id.tvName);
            risk = v.findViewById(R.id.tvRisk);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_student,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Student s = list.get(position);

        holder.name.setText(s.name);
        holder.risk.setText("Risk: " + s.riskLevel +
                " (" + s.riskScore + ")");

        // 🔥 COLOR LOGIC
        switch (s.riskLevel){

            case "HIGH":
                holder.risk.setTextColor(Color.RED);
                break;

            case "MEDIUM":
                holder.risk.setTextColor(Color.parseColor("#FFA500"));
                break;

            default:
                holder.risk.setTextColor(Color.GREEN);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
