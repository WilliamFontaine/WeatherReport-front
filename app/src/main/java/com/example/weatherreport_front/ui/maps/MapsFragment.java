package com.example.weatherreport_front.ui.maps;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.weatherreport_front.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    public static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private GoogleMap gMap;
    private ImageButton settingsButton;
    private float radius = 10.0f;
    private float size = 10.0f;
    public Location currentLocation;
    public FusedLocationProviderClient fusedLocationProviderClient;

    private Circle circle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        getLastLocation();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        requireActivity().getSupportFragmentManager().setFragmentResultListener("settingsSaved", this, (requestKey, result) -> {
            radius = result.getFloat("radius");
            size = result.getFloat("size");
            addRadiusToMap();
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsButton = view.findViewById(R.id.maps_settings);
        settingsButton.setOnClickListener(v -> {
            BottomSheetDialogFragment bottomSheetDialogFragment = new MapsSettingsFragment();
            bottomSheetDialogFragment.show(requireActivity().getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

            Bundle result = new Bundle();
            result.putFloat("radius", radius);
            result.putFloat("size", size);
            requireActivity().getSupportFragmentManager().setFragmentResult("settings", result);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        if (currentLocation == null) {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            return;
        }
        Task<Location> task =LocationServices.getFusedLocationProviderClient(requireContext()).getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                gMap.moveCamera(newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                gMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title("You are here"));
                addRadiusToMap();
            }
        });
    }

    private void addRadiusToMap() {
        if (circle != null) {
            circle.remove();
        }

        circle = gMap.addCircle(new CircleOptions()
                .center(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .radius(radius * 1000)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(30, 150, 50, 50))
                .strokeWidth(5));
    }
}