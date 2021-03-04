package com.example.TradewithMe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class Illustrate_pic extends AppCompatActivity {
    String image_ill;
    ImageView imageView;
    ImageButton close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illustrate_pic);
        image_ill = getIntent().getExtras().get("pic_illustrate").toString();
        imageView = findViewById(R.id.illustrate_image);
        close = findViewById(R.id.close_image);

        if (image_ill != null)
        {
            Picasso.get().load(image_ill).into(imageView);
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }
}