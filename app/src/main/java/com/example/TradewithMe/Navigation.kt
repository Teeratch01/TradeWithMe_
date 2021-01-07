package com.example.TradewithMe

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.selects.select

class Navigation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        val bottomNavigationView :BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navController = findNavController(R.id.fragment)

        bottomNavigationView.setupWithNavController(navController)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.top_navigation , menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent =Intent(this,Direct_Message::class.java)
        when(item.itemId){
            R.id.direct_message ->startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}