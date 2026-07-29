package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.media3.common.MediaEdition;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.databinding.AdapterEditionBinding;
import com.fongmi.android.tv.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EditionAdapter extends RecyclerView.Adapter<EditionAdapter.ViewHolder> {

    private final OnClickListener listener;
    private final AsyncListDiffer<MediaEdition> differ;

    public EditionAdapter(OnClickListener listener) {
        this.listener = listener;
        this.differ = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull MediaEdition oldItem, @NonNull MediaEdition newItem) {
                return Objects.equals(oldItem.label, newItem.label) && oldItem.durationUs == newItem.durationUs;
            }

            @Override
            public boolean areContentsTheSame(@NonNull MediaEdition oldItem, @NonNull MediaEdition newItem) {
                return oldItem.selected == newItem.selected && Objects.equals(oldItem.label, newItem.label) && oldItem.durationUs == newItem.durationUs;
            }
        });
    }

    public interface OnClickListener {

        void onItemClick(MediaEdition item);
    }

    public EditionAdapter addAll(List<MediaEdition> items) {
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
        return new ViewHolder(AdapterEditionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaEdition item = differ.getCurrentList().get(position);
        holder.binding.text.setSelected(item.selected);
        holder.binding.text.setText(getText(item));
    }

    private String getText(MediaEdition item) {
        if (item.durationUs == C.TIME_UNSET) return item.label;
        return item.label + " [" + Util.timeMs(item.durationUs / 1000) + "]";
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final AdapterEditionBinding binding;

        public ViewHolder(@NonNull AdapterEditionBinding binding) {
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
