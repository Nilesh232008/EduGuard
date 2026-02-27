package com.v2v.eduguard;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AttendanceAdapter
        extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder>{

    ArrayList<Student> list;

    public AttendanceAdapter(ArrayList<Student> list){
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        CheckBox present;

        public ViewHolder(View v){
            super(v);

            name = v.findViewById(R.id.tvName);
            present = v.findViewById(R.id.checkPresent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_attendance,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Student s = list.get(position);

        holder.name.setText(s.name);

        // 🔥 FIX: remove old listener first
        holder.present.setOnCheckedChangeListener(null);

        holder.present.setChecked(s.present);

        holder.present.setOnCheckedChangeListener((btn,checked) -> {
            s.present = checked;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}