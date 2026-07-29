package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.media3.common.MediaChapter;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.databinding.AdapterChapterBinding;
import com.fongmi.android.tv.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {

    private final OnClickListener listener;
    private final AsyncListDiffer<MediaChapter> differ;

    public ChapterAdapter(OnClickListener listener) {
        this.listener = listener;
        this.differ = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull MediaChapter oldItem, @NonNull MediaChapter newItem) {
                return oldItem.timeUs == newItem.timeUs && Objects.equals(oldItem.label, newItem.label);
            }

            @Override
            public boolean areContentsTheSame(@NonNull MediaChapter oldItem, @NonNull MediaChapter newItem) {
                return oldItem.selected == newItem.selected && oldItem.timeUs == newItem.timeUs && Objects.equals(oldItem.label, newItem.label);
            }
        });
    }

    public interface OnClickListener {

        void onItemClick(MediaChapter item);
    }

    public ChapterAdapter addAll(List<MediaChapter> items) {
        differ.submitList(new ArrayList<>(items));
        return this;
    }

    public int getSelected() {
        for (int i = 0; i < differ.getCurrentList().size(); i++) if (differ.getCurrentList().get(i).selected) return i;
        return 0;
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterChapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaChapter item = differ.getCurrentList().get(position);
        holder.binding.text.setSelected(item.selected);
        holder.binding.text.setText(getText(item));
    }

    private String getText(MediaChapter item) {
        if (item.timeUs == C.TIME_UNSET) return item.label;
        return item.label + " [" + Util.timeMs(item.timeUs / 1000) + "]";
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final AdapterChapterBinding binding;

        public ViewHolder(@NonNull AdapterChapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(differ.getCurrentList().get(getLayoutPosition()));
        }
    }
}
