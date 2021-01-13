package com.example.TradewithMe;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;


public class Profile extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =inflater.inflate(R.layout.fragment_profile, container, false);
        Button btnlogout = view.findViewById(R.id.logout);

        btnlogout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
                Intent intoLogin =new Intent(getActivity(),Login.class);
                startActivity(intoLogin);
            }

        });
        return view;

    }
}