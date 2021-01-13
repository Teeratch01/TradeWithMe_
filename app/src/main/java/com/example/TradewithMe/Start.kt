package com.example.TradewithMe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Start : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val button = findViewById<Button>(R.id.btn)
        button.setOnClickListener {
            val intent = Intent(this, Login::class.java)

            startActivity(intent)
        }
    }
}