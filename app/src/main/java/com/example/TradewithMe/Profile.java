package com.example.TradewithMe;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class Profile extends Fragment {


    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CallbackManager callbackManager;

    //For illustrate
    private TextView first_lastname,firstname,lasstname,email,phone_number,edit_profile;
    private DatabaseReference databaseReference,providerefference;
    private String userID;

    private SwipeRefreshLayout swipeRefreshLayout;

    private CircleImageView profileImageView;


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
        phone_number = view.findViewById(R.id.phone_number_text);
        profileImageView = view.findViewById(R.id.profile_image_beforechat);

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
                    String phone_number_fb =userProfile.phone_number;

                    first_lastname.setText(firstname_fb+" "+lastname_fb);
                    firstname.setText("Firstname: "+firstname_fb);
                    lasstname.setText("Lastname: "+lastname_fb);
                    email.setText("Email: "+email_fb);
                    phone_number.setText("Phone number: "+phone_number_fb);


                }
                if (snapshot.hasChild("image"))
                {
                    String image = snapshot.child("image").getValue().toString();
                    Picasso.get().load(image).into(profileImageView);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),"Something wrong happen",Toast.LENGTH_LONG).show();

            }
        });

        edit_profile = view.findViewById(R.id.edit_profile_btn);

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),EditProfile.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.refresh_profile);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

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
                            String phone_number_fb =userProfile.phone_number;

                            first_lastname.setText(firstname_fb+" "+lastname_fb);
                            firstname.setText("Firstname: "+firstname_fb);
                            lasstname.setText("Lastname: "+lastname_fb);
                            email.setText("Email: "+email_fb);
                            phone_number.setText("Phone number: "+phone_number_fb);
                        }
                        if (snapshot.hasChild("image"))
                        {
                            String image = snapshot.child("image").getValue().toString();
                            Picasso.get().load(image).into(profileImageView);
                        }

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(),"Something wrong happen",Toast.LENGTH_LONG).show();

                    }
                });

                swipeRefreshLayout.setRefreshing(false);
            }
        });




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