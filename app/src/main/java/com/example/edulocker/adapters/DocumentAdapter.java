package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemDocumentBinding;
import com.example.edulocker.models.EduDocument;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    public interface DocumentClickListener {
        void onDocumentClick(EduDocument document);
    }

    public interface DocumentDeleteListener {
        void onDocumentDelete(EduDocument document, int position);
    }

    private final List<EduDocument> documents;
    private final DocumentClickListener clickListener;
    private final DocumentDeleteListener deleteListener;
    // When set, only show delete for documents uploaded by this user; null = show for all
    private final String currentUserId;

    public DocumentAdapter(List<EduDocument> documents, DocumentClickListener clickListener) {
        this(documents, clickListener, null, null);
    }

    public DocumentAdapter(List<EduDocument> documents, DocumentClickListener clickListener,
                           DocumentDeleteListener deleteListener, String currentUserId) {
        this.documents = documents;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
        this.currentUserId = currentUserId;
    }

    public void removeAt(int position) {
        documents.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDocumentBinding binding = ItemDocumentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(documents.get(position), position);
    }

    @Override
    public int getItemCount() { return documents.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDocumentBinding binding;

        ViewHolder(ItemDocumentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(EduDocument doc, int position) {
            binding.tvDocTitle.setText(doc.getTitle());
            binding.tvDocType.setText(doc.getType());
            binding.tvDocYear.setText(doc.getAcademicYear() != null ? doc.getAcademicYear() : "");
            binding.tvVerified.setText(doc.isVerified() ? "✓ Verified" : "Pending");

            binding.getRoot().setOnClickListener(v -> clickListener.onDocumentClick(doc));

            // Show delete icon only when a deleteListener is provided and permission matches
            boolean canDelete = deleteListener != null &&
                    (currentUserId == null || currentUserId.equals(doc.getUploadedByUserId()));
            binding.ivDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
            if (canDelete) {
                binding.ivDelete.setOnClickListener(v ->
                        deleteListener.onDocumentDelete(doc, getAdapterPosition()));
            }
        }
    }
}
