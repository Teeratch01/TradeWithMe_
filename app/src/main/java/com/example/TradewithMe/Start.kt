package com.example.TradewithMe

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener


class Start : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null
    var mAuthListener: AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val button = findViewById<Button>(R.id.btn)
        button.setOnClickListener {
            val intent = Intent(this, Login::class.java)

            startActivity(intent)
        }

        var ShowDialog = intent.getBooleanExtra("showDialog", false)

        if (ShowDialog)
        {
            val alert = AlertDialog.Builder(this@Start)
            alert.setCancelable(true)
            alert.setTitle("Notification")
            alert.setMessage("You log in on another device")

            alert.setNegativeButton("Dismiss") { dialog, which -> dialog.cancel() }

            alert.show()
            ShowDialog = false
        }
        Log.d("check dialo", ShowDialog.toString())


        firebaseAuth = FirebaseAuth.getInstance()

        mAuthListener = AuthStateListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Log.d("check_login","login")
            }
        }



//        val intent = Intent()
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        setResult(Activity.RESULT_OK, intent)
//        finish()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        Log.d("check_request_code", requestCode.toString())
//
//        if (requestCode ==2)
//        {
//            val alert = AlertDialog.Builder(this@Start)
//            alert.setCancelable(true)
//            alert.setTitle("Notification")
//            alert.setMessage("You log in on another device")
//
//            alert.setNegativeButton("Dismiss") { dialog, which -> dialog.cancel() }
//
//            alert.show()
//
//        }
//    }
}