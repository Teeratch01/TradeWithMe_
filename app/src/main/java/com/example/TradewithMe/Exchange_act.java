package com.example.TradewithMe;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class Exchange_act extends Fragment {

    TextView current_location;
    EditText amount, rates;
    Button btn_post, btn_search;
    FusedLocationProviderClient fusedLocationProviderClient;
    DatabaseReference databaseReference, listdatabase;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    FirebaseRecyclerOptions options;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    RecyclerView exchange_result;
    String latitude, longitude, amount_get, rates_get;
    long maxid;
    TextView name_surnaame;
    CircleImageView profileImage_exchange;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private ResultReceiver resultReceiver ;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_exchange_act, container, false);
        current_location = view.findViewById(R.id.current_location);


        swipeRefreshLayout = view.findViewById(R.id.refresh_exchange_act);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                resultReceiver= new AddressResultReceiver(new Handler());
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    getCurrentLocation();
                    Log.d("check_in","check");
                }

                swipeRefreshLayout.setRefreshing(false);
            }
        });
        //Find Current Location
//        current_location = view.findViewById(R.id.current_location);
//        resultReceiver= new AddressResultReceiver(new Handler());
//        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
//        } else {
//            getCurrentLocation();
//            Log.d("check_in","check");
//        }
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
//
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)
//        {
//
//            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//                @Override
//                public void onComplete(@NonNull Task<Location> task) {
//                    Location location = task.getResult();
//                    if(location != null)
//                    {
//                        Geocoder geocoder= new Geocoder(getContext(), Locale.getDefault());
//                        try {
//                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
////                            Log.d("Location", addresses.get(0).getAddressLine(0));
//                            latitude = String.valueOf(addresses.get(0).getLatitude());
//                            longitude = String.valueOf(addresses.get(0).getLongitude());
//                            current_location.setText(Html.fromHtml("<b>  Location : </b>" + addresses.get(0).getAddressLine(0)));
//
//                        }
//                        catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//
//        }else
//        {
//            ActivityCompat.requestPermissions(getActivity(),new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION
//            },44);
//        }


        //Post Currency
        Spinner have_currency = view.findViewById(R.id.have_currency);
        ArrayAdapter<CharSequence> my_currency = ArrayAdapter.createFromResource(getContext(), R.array.have_currency, android.R.layout.simple_spinner_item);
        my_currency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        have_currency.setAdapter(my_currency);

        Spinner want_currency = view.findViewById(R.id.want_currency);
        ArrayAdapter<CharSequence> wmy_currency = ArrayAdapter.createFromResource(getContext(), R.array.want_currency, android.R.layout.simple_spinner_item);
        wmy_currency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        want_currency.setAdapter(wmy_currency);

        amount = view.findViewById(R.id.amount);
        rates = view.findViewById(R.id.rates);
        amount_get = amount.getText().toString();
        rates_get = rates.getText().toString();

        btn_post = view.findViewById(R.id.post);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Currency");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    maxid = (int) snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String have_currency_text = have_currency.getSelectedItem().toString();
                String amount_text = amount.getText().toString();
                String want_currency_text = want_currency.getSelectedItem().toString();
                String rates_text = rates.getText().toString();
                String userid = firebaseAuth.getCurrentUser().getUid();
                String combine_currency = have_currency_text + "_" + want_currency_text;

                if (have_currency_text.equals(want_currency_text)) {
                    Toast.makeText(getActivity(), "Plesee selected have currency and want currency diffrent", Toast.LENGTH_SHORT).show();
                } else {
                    if (amount_text.isEmpty()) {
                        amount.setError("Please specific your amount");
                        amount.requestFocus();
                        return;
                    } else if (rates_text.isEmpty()) {
                        rates.setError("Please specific your rates");
                        rates.requestFocus();
                        return;
                    } else if (latitude == null && longitude == null) {
                        Toast.makeText(getActivity(), "Plesee specific your locaiton", Toast.LENGTH_SHORT).show();
                    } else {

                        ExchangeData information = new ExchangeData(
                                have_currency_text,
                                amount_text,
                                want_currency_text,
                                rates_text,
                                userid,
                                latitude,
                                longitude,
                                combine_currency
                        );


                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String key = null;
                                    for (DataSnapshot data : snapshot.getChildren()) {
                                        if (data.child("uid").getValue().equals(userid) && data.child("combine_currency").getValue().equals(combine_currency)) {
                                            key = data.getKey();
                                        }
                                    }
                                    if (key != null) {
                                        HashMap<String, Object> currencyMap = new HashMap<>();
                                        currencyMap.put("amount", amount_text);
                                        currencyMap.put("rates", rates_text);
                                        currencyMap.put("latitude", latitude);
                                        currencyMap.put("longitude", longitude);
                                        databaseReference.child(key).updateChildren(currencyMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                                                alert.setCancelable(true);
                                                alert.setTitle("Notification");
                                                alert.setMessage("Update Post Successfully!");

                                                alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });

                                                alert.show();
                                            }
                                        });
                                    } else if (key == null) {
                                        FirebaseDatabase.getInstance().getReference("Currency").child(String.valueOf(maxid + 1)).setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                                                alert.setCancelable(true);
                                                alert.setTitle("Notification");
                                                alert.setMessage("Post Successfully!");

                                                alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });

                                                alert.show();
                                            }
                                        });
                                    }
                                } else {
                                    FirebaseDatabase.getInstance().getReference("Currency").child(String.valueOf(maxid + 1)).setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                                            alert.setCancelable(true);
                                            alert.setTitle("Notification");
                                            alert.setMessage("Post Successfully!");

                                            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });

                                            alert.show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                }
            }
        });


        //Search
        btn_search = view.findViewById(R.id.search_btn);
        exchange_result = view.findViewById(R.id.exchange_result);
