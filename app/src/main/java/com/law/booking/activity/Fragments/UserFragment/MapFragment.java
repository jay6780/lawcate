package com.law.booking.activity.Fragments.UserFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.law.booking.R;
import com.law.booking.activity.tools.DialogUtils.Dialog;
import com.law.booking.activity.tools.Model.RouteFetcher;
import com.law.booking.activity.tools.Model.Usermodel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView mapView;
    private LatLng userLocation;
    private LatLng destinationLocation;
    private LatLng providerlocation,eventlocation;
    private LocationCallback locationCallback;
    private Bundle mapstate;
    private double distanceKm;
    private LatLng currentLatLng;
    private FloatingActionButton refresh;
    @Nullable
    @Override


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmentmap, container, false);
        mapView = view.findViewById(R.id.mapview);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        refresh = view.findViewById(R.id.floating_refresh);
        mapstate = savedInstanceState;
        refresh.setOnClickListener(view1 ->initMap(mapView, mapstate));
        initMap(mapView, mapstate);
        return view;
    }




    private void initdatabase() {
        List<String> hmuaAddresslist = new ArrayList<>();
        DatabaseReference adminref = FirebaseDatabase.getInstance().getReference("Lawyer");
        adminref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Usermodel user = dataSnapshot.getValue(Usermodel.class);
                        if (user != null && user.getAddress() != null) {
                            Boolean isSuperAdmin = dataSnapshot.child("isSuperAdmin").getValue(Boolean.class);
                            if (isSuperAdmin == null || !isSuperAdmin) {
                                hmuaAddresslist.add(user.getAddress());
                                moveCameraToAddresses(hmuaAddresslist);
                            }
                        }


                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database error: " + error.getMessage());
            }
        });
    }


    private void moveCameraToAddresses(List<String> addressList) {
        if (addressList.isEmpty()) {
            Toast.makeText(getContext(), "No addresses found", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        for (String addressStr : addressList) {
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    providerlocation = new LatLng(address.getLatitude(), address.getLongitude());
                    fetchProvidersAndShowMarkers(addressStr);

                } else {
                    Log.e("MapFragment", "Address not found: " + addressStr);
                }
            } catch (Exception e) {
                Log.e("MapFragment", "Error moving camera: " + e.getMessage());
            }
        }
    }

    private LatLng getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address location = addressList.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            Log.e("Geocoding", "Error getting LatLng from address: " + e.getMessage());
        }
        return null;
    }




    private void fetchProvidersAndShowMarkers(String selectedAddress) {
        String city = null;
        String countrycode = null;
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                city = address.getLocality();
                countrycode = address.getCountryName();
            }
        } catch (IOException e) {
            Log.e("MapFragment", "Error geocoding destination location: " + e.getMessage());
        }

        if (city == null || selectedAddress == null || !selectedAddress.contains(city)) {
            MarkerOptions destinationMarkerOptions = new MarkerOptions()
                    .position(userLocation)
                    .title(city + ", " + countrycode)
                    .snippet("No providers found")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.addMarker(destinationMarkerOptions);
            Toast.makeText(getContext(), "providers not found", Toast.LENGTH_SHORT).show();
            return;
        }


        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Lawyer");
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean providerFound = false;
                Map<LatLng, Integer> locationCountMap = new HashMap<>();
                googleMap.clear();

                googleMap.setOnMarkerClickListener(marker -> {
                    Usermodel usermodel = (Usermodel) marker.getTag();
                    if (usermodel != null) {
                        if (currentLatLng == null) {
                            Log.e("MapFragment", "Current location is null. Cannot fetch route data.");
                            return true;
                        }
                        RouteFetcher routeFetcher = new RouteFetcher();
                        routeFetcher.fetchRouteData(getLatLngFromAddress(usermodel.getAddress()), currentLatLng, new RouteFetcher.RouteFetchListener() {
                            @Override
                            public void onSuccess(JSONObject jsonResponse) {
                                try {
                                    JSONObject route = jsonResponse.getJSONObject("route");
                                    int distanceMeters = route.getInt("distance");
                                    double updatedDistanceKm = distanceMeters / 1000.0;
                                    Log.d("KM", "Distance: " + updatedDistanceKm + " km");

                                    Dialog showmapkm = new Dialog();
                                    showmapkm.mapkmdialog(getActivity(), usermodel.getAddress(), usermodel.getName(), updatedDistanceKm, usermodel.getImage(), usermodel);

                                } catch (JSONException e) {
                                    Log.e("MapFragment", "Error parsing JSON response: " + e.getMessage());
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e("MapFragment", "Error fetching route data: " + errorMessage);
                            }
                        });
                    }
                    return true;
                });

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usermodel usermodel = snapshot.getValue(Usermodel.class);
                    if (usermodel == null) continue;

                    usermodel.setKey(snapshot.getKey());

                    Boolean isSuperAdmin = snapshot.child("isSuperAdmin").getValue(Boolean.class);
                    if (isSuperAdmin != null && isSuperAdmin) {
                        continue;
                    }

                    if (usermodel.getAddress() != null && usermodel.getAddress().equalsIgnoreCase(selectedAddress)) {
                        providerFound = true;
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocationName(usermodel.getAddress(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                LatLng providerLocation = new LatLng(address.getLatitude(), address.getLongitude());

                                int count = locationCountMap.getOrDefault(providerLocation, 0);
                                locationCountMap.put(providerLocation, count + 1);
                                LatLng offsetLocation = getOffsetLocation(providerLocation, count);

                                Glide.with(getContext())
                                        .asBitmap()
                                        .load(usermodel.getImage())
                                        .error(R.mipmap.man)
                                        .override(100, 100)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                Bitmap circularBitmap = getCircularBitmap(resource);
                                                MarkerOptions markerOptions = new MarkerOptions()
                                                        .position(offsetLocation)
                                                        .title(usermodel.getUsername())
                                                        .snippet("Address: " + usermodel.getAddress())
                                                        .icon(BitmapDescriptorFactory.fromBitmap(circularBitmap));

                                                Marker marker = googleMap.addMarker(markerOptions);
                                                marker.setTag(usermodel);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                            }
                                        });
                            }
                        } catch (IOException e) {
                            Log.e("MapFragment", "Error geocoding address: " + e.getMessage());
                        }
                    }
                }

                if (!providerFound) {
                    notExistprovider();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                Log.e("MapFragment", "DatabaseError: " + databaseError.getMessage());
            }
        });
    }

    private void notExistprovider() {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String addressText = "Unknown location";
        try {
            List<Address> addresses = geocoder.getFromLocation(destinationLocation.latitude, destinationLocation.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                if (address.getThoroughfare() != null) {
                    sb.append(address.getThoroughfare());
                }
                if (address.getLocality() != null) {
                    sb.append(", ").append(address.getLocality());
                }
                addressText = sb.toString();
            }
        } catch (IOException e) {
            Log.e("MapFragment", "Error geocoding destination location: " + e.getMessage());
        }
        MarkerOptions destinationMarkerOptions = new MarkerOptions()
                .position(destinationLocation)
                .title(addressText)
                .snippet("No providers found")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        googleMap.addMarker(destinationMarkerOptions);
        Toast.makeText(getContext(), "No providers found at this address", Toast.LENGTH_SHORT).show();
    }


    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return output;
    }

    private LatLng getOffsetLocation(LatLng originalLocation, int index) {
        double offset = 0.0001 * index;
        return new LatLng(originalLocation.latitude + offset, originalLocation.longitude + offset);
    }

    private void initMap(MapView mapView, Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.Apikey));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        requestAndSetUserLocation();
    }

    @SuppressLint("MissingPermission")
    private void requestAndSetUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        initdatabase();
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14));
                        geocodeLocation(userLocation);
                        startLocationUpdates();
                    } else {
                        Log.e("MapFragment", "User location is null");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MapFragment", "Error getting user location: " + e.getMessage());
                });
    }

    private void geocodeLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder locationText = new StringBuilder();
                if (address.getSubThoroughfare() != null) {
                    locationText.append(" ").append(address.getSubThoroughfare());
                }
            }
        } catch (IOException e) {
            Log.e("MapFragment", "Geocoding failed: " + e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        };
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (mapView == null) {
                mapView.onStart();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("Error", "Please open your location services");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (mapView != null) {
                mapView.onResume();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("MapError", "Please open your location services");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (mapView != null) {
                mapView.onPause();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("MapError", "Please open your location services");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (mapView != null) {
                mapView.onStop();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("MapError", "Please open your location services");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mapView != null) {
                mapView.onDestroy();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("MapError", "Please open your location services");
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try {
            if (mapView != null) {
                mapView.onLowMemory();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("MapError", "Please open your location services");
        }
    }
}
