package com.v2v.eduguard;

import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeworkAdapter
        extends RecyclerView.Adapter<HomeworkAdapter.ViewHolder>{

    ArrayList<Student> list;

    public HomeworkAdapter(ArrayList<Student> list){
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        CheckBox done;

        public ViewHolder(View v){
            super(v);

            name = v.findViewById(R.id.tvName);
            done = v.findViewById(R.id.checkDone);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_homework,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Student s = list.get(position);

        holder.name.setText(s.name);

        // 🔥 VERY IMPORTANT (avoid recycle bug)
        holder.done.setOnCheckedChangeListener(null);

        // 🔥 Use homeworkSubmitted instead of present
        holder.done.setChecked(s.homeworkSubmitted);

        holder.done.setOnCheckedChangeListener((btn, checked) -> {
            s.homeworkSubmitted = checked;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}