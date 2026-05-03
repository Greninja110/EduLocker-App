package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemStudentBinding;
import com.example.edulocker.models.Student;

import java.util.List;

public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.ViewHolder> {

    public interface OnStudentClickListener {
        void onClick(Student student);
    }

    private final List<Student> students;
    private final OnStudentClickListener listener;

    public StudentListAdapter(List<Student> students, OnStudentClickListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentBinding binding = ItemStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(students.get(position));
    }

    @Override
    public int getItemCount() { return students.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        ViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Student student) {
            binding.tvStudentName.setText(student.getName());
            binding.tvPassportId.setText(student.getPassportId());
            binding.tvClass.setText(student.getStudentClass());
            binding.tvKyc.setText(student.getKycStatus());
            binding.getRoot().setOnClickListener(v -> listener.onClick(student));
        }
    }
}
