package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edulocker.databinding.ItemNoticeBinding;
import com.example.edulocker.models.Notice;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    public interface NoticeActionListener {
        void onEdit(Notice notice);
        void onDelete(Notice notice);
    }

    private final List<Notice> notices;
    private final NoticeActionListener actionListener;

    public NoticeAdapter(List<Notice> notices) {
        this.notices = notices;
        this.actionListener = null;
    }

    public NoticeAdapter(List<Notice> notices, NoticeActionListener actionListener) {
        this.notices = notices;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoticeBinding binding = ItemNoticeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(notices.get(position), actionListener);
    }

    @Override
    public int getItemCount() { return notices.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNoticeBinding binding;

        ViewHolder(ItemNoticeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notice notice, NoticeActionListener listener) {
            binding.tvNoticeTitle.setText(notice.getTitle());
            binding.tvNoticeContent.setText(notice.getContent());
            binding.tvPostedBy.setText("— " + notice.getPostedByName());

            if (Notice.TYPE_STATE.equals(notice.getNoticeType())) {
                String stateLabel = (notice.getState() != null && !notice.getState().isEmpty()
                        && !"All".equals(notice.getState()))
                        ? notice.getState()
                        : "All India";
                binding.tvNoticeTypeBadge.setText("STATE  •  " + stateLabel);
            } else {
                binding.tvNoticeTypeBadge.setText("SCHOOL");
            }

            if (notice.getTimestamp() != null) {
                String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(notice.getTimestamp().toDate());
                binding.tvDate.setText(date);
            }

            if (listener != null) {
                binding.btnEditNotice.setVisibility(View.VISIBLE);
                binding.btnDeleteNotice.setVisibility(View.VISIBLE);
                binding.btnEditNotice.setOnClickListener(v -> listener.onEdit(notice));
                binding.btnDeleteNotice.setOnClickListener(v -> listener.onDelete(notice));
            } else {
                binding.btnEditNotice.setVisibility(View.GONE);
                binding.btnDeleteNotice.setVisibility(View.GONE);
            }
        }
    }
}
