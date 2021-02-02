package com.example.TradewithMe;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class Exchange_act extends Fragment {

    TextView current_location;
    EditText amount,rates;
    Button btn_post,btn_search;
    FusedLocationProviderClient fusedLocationProviderClient;
    DatabaseReference databaseReference,listdatabase;
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;
    FirebaseRecyclerOptions options;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    RecyclerView exchange_result;
    String latitude,longitude,amount_get,rates_get;
    long maxid;
    TextView name_surnaame;
    CircleImageView profileImage_exchange;

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
//                            Log.d("Location", addresses.get(0).getAddressLine(0));
                            latitude = String.valueOf(addresses.get(0).getLatitude());
                            longitude = String.valueOf(addresses.get(0).getLongitude());
                            current_location.setText(Html.fromHtml("<b>  Location : </b>" + addresses.get(0).getAddressLine(0)));

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
        amount_get = amount.getText().toString();
        rates_get = rates.getText().toString();

        btn_post = view.findViewById(R.id.post);


        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Currency");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
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
                String combine_currency = have_currency_text+"_"+want_currency_text;

                if(have_currency_text.equals(want_currency_text))
                {
                    Toast.makeText(getActivity(), "Plesee selected have currency and want currency diffrent", Toast.LENGTH_SHORT).show();
                }
                else{
                    if (amount_text.isEmpty())
                    {
                        amount.setError("Please specific your amount");
                        amount.requestFocus();
                        return;
                    }
                    else if (rates_text.isEmpty())
                    {
                        rates.setError("Please specific your rates");
                        rates.requestFocus();
                        return;
                    }
                    else if (latitude == null && longitude ==null)
                    {
                        Toast.makeText(getActivity(), "Plesee specific your locaiton", Toast.LENGTH_SHORT).show();
                    }
                    else{
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

                        FirebaseDatabase.getInstance().getReference("Currency").child(String.valueOf(maxid+1)).setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                                alert.setCancelable(true);
                                alert.setTitle("Notification");
                                alert.setMessage("Your transaction was succesfully");

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
                String combine_currency_text_seaarch = want_currency_text_search+"_"+have_currency_text_search;
                String amount_text = amount.getText().toString();
                String rates_text = rates.getText().toString();

                if(have_currency_text_search.equals(want_currency_text_search))
                {
                    Toast.makeText(getActivity(), "Plesee selected have currency and want currency diffrent", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (amount_text.isEmpty())
                    {
                        amount.setError("Please specific your amount");
                        amount.requestFocus();
                        return;
                    }
                    else if (rates_text.isEmpty())
                    {
                        rates.setError("Please specific your rates");
                        rates.requestFocus();
                        return;
                    }
                    else if (latitude == null && longitude ==null)
                    {
                        Toast.makeText(getActivity(), "Plesee specific your locaiton", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        firebaseExcahngeSearch(combine_currency_text_seaarch);
                    }
                }

            }
        });

        return view;
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

        exchange_result.setAdapter(firebaseRecyclerAdapter);


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