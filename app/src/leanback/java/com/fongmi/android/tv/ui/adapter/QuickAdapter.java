package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterQuickBinding;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.List;

public class QuickAdapter extends BaseDiffAdapter<Vod, QuickAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final int width;

    public QuickAdapter(OnClickListener listener) {
        mListener = listener;
        int space = ResUtil.dp2px(24) + ResUtil.dp2px(32);
        width = (ResUtil.getScreenWidth() - space) / 4;
    }

    public void addAll(List<Vod> items) {
        List<Vod> current = new ArrayList<>(getItems());
        current.addAll(items);
        setItems(current);
    }

    public void remove(int position) {
        List<Vod> current = new ArrayList<>(getItems());
        current.remove(position);
        setItems(current);
    }

    public Vod get(int position) {
        return getItem(position);
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(AdapterQuickBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        holder.binding.getRoot().getLayoutParams().width = width;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vod item = getItem(position);
        holder.binding.name.setText(item.getName());
        holder.binding.site.setText(item.getSiteName());
        holder.binding.remark.setText(item.getRemarks());
        holder.binding.getRoot().setOnClickListener(v -> mListener.onItemClick(item));
    }

    public interface OnClickListener {

        void onItemClick(Vod item);
    }

    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        private final AdapterQuickBinding binding;

        ViewHolder(@NonNull AdapterQuickBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
