package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemStudentBinding;
import com.example.edulocker.databinding.ItemTeacherBinding;
import com.example.edulocker.models.Student;
import com.example.edulocker.models.Teacher;

import java.util.List;

public class PeopleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_STUDENT = 0;
    private static final int TYPE_TEACHER = 1;

    public static class PersonItem {
        final int type;
        final Student student;
        final Teacher teacher;

        public PersonItem(Student s) { type = TYPE_STUDENT; student = s; teacher = null; }
        public PersonItem(Teacher t) { type = TYPE_TEACHER; student = null; teacher = t; }
    }

    public interface OnPersonClickListener {
        void onStudentClick(Student student);
        void onTeacherClick(Teacher teacher);
    }

    private List<PersonItem> items;
    private final OnPersonClickListener listener;

    public PeopleListAdapter(List<PersonItem> items, OnPersonClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<PersonItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_TEACHER) {
            return new TeacherViewHolder(ItemTeacherBinding.inflate(inflater, parent, false));
        }
        return new StudentViewHolder(ItemStudentBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PersonItem item = items.get(position);
        if (item.type == TYPE_TEACHER) {
            ((TeacherViewHolder) holder).bind(item.teacher);
        } else {
            ((StudentViewHolder) holder).bind(item.student);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ── Student ViewHolder ──────────────────────────────────────────────────
    class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding b;
        StudentViewHolder(ItemStudentBinding b) { super(b.getRoot()); this.b = b; }

        void bind(Student s) {
            b.tvStudentName.setText(s.getName());
            b.tvPassportId.setText(s.getPassportId());
            String classInfo = s.getStudentClass() != null ? s.getStudentClass() : "";
            if (s.getSection() != null && !s.getSection().isEmpty())
                classInfo += " – " + s.getSection();
            b.tvClass.setText(classInfo);
            String kyc = s.getKycStatus() != null ? s.getKycStatus() : "Pending";
            b.tvKyc.setText(kyc);
            b.tvKyc.setTextColor(b.getRoot().getContext().getResources().getColor(
                    "Verified".equals(kyc)
                            ? com.example.edulocker.R.color.verified_green
                            : com.example.edulocker.R.color.primary, null));
            b.getRoot().setOnClickListener(v -> listener.onStudentClick(s));
        }
    }

    // ── Teacher ViewHolder ──────────────────────────────────────────────────
    class TeacherViewHolder extends RecyclerView.ViewHolder {
        private final ItemTeacherBinding b;
        TeacherViewHolder(ItemTeacherBinding b) { super(b.getRoot()); this.b = b; }

        void bind(Teacher t) {
            b.tvTeacherName.setText(t.getName());
            b.tvSubject.setText(t.getSubject() != null ? t.getSubject() : "—");

            String classDiv = "Class " + (t.getAssignedClass() != null ? t.getAssignedClass() : "—");
            if (t.getDivision() != null && !t.getDivision().isEmpty())
                classDiv += " – " + t.getDivision();
            b.tvClassDivision.setText(classDiv);

            // Show first letter of name as avatar initials
            String initials = t.getName() != null && !t.getName().isEmpty()
                    ? String.valueOf(t.getName().charAt(0)).toUpperCase() : "F";
            b.tvInitials.setText(initials);

            b.getRoot().setOnClickListener(v -> listener.onTeacherClick(t));
        }
    }
}
