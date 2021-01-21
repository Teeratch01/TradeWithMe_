package com.example.TradewithMe;

import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.se.omapi.Session;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Profile extends Fragment {


    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CallbackManager callbackManager;

    //For illustrate
    private TextView first_lastname,firstname,lasstname,email;
    private DatabaseReference databaseReference,providerefference;
    private String userID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =inflater.inflate(R.layout.fragment_profile, container, false);
//        FacebookSdk.getApplicationContext();
//        AppEventsLogger.activateApp(getActivity());
        Button btnlogout = view.findViewById(R.id.logout);

        btnlogout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){

                FirebaseAuth.getInstance().signOut();

                LoginManager.getInstance().logOut();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                GoogleSignInClient googleSignInClient=GoogleSignIn.getClient(getContext(),gso);
                googleSignInClient.signOut();

                Intent intoLogin =new Intent(getActivity(),Login.class);
                startActivity(intoLogin);

            }

        });

        //Illustrate user info from firebase
        first_lastname = view.findViewById(R.id.first_lastname);
        firstname = view.findViewById(R.id.firstname_text);
        lasstname = view.findViewById(R.id.lastname_text);
        email = view.findViewById(R.id.email_text);

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user !=null)
//        {
//            String name;
//            String email;
//            for (UserInfo profile: user.getProviderData())
//            {
//                String name = profile.getDisplayName();
//                String email = profile.getEmail();
//
//            }
//
//
//        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();
        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userProfile =  snapshot.getValue(User.class);

                if (userProfile!=null)
                {
                    String firstname_fb = userProfile.firstname;
                    String lastname_fb = userProfile.lastname;
                    String email_fb = userProfile.email;

                    first_lastname.setText(firstname_fb+" "+lastname_fb);
                    firstname.setText("Firstname: "+firstname_fb);
                    lasstname.setText("Lastname: "+lastname_fb);
                    email.setText("Email: "+email_fb);

                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),"Something wrong happen",Toast.LENGTH_LONG).show();

            }
        });

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user!=null)
//        {
//            for (UserInfo profile : user.getProviderData()) {
//                // Id of the provider (ex: google.com)
//                String providerId = profile.getProviderId();
//                String uid = profile.getUid();
//                String name = profile.getDisplayName();
//                String email = profile.getEmail();
//
//                Log.d("userid",uid);
//                Log.d("name",name);
//                Log.d("email",email);
//
//            };
//        }




        return view;

    }

//    public void disconnectFromFacebook() {
//
//        if (AccessToken.getCurrentAccessToken() == null) {
//            return; // already logged out
//        }
//
//        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
//                .Callback() {
//            @Override
//            public void onCompleted(GraphResponse graphResponse) {
//
//                LoginManager.getInstance().logOut();
//
//            }
//        }).executeAsync();
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

    }
}