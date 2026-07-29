package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.databinding.AdapterPartBinding;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.List;

public class PartAdapter extends StringDiffAdapter<PartAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final int maxWidth;
    private int nextFocusUp;

    public PartAdapter(OnClickListener listener) {
        mListener = listener;
        maxWidth = ResUtil.getScreenWidth() - ResUtil.dp2px(48);
    }

    public void addAll(List<String> items) {
        setItems(items);
    }

    public void setNextFocusUp(int nextFocusUp) {
        this.nextFocusUp = nextFocusUp;
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterPartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = getItem(position);
        holder.binding.text.setText(text);
        holder.binding.text.setMaxWidth(maxWidth);
        holder.binding.text.setNextFocusUpId(nextFocusUp);
        holder.binding.getRoot().setOnClickListener(v -> mListener.onItemClick(text));
    }

    public interface OnClickListener {

        void onItemClick(String item);
    }

    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        private final AdapterPartBinding binding;

        ViewHolder(@NonNull AdapterPartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
