package com.example.edulocker.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edulocker.databinding.ItemNewsBinding;
import com.example.edulocker.models.NewsItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private final List<NewsItem> items;

    public NewsAdapter(List<NewsItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNewsBinding binding = ItemNewsBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNewsBinding binding;

        ViewHolder(ItemNewsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NewsItem item) {
            binding.tvSource.setText(item.getSourceName());
            binding.tvTitle.setText(item.getTitle());
            binding.tvContent.setText(item.getDescription());

            // Show first category as the "views" label
            String cat = (item.getCategories() != null && !item.getCategories().isEmpty())
                    ? item.getCategories().get(0) : "";
            binding.tvLikes.setText(cat.isEmpty() ? "" : "#" + cat);

            // Parse pubDate "yyyy-MM-dd HH:mm:ss" → "dd MMM yyyy"
            if (item.getPubDate() != null && !item.getPubDate().isEmpty()) {
                try {
                    java.util.Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .parse(item.getPubDate());
                    binding.tvDate.setText(
                            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d));
                } catch (Exception e) {
                    binding.tvDate.setText(item.getPubDate().substring(0, Math.min(10, item.getPubDate().length())));
                }
            }

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(item.getImageUrl())
                        .centerCrop()
                        .placeholder(com.example.edulocker.R.mipmap.ic_launcher)
                        .into(binding.ivNewsImage);
            }
        }
    }
}
