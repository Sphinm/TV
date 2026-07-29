package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Cache;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.databinding.AdapterTypeBinding;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.List;

public class TypeAdapter extends BaseDiffAdapter<Class, TypeAdapter.ViewHolder> {

    private final OnClickListener mListener;

    public TypeAdapter(OnClickListener listener) {
        mListener = listener;
    }

    public void addAll(List<Class> items) {
        setItems(items);
    }

    public Class get(int position) {
        return getItem(position);
    }

    public int indexOf(Class item) {
        return getItems().indexOf(item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Class item = getItem(position);
        holder.binding.text.setText(item.getTypeName());
        holder.binding.text.setCompoundDrawablePadding(ResUtil.dp2px(4));
        holder.binding.text.setCompoundDrawablesWithIntrinsicBounds(0, 0, getIcon(item), 0);
        holder.binding.text.setListener(() -> mListener.onRefresh(item));
        holder.binding.getRoot().setOnClickListener(v -> mListener.onItemClick(item));
    }

    private int getIcon(Class item) {
        return Cache.get(item).isEmpty() ? 0 : item.getFilter() ? R.drawable.ic_vod_filter_off : R.drawable.ic_vod_filter_on;
    }

    public interface OnClickListener {

        void onItemClick(Class item);

        void onRefresh(Class item);
    }

    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        private final AdapterTypeBinding binding;

        ViewHolder(@NonNull AdapterTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
