package com.fongmi.android.tv.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class StringDiffAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final AsyncListDiffer<String> differ;

    public StringDiffAdapter() {
        differ = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    public String getItem(int position) {
        return differ.getCurrentList().get(position);
    }

    public List<String> getItems() {
        return differ.getCurrentList();
    }

    public void setItems(List<String> items) {
        differ.submitList(Objects.requireNonNullElseGet(items, ArrayList::new));
    }

    public void addAll(List<String> items) {
        List<String> current = new ArrayList<>(getItems());
        current.addAll(items);
        setItems(current);
    }

    public void clear() {
        setItems(new ArrayList<>());
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    @NonNull
    @Override
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(@NonNull VH holder, int position);
}
