package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.AdapterArrayBinding;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.List;

public class ArrayAdapter extends StringDiffAdapter<ArrayAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final String backward;
    private final String forward;
    private final String reverse;

    public ArrayAdapter(OnClickListener listener) {
        mListener = listener;
        forward = ResUtil.getString(R.string.play_forward);
        reverse = ResUtil.getString(R.string.play_reverse);
        backward = ResUtil.getString(R.string.play_backward);
    }

    public void addAll(List<String> items) {
        setItems(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterArrayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = getItem(position);
        holder.binding.text.setText(text);
        if (text.equals(reverse)) holder.binding.getRoot().setOnClickListener(view -> mListener.onRevSort());
        else if (text.equals(backward) || text.equals(forward)) holder.binding.getRoot().setOnClickListener(view -> mListener.onRevPlay(holder.binding.text));
        else holder.binding.getRoot().setOnClickListener(null);
    }

    public interface OnClickListener {

        void onRevSort();

        void onRevPlay(TextView view);
    }

    public static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {

        private final AdapterArrayBinding binding;

        ViewHolder(@NonNull AdapterArrayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
