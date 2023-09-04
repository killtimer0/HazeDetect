package com.example.hazedetect;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CityListAdapter extends RecyclerView.Adapter<CityViewHolder> {
    private @Nullable LocationInfo[] locationInfo;

    @SuppressLint("NotifyDataSetChanged")
    public void updateCityList(@Nullable LocationInfo[] locationInfo) {
        this.locationInfo = locationInfo;
        notifyDataSetChanged();
    }

    public static interface Listener {
        void onLocationSelected(LocationInfo locationInfo);
    }

    private @Nullable Listener listener;
    public void setLocationSelectedListener(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_view, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        if (null != locationInfo) {
            holder.itemView.setOnClickListener(v -> {
                if (null != listener) {
                    listener.onLocationSelected(locationInfo[position]);
                }
            });
            holder.updateData(locationInfo[position]);
        }
    }

    @Override
    public int getItemCount() {
        return null == locationInfo ?  0 : locationInfo.length;
    }
}
