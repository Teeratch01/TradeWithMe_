package com.example.TradewithMe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class Exchange_act extends Fragment {

    TextView current_location;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =inflater.inflate(R.layout.fragment_exchange_act, container, false);

        //Find Current Location
        current_location = view.findViewById(R.id.current_location);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if(location != null)
                    {
                        Geocoder geocoder= new Geocoder(getContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                            Log.d("Location", addresses.get(0).getAddressLine(0));
                            current_location.setText(Html.fromHtml("<font color='#6200EE'><b> Address : </b><br></font>" + addresses.get(0).getAddressLine(0)));

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }else
        {
            ActivityCompat.requestPermissions(getActivity(),new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },44);
        }

        return view;
    }



}