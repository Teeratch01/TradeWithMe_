package com.example.TradewithMe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.TradewithMe.Navigation
import com.example.TradewithMe.R
import com.google.firebase.auth.FirebaseAuth

// https://www.youtube.com/watch?v=S-Mr-CcdU08&t=159s for Facebook authen
class Login : AppCompatActivity() {
    //    var firebaseAuth: FirebaseAuth? = null
//    var callbackManager: CallbackManager? = null
    var editTextTextEmailAddress: EditText? = null
    var editTextTextPassword: EditText? = null
    var button2: Button? = null
    var mFirebaseAuth: FirebaseAuth? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        this.mFirebaseAuth = FirebaseAuth.getInstance()
        this.editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress)
        this.editTextTextPassword = findViewById(R.id.editTextTextPassword)
        this.button2 = findViewById(R.id.button2)

        this.mAuthStateListener = FirebaseAuth.AuthStateListener {
            val mFirebaseUser = mFirebaseAuth!!.currentUser
            if (mFirebaseUser != null) {
                Toast.makeText(this@Login, "Welcome to Trade With Me application", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Login, Navigation::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this@Login, "Please Login", Toast.LENGTH_SHORT).show()
            }
        }



            button2?.setOnClickListener(View.OnClickListener {
                val email = editTextTextEmailAddress?.getText().toString()
                val pwd = editTextTextPassword?.getText().toString()
                if (email.isEmpty()) {
                    editTextTextEmailAddress?.setError("Please enter your email")
                    editTextTextEmailAddress?.requestFocus()
                } else if (pwd.isEmpty()) {
                    editTextTextPassword?.setError("Please enter your password")
                    editTextTextPassword?.requestFocus()
                } else if (email.isEmpty() && pwd.isEmpty()) {
                    Toast.makeText(this@Login, "Please fill in the blank", Toast.LENGTH_SHORT).show()
                }

                /* check email and password are correct that you register in Firebase or not*/
                else if (!(email.isEmpty() && pwd.isEmpty())) {
                    mFirebaseAuth!!.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(this@Login) { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(this@Login, "Log In Error, Please try Again", Toast.LENGTH_SHORT).show()
                        } else {
                            val intToDashboard = Intent(this@Login, Navigation::class.java)
                            startActivity(intToDashboard)
                        }
                    }
                } else {
                    Toast.makeText(this@Login, "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })

    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth!!.addAuthStateListener(mAuthStateListener!!)
    }
}





