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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class Exchange_act extends Fragment {

    TextView current_location;
    EditText amount,rates;
    Button btn_post;
    FusedLocationProviderClient fusedLocationProviderClient;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;

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
                            current_location.setText(Html.fromHtml("<b>  Address : </b>" + addresses.get(0).getAddressLine(0)));

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


        //Post Currency
        Spinner have_currency = view.findViewById(R.id.have_currency);
        ArrayAdapter<CharSequence> my_currency = ArrayAdapter.createFromResource(getContext(),R.array.have_currency, android.R.layout.simple_spinner_item);
        my_currency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        have_currency.setAdapter(my_currency);

        Spinner want_currency = view.findViewById(R.id.want_currency);
        ArrayAdapter<CharSequence> wmy_currency = ArrayAdapter.createFromResource(getContext(),R.array.want_currency, android.R.layout.simple_spinner_item);
        wmy_currency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        want_currency.setAdapter(wmy_currency);

        amount = view.findViewById(R.id.amount);
        rates = view.findViewById(R.id.rates);

        btn_post = view.findViewById(R.id.post);

        databaseReference = FirebaseDatabase.getInstance().getReference("Currency");
        firebaseAuth = FirebaseAuth.getInstance();

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String have_currency_text = have_currency.getSelectedItem().toString();
                String amount_text = amount.getText().toString();
                String want_currency_text = want_currency.getSelectedItem().toString();
                String rates_text = rates.getText().toString();

                ExchangeData information = new ExchangeData(
                  have_currency_text,
                  amount_text,
                  want_currency_text,
                  rates_text
                );

                FirebaseDatabase.getInstance().getReference("Currency").child(firebaseAuth.getCurrentUser().getUid()).setValue(information);

            }
        });






        return view;
    }



}