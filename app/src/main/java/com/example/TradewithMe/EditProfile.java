package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    EditText edit_firstname,edit_lastname,edit_pnumber;
    FirebaseDatabase database  =FirebaseDatabase.getInstance();
    DatabaseReference reference;
    Button update_btn;
    String userID;
    String myUri = "";
    CircleImageView profileImageView;
    Uri imageURI;
    StorageTask uploadTask;
    StorageReference storageProfilePicRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edit_firstname =findViewById(R.id.edit_profile_fireestname);
        edit_lastname = findViewById(R.id.edit_profile_lastname);
        edit_pnumber = findViewById(R.id.edit_profile_pnumber);

        reference = database.getReference("Users");
        userID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(user!=null)
                {
                    String firstname_fb = user.firstname;
                    String lastname_fb =user.lastname;
                    String phone_number_fb =user.phone_number;

                    edit_firstname.setText(firstname_fb);
                    edit_lastname.setText(lastname_fb);
                    if(phone_number_fb.equals("The user have to edit first"))
                    {
                        edit_pnumber.setText("");
                    }
                    else{
                        edit_pnumber.setText(phone_number_fb);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        update_btn = findViewById(R.id.save_profile);
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap map = new HashMap();
                map.put("firstname",edit_firstname.getText().toString().trim());
                map.put("lastname",edit_lastname.getText().toString().trim());
                map.put("phone_number",edit_pnumber.getText().toString().trim());
                reference.child(userID).updateChildren(map);

                updateProfileImage();

                Intent intent = new Intent(EditProfile.this,Profile.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

////                setResult(EditProfile.RESULT_OK);
//                Fragment frg = new FirstFragment();
//                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//                ft.detach(frg);
//                ft.attach(frg);
//                ft.addToBackStack(FirstFragment.class.getSimpleName());
//                ft.commit();

                finish();


            }
        });

        //Upload Profile Picture
        storageProfilePicRef= FirebaseStorage.getInstance().getReference().child("Profile Pic");

        profileImageView = findViewById(R.id.profile_image);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1,1).start(EditProfile.this);

            }
        });

        getUserInfo();

    }

    private void getUserInfo() {

        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()&& snapshot.getChildrenCount()>0)
                {
                    if (snapshot.hasChild("image"))
                    {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageURI = result.getUri();
                profileImageView.setImageURI(imageURI);
            }

        }
        else
        {
            Toast.makeText(this,"Error, Try again",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileImage() {
        final ProgressDialog progressDialog=  new ProgressDialog(this);
        progressDialog.setTitle("Set your profile");
        progressDialog.setMessage("Please wait while we are setting your information");

        if(imageURI !=null)
        {
            final StorageReference fileRef =  storageProfilePicRef.child(userID+".jpg");

            uploadTask = fileRef.putFile(imageURI);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful())
                    {
                        throw  task.getException();
                    }

                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful());
                    {
                        Uri downloadUri = task.getResult();
                        myUri =downloadUri.toString();

                        HashMap<String,Object> userMap = new HashMap<>();
                        userMap.put("image",myUri);

                        reference.child(userID).updateChildren(userMap);

                        progressDialog.dismiss();
                    }
                }
            });
        }
        else{
            progressDialog.dismiss();
            Toast.makeText(this, "Image not selected",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Integer id =item.getItemId();
        if (id == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}