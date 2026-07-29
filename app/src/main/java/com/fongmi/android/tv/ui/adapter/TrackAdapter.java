package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.bean.Track;
import com.fongmi.android.tv.databinding.AdapterTrackBinding;

import java.util.List;

public class TrackAdapter extends BaseDiffAdapter<Track, TrackAdapter.ViewHolder> {

    private final OnClickListener listener;

    public TrackAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {

        void onItemClick(Track item);
    }

    public TrackAdapter withItems(List<Track> items) {
        super.addAll(items);
        return this;
    }

    public int getSelected() {
        for (int i = 0; i < getItemCount(); i++) if (getItem(i).isSelected()) return i;
        return 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterTrackBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track item = getItem(position);
        holder.binding.text.setText(item.getName());
        holder.binding.text.setSelected(item.isSelected());
    }

    public class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder implements View.OnClickListener {

        private final AdapterTrackBinding binding;

        public ViewHolder(@NonNull AdapterTrackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(getItem(getLayoutPosition()).toggle());
        }
    }
}
