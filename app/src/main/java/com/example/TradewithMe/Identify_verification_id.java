package com.example.TradewithMe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class Identify_verification_id extends AppCompatActivity {
    private ImageView upload_id,upload_yourself;
    private Uri imageUri,second_imageUri;
    private Button next_btn;
    private StorageReference storageIdRefference_id,storageIdRefference_yourself;
    private String userID,ID_uri="",User_uri="";
    private StorageTask uploadId,uploadUser;
    private DatabaseReference verification_ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_verification_id);

        upload_id = findViewById(R.id.upload_id);
        upload_yourself = findViewById(R.id.upload_id_with_user);
        next_btn = findViewById(R.id.next_id_verification);
        storageIdRefference_id = FirebaseStorage.getInstance().getReference().child("Identity Verification").child("ID card");
        storageIdRefference_yourself = FirebaseStorage.getInstance().getReference().child("Identity Verification").child("User with ID card");
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        verification_ref = FirebaseDatabase.getInstance().getReference("Identity Verification");

        upload_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent  = CropImage.activity(imageUri).getIntent(getApplicationContext());
                startActivityForResult(intent,400);
            }
        });

        upload_yourself.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = CropImage.activity(second_imageUri).getIntent(getApplicationContext());
                startActivityForResult(intent,500);

            }
        });
        
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImageID();
//                Intent intent = new Intent(Identify_verification_id.this,Identity_verification_info.class);
//                startActivity(intent);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 400)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                upload_id.setImageURI(imageUri);
            }
        }
        else if (requestCode == 500)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                second_imageUri = result.getUri();
                upload_yourself.setImageURI(second_imageUri);
            }
        }
        else
        {
            Toast.makeText(this,"Error, Try again",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImageID() {
        final ProgressDialog progressDialog=  new ProgressDialog(this);
        progressDialog.setTitle("Set up your ID verification");
        progressDialog.setMessage("Please wait while we are setting your information");

        if (imageUri != null && second_imageUri!=null)
        {
            final StorageReference IDref = storageIdRefference_id.child(userID+".jpg");
            final StorageReference UserRef = storageIdRefference_yourself.child(userID+".jpg");

            uploadId = IDref.putFile(imageUri);
            uploadId.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw  task.getException();
                    }

                    return IDref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri = task.getResult();
                        ID_uri = downloadUri.toString();

                        HashMap<String,Object> userMap = new HashMap<>();
                        userMap.put("IDcard",ID_uri);

                        verification_ref.child(userID).child("ID card").updateChildren(userMap);
                    }
                }
            });

            uploadUser = UserRef.putFile(second_imageUri);
            uploadUser.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw  task.getException();
                    }

                    return UserRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri = task.getResult();
                        User_uri = downloadUri.toString();

                        HashMap<String,Object> userMap = new HashMap<>();
                        userMap.put("IDcard_with_User",User_uri);

                        verification_ref.child(userID).child("ID card with User").updateChildren(userMap);

                        progressDialog.dismiss();

                        Intent intent = new Intent(Identify_verification_id.this,Identity_verification_info.class);
                        startActivity(intent);
                    }
                }
            });
        }
        else{
            progressDialog.dismiss();
            Toast.makeText(this, "Image not selected",Toast.LENGTH_SHORT).show();
        }
    }

    
}