package com.example.weatherreport_front.ui.maps;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherreport_front.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    public static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    public Location currentLocation;
    public FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap gMap;
    private ImageButton settingsButton;
    private int radius = 10;
    private int size = 10;
    private Circle circle;

    private List<MarkerOptions> markers = new ArrayList<>();

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
            radius = result.getInt("radius");
            size = result.getInt("size");
            getReports();
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
        Task<Location> task = LocationServices.getFusedLocationProviderClient(requireContext()).getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                gMap.moveCamera(newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15));
                reinitMap();
                getReports();
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

    private void getReports() {
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = getResources().getString(R.string.api_url)
                + "?latitude=" + String.valueOf(currentLocation.getLatitude())
                + "&longitude=" + String.valueOf(currentLocation.getLongitude())
                + "&radius=" + String.valueOf(radius)
                + "&size=" + String.valueOf(size);

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray reportsArray = jsonResponse.getJSONArray("content");


                        markers.clear();
                        reinitMap();

                        for (int i = 0; i < reportsArray.length(); i++) {
                            JSONObject reportObject = reportsArray.getJSONObject(i);
                            double latitude = reportObject.getDouble("latitude");
                            double longitude = reportObject.getDouble("longitude");

                            // Add marker to map
                            LatLng location = new LatLng(latitude, longitude);

                            markers.add(new MarkerOptions()
                                    .position(location)
                                    .title(getMarkerTitle(reportObject.getString("weatherType"), reportObject.getDouble("temperature")))
                                    .icon(getIcon(reportObject.getString("weatherType"))));

                            markers.forEach(gMap::addMarker);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            System.out.println("That didn't work!");
        });

        queue.add(stringRequest);
    }

    private BitmapDescriptor getIcon(String type) {
        int iconResource;
        switch (type) {
            case "CLEAR_SKY":
                iconResource = R.drawable.clear_sky;
                break;
            case "FEW_CLOUDS":
                iconResource = R.drawable.few_clouds;
                break;
            case "PARTLY_CLOUDY":
                iconResource = R.drawable.partly_cloudy;
                break;
            case "OVERCAST":
                iconResource = R.drawable.overcast;
                break;
            case "FOG":
                iconResource = R.drawable.fog;
                break;
            case "SHOWERS":
                iconResource = R.drawable.showers;
                break;
            case "RAIN":
                iconResource = R.drawable.rain;
                break;
            case "SNOW":
                iconResource = R.drawable.snow;
                break;
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }

        BitmapDescriptor icon;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconResource);
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 150, 150, false);
        icon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        return icon;
    }

    private String getMarkerTitle(String type, double temperature) {
        String weatherType = "";
        switch (type) {
            case "CLEAR_SKY":
                weatherType = getString(R.string.weather_type_clear_sky);
                break;
            case "FEW_CLOUDS":
                weatherType = getString(R.string.weather_type_few_cloud);
                break;
            case "PARTLY_CLOUDY":
                weatherType = getString(R.string.weather_type_partly_cloudy);
                break;
            case "OVERCAST":
                weatherType = getString(R.string.weather_type_overcast);
                break;
            case "FOG":
                weatherType = getString(R.string.weather_type_fog);
                break;
            case "SHOWERS":
                weatherType = getString(R.string.weather_type_showers);
                break;
            case "RAIN":
                weatherType = getString(R.string.weather_type_rain);
                break;
            case "SNOW":
                weatherType = getString(R.string.weather_type_snow);
                break;
        }

        return weatherType + " - " + getString(R.string.temperature) + ": " + temperature + "°C";
    }

    private void reinitMap() {
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title(getString(R.string.current_location)));
        addRadiusToMap();
    }

}