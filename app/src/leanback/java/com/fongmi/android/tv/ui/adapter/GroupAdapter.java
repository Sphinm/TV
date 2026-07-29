package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.bean.Group;
import com.fongmi.android.tv.databinding.AdapterGroupBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupAdapter extends BaseDiffAdapter<Group, GroupAdapter.ViewHolder> {

    private final OnClickListener mListener;

    public GroupAdapter(OnClickListener listener) {
        mListener = listener;
    }

    public void addAll(List<Group> items) {
        setItems(items);
    }

    public void add(int position, Group item) {
        List<Group> current = new ArrayList<>(getItems());
        current.add(position, item);
        setItems(current);
    }

    public Group get(int position) {
        return getItem(position);
    }

    public int indexOf(Group item) {
        return getItems().indexOf(item);
    }

    public List<Group> unmodifiableList() {
        return Collections.unmodifiableList(getItems());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group item = getItem(position);
        holder.binding.name.setText(item.getName());
        holder.binding.getRoot().setOnClickListener(v -> mListener.onItemClick(item));
    }

    public interface OnClickListener {
        void onItemClick(Group item);
    }

    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        private final AdapterGroupBinding binding;

        ViewHolder(@NonNull AdapterGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
