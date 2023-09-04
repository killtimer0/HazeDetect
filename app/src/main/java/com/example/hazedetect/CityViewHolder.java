package com.example.hazedetect;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hazedetect.databinding.CityViewBinding;

public class CityViewHolder extends RecyclerView.ViewHolder {
    private final CityViewBinding binding;

    public CityViewHolder(@NonNull View itemView) {
        super(itemView);

        binding = CityViewBinding.bind(itemView);
    }

    public void updateData(LocationInfo locationInfo) {
        binding.textCityName.setText(locationInfo.name);
        binding.textCountry.setText(locationInfo.country);
        binding.textAdm1.setText(locationInfo.adm1);
        binding.textAdm2.setText(locationInfo.adm2);
    }
}