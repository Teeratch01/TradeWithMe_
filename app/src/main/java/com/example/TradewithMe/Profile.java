package com.example.TradewithMe;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class Profile extends Fragment {


    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private CallbackManager callbackManager;

    //For illustrate
    private TextView first_lastname,firstname,lasstname,email,phone_number,edit_profile,rating_number,feedback_datail,report_issue;
    private DatabaseReference databaseReference,providerefference,issue_ref;
    private String userID;

    private FirebaseAuth firebaseAuth;

    private SwipeRefreshLayout swipeRefreshLayout;

    private CircleImageView profileImageView;

    private RatingBar ratingBar;

    private Dialog issue_dialog;

    private EditText issue_report;

    private Button submit_issue,close_issue;

    private  long maxid;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view =inflater.inflate(R.layout.fragment_profile, container, false);
//        FacebookSdk.getApplicationContext();
//        AppEventsLogger.activateApp(getActivity());

        firebaseAuth = FirebaseAuth.getInstance();
        String current_userID =  firebaseAuth.getCurrentUser().getUid();

        Log.d("Check_Uid",current_userID);
        Button btnlogout = view.findViewById(R.id.logout);



        btnlogout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){



//                FirebaseDatabase.getInstance().getReference("Users").child(current_userID).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.hasChild("token"))
//                        {
//                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("token").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

                HashMap map = new HashMap();
                map.put("token","");
                FirebaseDatabase.getInstance().getReference("Users").child(current_userID).updateChildren(map);

                FirebaseAuth.getInstance().signOut();




                Log.d("check,ch","check");

                LoginManager.getInstance().logOut();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
                GoogleSignInClient googleSignInClient=GoogleSignIn.getClient(getContext(),gso);
                googleSignInClient.signOut();

                Intent intoLogin =new Intent(getActivity(),Start.class);
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

                    if (phone_number_fb.equals("The user have to edit first"))
                    {
                        phone_number.setText(Html.fromHtml("Phone number : <font color='#FF0000'>"+phone_number_fb+"</font>"));
                    }
                    else
                    {
                        phone_number.setText("Phone number: "+phone_number_fb);
                    }



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

                            if (phone_number_fb.equals("The user have to edit first"))
                            {
                                phone_number.setText(Html.fromHtml("Phone number : <font color='#FF0000'>"+phone_number_fb+"</font>"));
                            }
                            else
                            {
                                phone_number.setText("Phone number: "+phone_number_fb);
                            }


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

        ratingBar = view.findViewById(R.id.profile_rating);
        rating_number = view.findViewById(R.id.rating_number_text);
        databaseReference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("ratings"))
                {
                    Float current_user_rating = Float.valueOf(snapshot.child("ratings").getValue().toString());
//                    ratingBar.setMax(5);
                    ratingBar.setRating(current_user_rating);
                    rating_number.setText(String.valueOf(current_user_rating));
                }
                else
                {
                    rating_number.setText("No Rating Yet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        feedback_datail = view.findViewById(R.id.feedback_detail);
        feedback_datail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(),Feedback.class);
                intent.putExtra("feedback_id",current_userID);
                startActivity(intent);
            }
        });

        report_issue = view.findViewById(R.id.report_issue_problem);
        issue_ref = FirebaseDatabase.getInstance().getReference("Issue");

        issue_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    maxid = snapshot.getChildrenCount();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        report_issue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                issue_dialog = new Dialog(getActivity());
                issue_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                issue_dialog.setContentView(R.layout.report_issue_dialog);
                issue_dialog.setCancelable(false);

                issue_report = issue_dialog.findViewById(R.id.issue_report);


                submit_issue = issue_dialog.findViewById(R.id.submit_issue);
                close_issue = issue_dialog.findViewById(R.id.close_isuue);

                submit_issue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String issue_text = issue_report.getText().toString();
                        if (issue_text.isEmpty())
                        {
                            issue_report.setError("Please specify your problem");
                            issue_report.requestFocus();
                            return;
                        }
                        else if (! issue_text.isEmpty()){

                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            String time = sdf.format(new Date());

                            SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
                            String date_for = date.format(new Date());

                            Issue_report issue_info = new Issue_report(
                                    current_userID,
                                    issue_text,
                                    date_for,
                                    time
                            );

                           issue_ref.child(String.valueOf(maxid+1)).setValue(issue_info).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   issue_dialog.dismiss();
                               }
                           });

                        }
                    }
                });

                close_issue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        issue_dialog.dismiss();
                    }
                });

                issue_dialog.show();

            }

        });

        return view;

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


}