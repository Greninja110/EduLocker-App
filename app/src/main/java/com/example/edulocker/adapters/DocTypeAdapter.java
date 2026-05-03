package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemDocTypeBinding;
import com.example.edulocker.models.DocumentType;

import java.util.List;

public class DocTypeAdapter extends RecyclerView.Adapter<DocTypeAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(DocumentType docType);
    }

    private final List<DocumentType> items;
    private final OnDeleteListener deleteListener;

    public DocTypeAdapter(List<DocumentType> items, OnDeleteListener deleteListener) {
        this.items = items;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDocTypeBinding binding = ItemDocTypeBinding.inflate(
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
        private final ItemDocTypeBinding binding;

        ViewHolder(ItemDocTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DocumentType dt) {
            binding.tvDocTypeName.setText(dt.getName());
            binding.tvMandatory.setText(dt.isMandatory() ? "Mandatory" : "Optional");
            binding.tvMandatory.setTextColor(dt.isMandatory() ? 0xFFB71C1C : 0xFF2E7D32);
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDelete(dt));
        }
    }
}
