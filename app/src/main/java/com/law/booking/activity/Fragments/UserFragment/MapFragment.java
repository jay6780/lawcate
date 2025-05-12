package com.law.booking.activity.Fragments.UserFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.auth.FirebaseAuth;
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

    private LocationCallback locationCallback;
    private Bundle mapstate;
    private LatLng currentLatLng;
    private FloatingActionButton refresh;
    private Marker currentLocationMarker;
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
        Log.d("AddressList","value: "+addressList);
        if (addressList.isEmpty()) {
            Toast.makeText(getContext(), "No addresses found", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        for (String addressStr : addressList) {
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
                if (addresses != null && !addresses.isEmpty()) {
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

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String userProvince = null;
        String selectedProvince = null;

        try {
            List<Address> userAddresses = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1);
            List<Address> selectedAddresses = geocoder.getFromLocationName(selectedAddress, 1);

            if (userAddresses != null && !userAddresses.isEmpty()) {
                userProvince = userAddresses.get(0).getAdminArea();
            }

            if (selectedAddresses != null && !selectedAddresses.isEmpty()) {
                selectedProvince = selectedAddresses.get(0).getAdminArea();
            }

        } catch (IOException e) {
            Log.e("MapFragment", "Error checking provinces: " + e.getMessage());
        }

        if (userProvince == null || selectedProvince == null || !userProvince.equalsIgnoreCase(selectedProvince)) {
            MarkerOptions destinationMarkerOptions = new MarkerOptions()
                    .position(userLocation)
                    .title("Different Province")
                    .snippet("No providers found in your province")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.addMarker(destinationMarkerOptions);
//            Toast.makeText(getContext(), "No providers found in your province", Toast.LENGTH_SHORT).show();
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
                    if (usermodel == null || currentLatLng == null) {
                        currentLocationMarker.showInfoWindow();
                        return true;
                    }

                        Log.d("CurrentLatlang", "value: " + currentLatLng);
                        RouteFetcher routeFetcher = new RouteFetcher();
                        ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Loading....");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        routeFetcher.fetchRouteData(getLatLngFromAddress(usermodel.getAddress()), currentLatLng, new RouteFetcher.RouteFetchListener() {
                            @Override
                            public void onSuccess(JSONObject jsonResponse) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    progressDialog.dismiss();
                                    try {
                                        JSONObject route = jsonResponse.getJSONObject("route");
                                        int distanceMeters = route.getInt("distance");
                                        double updatedDistanceKm = distanceMeters / 1000.0;

                                        int durationSeconds = route.getInt("duration");
                                        int hours = durationSeconds / 3600;
                                        int minutes = (durationSeconds % 3600) / 60;
                                        int seconds = durationSeconds % 60;
                                        String durationText;
                                        if (hours > 0) {
                                            durationText = String.format("%d hrs %d min", hours, minutes);
                                        } else if (minutes > 0) {
                                            durationText = String.format("%d min", minutes);
                                        } else {
                                            durationText = String.format("%d sec", seconds);
                                        }
                                        Log.d("Duration", "Formatted duration: " + durationText);
                                        Log.d("KM", "Distance: " + updatedDistanceKm + " km");
                                        Dialog showmapkm = new Dialog();
                                        showmapkm.mapkmdialog(getActivity(), usermodel.getAddress(), usermodel.getName(), updatedDistanceKm, usermodel.getImage(), usermodel, durationText);
                                    } catch (JSONException e) {
                                        Log.e("MapFragment", "Error parsing JSON response: " + e.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Failed to fetch route: " + errorMessage, Toast.LENGTH_SHORT).show();
                                });

                                Log.e("MapFragment", "Error fetching route data: " + errorMessage);
                            }
                        });
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
        LatLng destinationLocation = null;
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
                        initMe();
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

    private void initMe() {
        String myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference merReference = FirebaseDatabase.getInstance().getReference("Client").child(myUserId);
        merReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String image = snapshot.child("image").getValue(String.class);
//                Log.d("MyInfo","value: "+"name: "+name +" image: "+image);
                if(name !=null && image!=null){
                    loadUserImageAndAddMarker(name,image,userLocation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadUserImageAndAddMarker(String name, String image, LatLng offsetLocation) {
        Context context = getContext();
        if (context == null) {
            Log.e("MapFragment", "Context is null, cannot create Geocoder.");
            return;
        }

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(offsetLocation.latitude, offsetLocation.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                StringBuilder locationText = new StringBuilder();
                android.location.Address address = addresses.get(0);
                if (address.getThoroughfare() != null) {
                    locationText.append(address.getThoroughfare());
                }
                if (address.getLocality() != null) {
                    locationText.append(", ").append(address.getLocality());
                }
                if (address.getSubAdminArea() != null) {
                    locationText.append(", ").append(address.getSubAdminArea());
                }


                Glide.with(getContext())
                        .asBitmap()
                        .load(image)
                        .override(100, 100)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Bitmap circularBitmap = getCircularBitmap(resource);
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(offsetLocation)
                                        .title(name)
                                        .snippet("Address: " + locationText)
                                        .icon(BitmapDescriptorFactory.fromBitmap(circularBitmap));
                                if (currentLocationMarker != null) {
                                    currentLocationMarker.remove();
                                }
                                currentLocationMarker = googleMap.addMarker(markerOptions);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                currentLocationMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title(locationText.toString()));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                currentLocationMarker.showInfoWindow();
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
