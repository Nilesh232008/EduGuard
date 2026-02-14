package com.v2v.eduguard;

import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FacultyAdapter
        extends RecyclerView.Adapter<FacultyAdapter.ViewHolder>{

    ArrayList<Faculty> list;

    public interface OnFacultyClick {
        void onClick(Faculty faculty);
    }

    OnFacultyClick listener;

    public FacultyAdapter(ArrayList<Faculty> list,
                          OnFacultyClick listener){
        this.list = list;
        this.listener = listener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, subject, className;

        public ViewHolder(View itemView){
            super(itemView);

            name = itemView.findViewById(R.id.tvFacultyName);
            subject = itemView.findViewById(R.id.tvSubject);
            className = itemView.findViewById(R.id.tvClass);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_faculty,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Faculty faculty = list.get(position);

        holder.name.setText(faculty.name);
        holder.subject.setText("Subject: " + faculty.subject);
        holder.className.setText("Class: " + faculty.className);

        holder.itemView.setOnClickListener(v -> {
            listener.onClick(faculty);
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}
