package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.databinding.AdapterSearchRecordBinding;
import com.fongmi.android.tv.setting.Setting;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends StringDiffAdapter<RecordAdapter.ViewHolder> {

    private final OnClickListener listener;

    public RecordAdapter(OnClickListener listener) {
        this.listener = listener;
        setItems(loadItems());
        listener.onDataChanged(getItemCount());
    }

    public interface OnClickListener {

        void onItemClick(String text);

        void onDataChanged(int size);
    }

    private List<String> loadItems() {
        if (Setting.getKeyword().isEmpty()) return new ArrayList<>();
        return App.gson().fromJson(Setting.getKeyword(), TypeToken.getParameterized(List.class, String.class).getType());
    }

    public void add(String item) {
        List<String> items = new ArrayList<>(getItems());
        items.remove(item);
        items.add(0, item);
        if (items.size() > 9) items.remove(9);
        setItems(items);
        listener.onDataChanged(getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterSearchRecordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = getItem(position);
        holder.binding.text.setText(text);
        holder.binding.text.setOnClickListener(v -> listener.onItemClick(text));
    }

    public class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder implements View.OnLongClickListener {

        private final AdapterSearchRecordBinding binding;

        public ViewHolder(@NonNull AdapterSearchRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            List<String> items = new ArrayList<>(getItems());
            int position = getLayoutPosition();
            if (position < 0 || position >= items.size()) return false;
            items.remove(position);
            setItems(items);
            listener.onDataChanged(getItemCount());
            Setting.putKeyword(App.gson().toJson(items));
            return true;
        }
    }
}
