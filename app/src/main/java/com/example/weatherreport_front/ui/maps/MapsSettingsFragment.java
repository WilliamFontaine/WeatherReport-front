package com.example.weatherreport_front.ui.maps;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.weatherreport_front.MainActivity;
import com.example.weatherreport_front.R;
import com.example.weatherreport_front.databinding.FragmentMapsSettingsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MapsSettingsFragment extends BottomSheetDialogFragment {

    private FragmentMapsSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapsSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        view.findViewById(R.id.search).setOnClickListener(v -> {;
            Bundle result = new Bundle();
            result.putFloat("radius", binding.radiusSlider.getValues().get(0));
            result.putFloat("size", binding.sizeSlider.getValues().get(0));
            requireActivity().getSupportFragmentManager().setFragmentResult("settingsSaved", result);
            dismiss();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getSupportFragmentManager().setFragmentResultListener("settings", this, (requestKey, result) -> {
            binding.radiusSlider.setValues(result.getFloat("radius"));
            binding.sizeSlider.setValues(result.getFloat("size"));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}