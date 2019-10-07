package com.sthaabhash.mycheckins.activities;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sthaabhash.mycheckins.R;

public class MyMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_maps);

        if (getIntent()!=null) {
            lat = Double.parseDouble(getIntent().getStringExtra("lat"));
            lng = Double.parseDouble(getIntent().getStringExtra("lng"));
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        updateUI();
    }

    private void updateUI() {
        LatLng selectedPoint=new LatLng(lat,lng);

        MarkerOptions myMarker=new MarkerOptions().position(selectedPoint).title("Your Location");
        mMap.clear();
        mMap.addMarker(myMarker);

        int zoomLevel=14;
        CameraUpdate update= CameraUpdateFactory.newLatLngZoom(selectedPoint,zoomLevel);
        mMap.animateCamera(update);
    }
}
