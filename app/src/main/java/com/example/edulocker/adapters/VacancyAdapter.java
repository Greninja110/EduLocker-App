package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemVacancyBinding;
import com.example.edulocker.models.Vacancy;

import java.util.List;

public class VacancyAdapter extends RecyclerView.Adapter<VacancyAdapter.ViewHolder> {

    public interface OnEditVacancy {
        void onEdit(Vacancy vacancy);
    }

    public interface OnDeleteVacancy {
        void onDelete(Vacancy vacancy);
    }

    private final List<Vacancy> items;
    private final boolean showAdminActions;
    private final OnEditVacancy editListener;
    private final OnDeleteVacancy deleteListener;

    public VacancyAdapter(List<Vacancy> items, boolean showAdminActions,
                          OnEditVacancy editListener, OnDeleteVacancy deleteListener) {
        this.items = items;
        this.showAdminActions = showAdminActions;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVacancyBinding binding = ItemVacancyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemVacancyBinding binding;

        ViewHolder(ItemVacancyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Vacancy v) {
            binding.tvClassStream.setText(v.getClassStream());
            binding.tvSeats.setText(v.getSeats() + " seats available");
            binding.tvContactPhone.setText(v.getContactPhone() != null ? v.getContactPhone() : "");
            binding.tvContactEmail.setText(v.getContactEmail() != null ? v.getContactEmail() : "");

            if (showAdminActions) {
                binding.btnEditVacancy.setVisibility(View.VISIBLE);
                binding.btnDeleteVacancy.setVisibility(View.VISIBLE);
                binding.btnEditVacancy.setOnClickListener(x -> editListener.onEdit(v));
                binding.btnDeleteVacancy.setOnClickListener(x -> deleteListener.onDelete(v));
            } else {
                binding.btnEditVacancy.setVisibility(View.GONE);
                binding.btnDeleteVacancy.setVisibility(View.GONE);
            }
        }
    }
}
