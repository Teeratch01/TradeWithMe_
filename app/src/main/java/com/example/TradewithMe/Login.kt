package com.example.TradewithMe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*


// https://www.youtube.com/watch?v=S-Mr-CcdU08&t=159s for Facebook authen
class Login : AppCompatActivity() {
    //    var firebaseAuth: FirebaseAuth? = null
//    var callbackManager: CallbackManager? = null
    var editTextTextEmailAddress: EditText? = null
    var editTextTextPassword: EditText? = null
    var button2: Button? = null
    var mFirebaseAuth: FirebaseAuth? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private var mAuthStateGoogle: FirebaseAuth.AuthStateListener?= null
    private var mAuthStateFacebook :FirebaseAuth.AuthStateListener?=null
    private lateinit var facebook_login :ImageView
    private var callbackManager: CallbackManager? = null
    private var loginButton: LoginButton? = null
    private val auth = FirebaseAuth.getInstance()
    lateinit var signup_btn: TextView
    private var user_info:User?= null
    private var user_faccebook: User? =null


    //Google login Variable
    var signin: SignInButton? = null
    var gso: GoogleSignInOptions? = null
    var signInClient: GoogleSignInClient? = null
    var firebaseAuth: FirebaseAuth? = null

    lateinit var databaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        this.mFirebaseAuth = FirebaseAuth.getInstance()
        this.editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress)
        this.editTextTextPassword = findViewById(R.id.editTextTextPassword)
        this.button2 = findViewById(R.id.button2)

        signup_btn = findViewById(R.id.signup_btn)
        signup_btn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        })

        val forgotPassword_btn :TextView = findViewById(R.id.forgot_password)
       forgotPassword_btn.setOnClickListener(View.OnClickListener {
           val goto_forgotpassword = Intent(this,ForgotPassword::class.java)
           startActivity(goto_forgotpassword)
       })


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
                mFirebaseAuth?.signInWithEmailAndPassword(email, pwd)
                        ?.addOnCompleteListener(this@Login) { task ->
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

        //Facebook Login
        OnClickButtonListtener()

        //Google Login Part
        signin = findViewById(R.id.google_signin)
        firebaseAuth = FirebaseAuth.getInstance()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("122025052780-bgfk34p2hlfs5gvvfov1olsnlmirh4cf.apps.googleusercontent.com")
            .requestEmail().build()
        signInClient = GoogleSignIn.getClient(this, gso!!)
        val signInAccount = GoogleSignIn.getLastSignedInAccount(this)
        Log.d("check", signInAccount.toString())
        if (signInAccount != null) {

//            val firstname = signInAccount.givenName
//            val lastname = signInAccount.familyName
//            val email = signInAccount.email
//
//            user_info = User(
//                    firstname, lastname, email
//            )

            startActivity(Intent(this, Navigation::class.java))

        }

        signin!!.setOnClickListener(View.OnClickListener {
            val sign = signInClient!!.getSignInIntent()
            startActivityForResult(sign, GOOGLE_SIGN_IN_CODE)
//            val user_id= mFirebaseAuth?.currentUser?.uid
//            FirebaseDatabase.getInstance().getReference("Users").child(user_id.toString()).setValue(user_google)
        })

        this.mAuthStateListener = AuthStateListener {
            val mFirebaseUser = mFirebaseAuth?.currentUser
            if (mFirebaseUser != null) {
                val user_id = mFirebaseUser.uid
                Toast.makeText(this@Login, "Welcome to Trade With Me application", Toast.LENGTH_SHORT).show()
                if (user_info!=null)
                {
                    FirebaseDatabase.getInstance().getReference("Users").child(user_id).setValue(user_info)
                }
//                FirebaseDatabase.getInstance().getReference("Users").child(user_id).setValue(user_google)
                val intent = Intent(this@Login, Navigation::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this@Login, "Please Login", Toast.LENGTH_SHORT).show()
            }
        }
    }



    fun OnClickButtonListtener(){
        callbackManager = CallbackManager.Factory.create()
        loginButton = findViewById(R.id.login_button)
        loginButton?.setPermissions(Arrays.asList(EMAIL))
        loginButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
                loadUserProfile(loginResult.accessToken)
            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                Log.d(TAG, "on error" + exception.message)
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        //Google Login Part
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            val signInTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val signInAccount = signInTask.getResult(ApiException::class.java)
                val authCredential = GoogleAuthProvider.getCredential(signInAccount!!.idToken, null)
                firebaseAuth!!.signInWithCredential(authCredential).addOnCompleteListener {
                    val firstname: String? = signInAccount.givenName
                    val lastname: String? = signInAccount.familyName
                    val email: String? = signInAccount.email
                    user_info = User(
                            firstname, lastname, email
                    )
                    Log.d("userinfo", user_info.toString())
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().currentUser?.uid.toString()).setValue(user_info)
                    Toast.makeText(applicationContext, "Your Google Account is Connected to our Application", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, Navigation::class.java))
                }.addOnFailureListener { }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }

    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    //                            FirebaseUser user = auth.getCurrentUser();
                    openProfile()
//                    loadUserProfile(token)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                }

                // ...
            }
    }

    private fun openProfile() {
        startActivity(Intent(this, Navigation::class.java))
        finish()
    }

    //Input Facebook information
    private fun loadUserProfile(newAccessToken: AccessToken)
    {
        val request : GraphRequest = GraphRequest.newMeRequest(newAccessToken, GraphRequest.GraphJSONObjectCallback { `object`, response ->

            val name: String = `object`.getString("name")
            val firstname = name.split(" ")[0]
            val lastname = name.split(" ")[1]
            val email: String = `object`.getString("email")
            val uid = `object`.getString("id")


            user_info = User(
                    firstname, lastname, email
            )

//            val auth :FirebaseAuth = FirebaseAuth.getInstance()
//            if (auth.currentUser!=null)
//            {
//                val userid = auth.currentUser!!.uid
//                FirebaseDatabase.getInstance().getReference("Users").child(userid).setValue(user_info)
//            }


//            mAuthStateFacebook = AuthStateListener { firebaseAuth ->
//                val user = firebaseAuth.currentUser
//                if (user != null) {
//                    val user_id = user.uid
////                    Log.d("user_id",user_id)
//                    FirebaseDatabase.getInstance().getReference("Users").child(user_id).setValue(user_info)
//                }
//            }

//            FirebaseAuth.getInstance().addAuthStateListener(mAuthStateFacebook!!)
//
////
//            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateFacebook!!)


        })

        val parameters:Bundle = Bundle()
        parameters.putString("fields", "name,email,id,picture.type(large)")
        request.parameters = parameters
        request.executeAsync()

    }

    companion object {
        private const val TAG = "FBAUTH"
        private const val EMAIL = "email"
        const val GOOGLE_SIGN_IN_CODE = 0
    }
    override fun onStart() {
        super.onStart()

        mFirebaseAuth?.addAuthStateListener(mAuthStateListener!!)
        if (auth.currentUser != null) {
            openProfile()
        }
//        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateFacebook!!)
    }

    override fun onStop() {
        super.onStop()
        if (mAuthStateListener !=null)
        {
            mFirebaseAuth?.removeAuthStateListener(mAuthStateListener!!)
        }
//        if (mAuthStateFacebook!=null)
//        {
//            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateFacebook!!)
//        }


    }
}