//        exchange_result.setHasFixedSize(true);
        exchange_result.setLayoutManager(new LinearLayoutManager(getContext()));
        listdatabase = FirebaseDatabase.getInstance().getReference("Currency");

        name_surnaame = view.findViewById(R.id.name_surname);
        profileImage_exchange = view.findViewById(R.id.profile_image_beforechat);


        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String have_currency_text_search = have_currency.getSelectedItem().toString();
                String want_currency_text_search = want_currency.getSelectedItem().toString();
                String combine_currency_text_seaarch = want_currency_text_search + "_" + have_currency_text_search;
                String amount_text = amount.getText().toString();
                String rates_text = rates.getText().toString();

                if (have_currency_text_search.equals(want_currency_text_search)) {
                    Toast.makeText(getActivity(), "Plesee selected have currency and want currency diffrent", Toast.LENGTH_SHORT).show();
                } else {
                    if (amount_text.isEmpty()) {
                        amount.setError("Please specific your amount");
                        amount.requestFocus();
                        return;
                    } else if (rates_text.isEmpty()) {
                        rates.setError("Please specific your rates");
                        rates.requestFocus();
                        return;
                    } else if (latitude == null && longitude == null) {
                        Toast.makeText(getActivity(), "Plesee specific your locaiton", Toast.LENGTH_SHORT).show();
                    } else {
                        firebaseExcahngeSearch(combine_currency_text_seaarch);
                    }
                }

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        resultReceiver= new AddressResultReceiver(new Handler());
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            getCurrentLocation();
            Log.d("check_in","check");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Log.d("latitude","checck");

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(locationRequest, new LocationCallback(){

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    LocationServices.getFusedLocationProviderClient(getActivity()).removeLocationUpdates(this);
                    if (locationResult!=null &&locationResult.getLocations().size()>0)
                    {
                        int latestLocationIndex = locationResult.getLocations().size()-1;
                        double latitude_value = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                        double longitude_value = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                        latitude = String.valueOf(latitude_value);
                        longitude = String.valueOf(longitude_value);

                        Log.d("latitude",latitude);

                        Location location = new Location("providerNA");
                        location.setLatitude(latitude_value);
                        location.setLongitude(longitude_value);
                        fetchAddressfromLatLong(location);

                    }
                }

            },Looper.getMainLooper());
        }


    }

    private void fetchAddressfromLatLong(Location location)
    {
        Intent intent = new Intent(getContext(),FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER,resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA,location);
        Log.d("check","location");
        getActivity().startService(intent);
    }


    private class AddressResultReceiver extends ResultReceiver
    {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Constants.SUCCESS_RESULT)
            {
                Log.d("check_lo",resultData.getString(Constants.RESULT_DATA_KEY));
                current_location.setText("Location :  "+resultData.getString(Constants.RESULT_DATA_KEY));
            }
            else {
                Toast.makeText(getContext(),resultData.getString(Constants.RESULT_DATA_KEY), Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void firebaseExcahngeSearch(String combine_currency) {
//
        Query firebasesearchquerry = databaseReference.orderByChild("combine_currency").startAt(combine_currency).endAt(combine_currency);

        options = new FirebaseRecyclerOptions.Builder<ExchangeData>().setQuery(firebasesearchquerry,ExchangeData.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ExchangeData, ExchangeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ExchangeViewHolder holder, int position, @NonNull ExchangeData model) {

                Double latitude_check= Double.valueOf(model.getLatitude());
                Double longitude_check= Double.valueOf(model.getLongitude());
                Integer amount_check = Integer.parseInt(model.getAmount());
                Integer rate_check = Integer.parseInt(model.getRates());
                String uid_check = model.getUid();
                String uid_get = firebaseAuth.getInstance().getCurrentUser().getUid();
                Integer amount_int = Integer.valueOf(amount.getText().toString());
                Integer rate_int = Integer.valueOf(rates.getText().toString());


                if (latitude_check <= Double.valueOf(latitude)+0.1 && latitude_check>=Double.valueOf(latitude)-0.1 ){
                    if (longitude_check <= Double.valueOf(longitude)+0.1 && longitude_check>=Double.valueOf(longitude)-0.1 )
                    {
                        if (amount_check <= amount_int && rate_check <= rate_int)
                        {
                            if (!uid_check.equals(uid_get))
                            {
                                holder.setDetail(model.getAmount(),model.getRates(),model.getUid());
                                holder.itemView.setVisibility(View.VISIBLE);
                                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            }
                            else
                            {
                                holder.itemView.setVisibility(View.GONE);
                                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                            }
                        }
                        else
                        {
                            holder.itemView.setVisibility(View.GONE);
                            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                        }

                    }
                    else {
                        holder.itemView.setVisibility(View.GONE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    }
                }
                else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.child(uid_check).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User userProfile =  snapshot.getValue(User.class);
                        if (userProfile!=null)
                        {
                            String firstname_fb = userProfile.firstname;
                            String lastname_fb = userProfile.lastname;


                            holder.setProfile(firstname_fb+" "+lastname_fb);

                        }
                        if (snapshot.hasChild("image"))
                        {
                            String image = snapshot.child("image").getValue().toString();
                            holder.setPic(image);
//                            Picasso.get().load(image).into(profileImage_exchange);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Onclick of RecyclerView
                final String user_id_clickable = getRef(position).getKey();
                DatabaseReference currency_bfchat = FirebaseDatabase.getInstance().getReference("Currency").child(user_id_clickable);

                currency_bfchat.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       String user_id_bfchat=snapshot.child("uid").getValue().toString();

                        reference.child(user_id_bfchat).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists())
                                {
                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            String name = "",email = "",phone_number="",image="";

                                            User userProfile =  snapshot.getValue(User.class);
                                            if (userProfile!=null)
                                            {
                                                String firstname_fb = userProfile.firstname;
                                                String lastname_fb = userProfile.lastname;

                                                name =firstname_fb+" "+lastname_fb;
                                                email = userProfile.getEmail();
                                                phone_number = userProfile.getPhone_number();

                                            }
                                            if (snapshot.hasChild("image"))
                                            {
                                                image = snapshot.child("image").getValue().toString();
                                            }




                                            Intent chatIntent = new Intent(getContext(),Profile_rating.class);
                                            chatIntent.putExtra("name_bf_chat",name);
                                            chatIntent.putExtra("email_for_chat",email);
                                            chatIntent.putExtra("phnumber_for_chat",phone_number);
                                            chatIntent.putExtra("image_for_chat",image);
                                            chatIntent.putExtra("userID_exchanger",user_id_bfchat);
                                            startActivity(chatIntent);

                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public ExchangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exchange_list,parent,false);

                return new ExchangeViewHolder(view);
            }
            
        };
        firebaseRecyclerAdapter.startListening();

        firebaseRecyclerAdapter.notifyDataSetChanged();

        exchange_result.setAdapter(firebaseRecyclerAdapter);

        exchange_result.smoothScrollToPosition(exchange_result.getAdapter().getItemCount());



    }


    public class ExchangeViewHolder extends RecyclerView.ViewHolder{

        View mview;

        public ExchangeViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
        }

        public void setDetail(String amountill ,String rateill ,String uid_check){
            TextView amount = mview.findViewById(R.id.amount_ill);
            TextView rates = mview.findViewById(R.id.rate_ill);
            name_surnaame = mview.findViewById(R.id.name_surname);
            profileImage_exchange = mview.findViewById(R.id.profile_image_beforechat);


            amount.setText(amountill);
            rates.setText(rateill);


        }

        public void setProfile(String name_surname_ill ){
            name_surnaame = mview.findViewById(R.id.name_surname);

            name_surnaame.setText(name_surname_ill);

        }

        public void setPic(String profile_image_ill){
            profileImage_exchange = mview.findViewById(R.id.profile_image_list_bfchat);

            Picasso.get().load(profile_image_ill).into(profileImage_exchange);

        }


        public void donoting()
        {

        }
    }



}