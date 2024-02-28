package com.example.weatherreport_front.ui.report;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherreport_front.R;
import com.example.weatherreport_front.databinding.FragmentReportBinding;
import com.example.weatherreport_front.ui.maps.MapsFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

public class ReportFragment extends Fragment {

    public FusedLocationProviderClient fusedLocationProviderClient;
    private FragmentReportBinding binding;
    private String api;
    private double latitude;
    private double longitude;

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        api = getResources().getString(R.string.api_url);


        view.findViewById(R.id.search).setOnClickListener(v -> {
            RadioGroup weatherType = view.findViewById(R.id.weather_type_radio);
            EditText temperature = view.findViewById(R.id.temperature);
            postReport(weatherType, temperature);
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MapsFragment.FINE_LOCATION_ACCESS_REQUEST_CODE);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        });
    }

    private void postReport(RadioGroup weatherType, EditText temperature) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String weather = getWeatherType(weatherType);
        String temp = temperature.getText().toString();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);
            jsonBody.put("weatherType", weather);
            jsonBody.put("temperature", temp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, api, jsonBody,
                response -> {
                    Toast.makeText(requireContext(), getString(R.string.report_success), Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(requireContext(), getString(R.string.report_error), Toast.LENGTH_SHORT).show();
                }
        ) {
        };

        queue.add(postRequest);
    }

    private String getWeatherType(RadioGroup weatherType) {
        int selectedId = weatherType.getCheckedRadioButtonId();
        if (selectedId == R.id.clear_sky) {
            return "CLEAR_SKY";
        } else if (selectedId == R.id.few_cloud) {
            return "FEW_CLOUDS";
        } else if (selectedId == R.id.partly_cloudy) {
            return "PARTLY_CLOUDY";
        } else if (selectedId == R.id.overcast) {
            return "OVERCAST";
        } else if (selectedId == R.id.fog) {
            return "FOG";
        } else if (selectedId == R.id.showers) {
            return "SHOWERS";
        } else if (selectedId == R.id.rain) {
            return "RAIN";
        } else if (selectedId == R.id.snow) {
            return "SNOW";
        }
        return null;
    }
}