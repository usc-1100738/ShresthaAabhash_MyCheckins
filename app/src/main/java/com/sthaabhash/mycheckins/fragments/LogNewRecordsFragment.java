package com.sthaabhash.mycheckins.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sthaabhash.mycheckins.BuildConfig;
import com.sthaabhash.mycheckins.R;
import com.sthaabhash.mycheckins.activities.MyMapsActivity;
import com.sthaabhash.mycheckins.database.DBHandler;
import com.sthaabhash.mycheckins.model.RecordsModel;

import java.io.File;
import java.util.Calendar;

public class LogNewRecordsFragment extends Fragment implements DatePickerDialog.OnDateSetListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private EditText et_title, et_place, et_details;
    private Button btn_date, btnShowMap, btnClickImage, btnShare, btnDelete;
    private TextView tvLocation;
    private ImageView iv_receiptImage;
    private DBHandler dbHandler;
    private String imageRes;
    private String key = "";
    private RecordsModel recordsModel;
    private int id;

    private Long tsLong = System.currentTimeMillis() / 1000;
    private String ts = tsLong.toString();

    private static final int CAM_REQUEST = 0;
    private static final int REQUEST_CODE_PERMISSION = 3;
    private String[] mPermission = {Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};

    //    private GoogleApiClient mClient;
    private GoogleApiClient mGoogleApiClient;

    String lat = "", lng = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            id = getArguments().getInt("position");
            key = getArguments().getString("key");
            recordsModel = (RecordsModel) getArguments().getSerializable("data");
        }


        return inflater.inflate(R.layout.fragment_log_new_records, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//capture GPS location only if its new Log record
        if (!key.equals("delete")) {
            //Initialize Google Play Services
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Location Permission already granted
                    buildGoogleApiClient();
                } else {
                    //Request Location Permission
                    checkLocationPermission();
                }
            } else {
                buildGoogleApiClient();

            }
        }

        dbHandler = new DBHandler(getContext());

        et_title = view.findViewById(R.id.et_title);
        et_place = view.findViewById(R.id.et_place);
        et_details = view.findViewById(R.id.et_detail);

        btn_date = view.findViewById(R.id.btn_date);
        btnShowMap = view.findViewById(R.id.btnShowMap);
        btnClickImage = view.findViewById(R.id.btn_captureImage);
        btnShare = view.findViewById(R.id.btnShare);
        btnDelete = view.findViewById(R.id.btnDelete);

        tvLocation = view.findViewById(R.id.tv_location);
        iv_receiptImage = view.findViewById(R.id.iv_receiptImage);

        btn_date.setText(Calendar.getInstance().get(Calendar.MONTH) + "/" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "/" + Calendar.getInstance().get(Calendar.YEAR));

        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog();
            }
        });

        btnClickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((ActivityCompat.checkSelfPermission(getActivity(), mPermission[0]) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(getActivity(), mPermission, REQUEST_CODE_PERMISSION);

                } else
                    takePhoto();
            }
        });

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lat == "" && lng == "") {
                    Toast.makeText(getContext(), "Requesting Location", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getContext(), MyMapsActivity.class);
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    startActivity(intent);
                }
            }
        });


        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordsModel recordsModel = new RecordsModel();
                recordsModel.setTitle(et_title.getText().toString().trim());
                recordsModel.setDate(btn_date.getText().toString());
                recordsModel.setDetails(et_details.getText().toString());
                recordsModel.setLocation(tvLocation.getText().toString());
                recordsModel.setPlace(et_place.getText().toString());
                recordsModel.setImage(imageRes);
                dbHandler.insertRecords(recordsModel);
                Toast.makeText(getContext(), "Records Saved Successfully", Toast.LENGTH_SHORT).show();
                backToPrevious();
            }
        });


        if (key.equals("delete") && !key.equals("")) {
            et_title.setText(recordsModel.getTitle());
            et_title.setEnabled(false);
            et_place.setText(recordsModel.getPlace());
            et_place.setEnabled(false);
            et_details.setText(recordsModel.getDetails());
            et_details.setEnabled(false);
            btn_date.setText(recordsModel.getDate());
            btn_date.setClickable(false);
            tvLocation.setText(recordsModel.getLocation());
            tvLocation.setEnabled(false);
            Glide.with(getContext()).load(recordsModel.getImage()).into(iv_receiptImage);
            btnClickImage.setEnabled(false);
            btnDelete.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.GONE);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
                    alertBuilder.setTitle("Are you sure you want to delete?");
                    alertBuilder.setMessage("Delete this Record");
                    alertBuilder.setPositiveButton("Deltet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHandler.deleteRecords(recordsModel);
                            Toast.makeText(getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            backToPrevious();
                        }
                    });

                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();

                }
            });
        } else {
            btnShare.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.GONE);
        }

    }

    private void backToPrevious() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction backToPreviousFT = manager.beginTransaction().remove(this);
        backToPreviousFT.commit();
        manager.popBackStack();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app need the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                //No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                //If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }

                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    protected synchronized void buildGoogleApiClient() {
      /*  mClient=new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        LocationRequest request=LocationRequest.create();
                        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        request.setNumUpdates(1);
                        request.setInterval(0);

                        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                            return;
                        }

                        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                lat= String.valueOf(location.getLatitude());
                                lng= String.valueOf(location.getLongitude());
                                tvLocation.setText("Lat:"+lat+", Lng:"+lng);
                            }
                        });

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();*/

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    private void takePhoto() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getFile();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
            camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }
        startActivityForResult(camera_intent, CAM_REQUEST);

    }

    private File getFile() {
        File folder = new File("sdcard/mycheckins");
        if (!folder.exists()) {
            folder.mkdir();
        }
        return new File(folder, "cam_image" + ts + ".jpg");
    }

    private void datePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        btn_date.setText(month + "/" + dayOfMonth + "/" + year);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAM_REQUEST) {
            String path = "sdcard/mycheckins/cam_image" + ts + ".jpg";
            Glide.with(getContext()).load(path).into(iv_receiptImage);
            imageRes = path;

        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        lat = String.valueOf(location.getLatitude());
        lng = String.valueOf(location.getLongitude());
        tvLocation.setText("Lat:" + lat + ", Lng:" + lng);

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
}
