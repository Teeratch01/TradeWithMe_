package com.example.TradewithMe

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
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
    private lateinit var user_ref : DatabaseReference


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
        user_ref = FirebaseDatabase.getInstance().getReference("Users")
        var loadingDialog = loadin_dialog(this@Login)

        signup_btn = findViewById(R.id.signup_btn)
        signup_btn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        })

        val forgotPassword_btn :TextView = findViewById(R.id.forgot_password)
       forgotPassword_btn.setOnClickListener(View.OnClickListener {
           val goto_forgotpassword = Intent(this, ForgotPassword::class.java)
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
                                val userID = mFirebaseAuth!!.currentUser?.uid


                                val map = hashMapOf<String, String>()
                                val deviceToken = FirebaseInstanceId.getInstance().getToken()
                                map.put("token", deviceToken.toString())
                                FirebaseDatabase.getInstance().getReference("Users").child(userID.toString()).updateChildren(map as Map<String, Any>)
//                                val intToDashboard = Intent(this@Login, Navigation::class.java)
//                                startActivity(intToDashboard)

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


        }

        signin!!.setOnClickListener(View.OnClickListener {
            val sign = signInClient!!.getSignInIntent()
            startActivityForResult(sign, GOOGLE_SIGN_IN_CODE)
//            var loadingDialog = loadin_dialog(this@Login)
//            loadingDialog.startDialog()
//            val user_id= mFirebaseAuth?.currentUser?.uid
//            FirebaseDatabase.getInstance().getReference("Users").child(user_id.toString()).setValue(user_google)
        })

        this.mAuthStateListener = AuthStateListener {

            val mFirebaseUser = mFirebaseAuth?.currentUser
            val uid = mFirebaseUser?.uid
            if (mFirebaseUser != null) {
                Log.d("check_login","login_ornot")
                var user_id = mFirebaseUser.uid
                if (uid != null) {
                    Log.d("check_login1",uid)
                }
                Log.d("check_login",user_id)
                loadingDialog.startDialog()
                if (user_info!=null)
                {
                    FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.hasChild(FirebaseAuth.getInstance().currentUser?.uid.toString())) {
                                Log.d("checck_In", "check_in_check_google")
                                FirebaseDatabase.getInstance().getReference("Users").child(user_id).setValue(user_info)
                                val userID = mFirebaseAuth!!.currentUser?.uid
                                val map = hashMapOf<String, String>()
                                val deviceToken = FirebaseInstanceId.getInstance().getToken()
                                map.put("token", deviceToken.toString())
//                                if (!snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).hasChild("token")) {
                                FirebaseDatabase.getInstance().getReference("Users").child(user_id.toString()).updateChildren(map as Map<String, Any>)
//                                } else if (snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).hasChild("token")) {
//                                    if (!snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("token").value?.equals(deviceToken)!!) {
//                                        FirebaseDatabase.getInstance().getReference("Users").child(userID.toString()).updateChildren(map as Map<String, Any>)
//                                    }
//                                }
                            } else if (snapshot.hasChild(FirebaseAuth.getInstance().currentUser?.uid.toString())) {
                                Log.d("checck_In", "check_in_check_google")
                                val userID = mFirebaseAuth!!.currentUser?.uid
                                val map = hashMapOf<String, String>()
                                val deviceToken = FirebaseInstanceId.getInstance().getToken()
                                map.put("token", deviceToken.toString())
//                                if (!snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).hasChild("token")) {
                                FirebaseDatabase.getInstance().getReference("Users").child(user_id.toString()).updateChildren(map as Map<String, Any>)
//                                } else if (snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).hasChild("token")) {
//                                    if (!snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("token").value?.equals(deviceToken)!!) {
//                                        FirebaseDatabase.getInstance().getReference("Users").child(userID.toString()).updateChildren(map as Map<String, Any>)
//                                    }
//                                }
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })


                }

                user_ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.child(user_id).hasChild("verification")) {
                            loadingDialog.dismissdialog()
                            val alert = AlertDialog.Builder(this@Login)
                            alert.setCancelable(true)
                            alert.setTitle("Notification")
                            alert.setMessage("Please verify your Identity before direct to use the application")
                            alert.setNegativeButton("Continue")
                            { dialog, which ->
                                dialog.cancel()
                                val intent = Intent(this@Login, Identify_verification_id::class.java)
                                startActivity(intent)
                            }

                            alert.show()
                        } else if (snapshot.child(user_id).hasChild("verification")) {
                            if (snapshot.child(user_id).child("verification").value!!.equals("")) {
                                val alert = AlertDialog.Builder(this@Login)
                                loadingDialog.dismissdialog()
                                alert.setCancelable(true)
                                alert.setTitle("Notification")
                                alert.setMessage("Please wait for our staff to contact you via email, when the verification was done")
                                alert.setNegativeButton("Dismiss")
                                { dialog, which ->
                                    FirebaseAuth.getInstance().signOut()
                                    LoginManager.getInstance().logOut()
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                    val googleSignInClient = GoogleSignIn.getClient(applicationContext, gso)
                                    googleSignInClient.signOut()
                                    dialog.cancel()
                                }
                                alert.show()
                            } else if (snapshot.child(user_id).child("verification").value!!.equals("yes")) {
                                val userID = mFirebaseAuth!!.currentUser?.uid
                                val map = hashMapOf<String, String>()
                                val deviceToken = FirebaseInstanceId.getInstance().getToken()
                                map.put("token", deviceToken.toString())
                                FirebaseDatabase.getInstance().getReference("Users").child(userID.toString()).updateChildren(map as Map<String, Any>).addOnSuccessListener {

                                    if (user_id!= null)
                                    {
                                        user_ref.child(user_id).addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if(snapshot.exists())
                                                {
                                                    if (snapshot.child("token").exists())
                                                    {
                                                        var token_fb: String = snapshot.child("token").getValue().toString()
                                                        if (!token_fb!!.equals("")) {
                                                            val checkToken: String = FirebaseInstanceId.getInstance().getToken().toString()
                                                            if (!token_fb!!.equals(checkToken)) {
                                                                if (!user_id.equals(null))
                                                                {
                                                                    FirebaseAuth.getInstance().signOut()
                                                                    LoginManager.getInstance().logOut()
                                                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                                                    val googleSignInClient = GoogleSignIn.getClient(this@Login, gso)
                                                                    googleSignInClient.signOut()

                                                                    Log.d("check_id", user_id)
                                                                    Log.d("check_condition", token_fb)
                                                                    Log.d("check_condition2",checkToken)

                                                                    if(!user_id.isEmpty())
                                                                    {
                                                                        val intent = Intent(this@Login, Start::class.java)
                                                                        intent.putExtra("showDialog",true)
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                                        startActivityForResult(intent,2)
                                                                        user_id = ""
                                                                        token_fb = ""

                                                                    }

                                                                }
                                                            }

                                                        }
                                                    }


                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }

                                        })
                                    }

                                    loadingDialog.dismissdialog()
                                    Toast.makeText(this@Login, "Welcome to Trade With Me application", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@Login, Navigation::class.java)
                                    startActivity(intent)
                                }

                            }
                        }


                    }


                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

//                if (user_id!= null)
//                {
//                    user_ref.child(user_id).addValueEventListener(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if(snapshot.exists())
//                            {
//                                if (snapshot.child("token").exists())
//                                {
//                                    var token_fb: String = snapshot.child("token").getValue().toString()
//                                    if (!token_fb!!.equals("")) {
//                                        val checkToken: String = FirebaseInstanceId.getInstance().getToken().toString()
//                                        if (!token_fb!!.equals(checkToken)) {
//                                            if (!user_id.equals(null))
//                                            {
//                                                FirebaseAuth.getInstance().signOut()
//                                                LoginManager.getInstance().logOut()
//                                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
//                                                val googleSignInClient = GoogleSignIn.getClient(this@Login, gso)
//                                                googleSignInClient.signOut()
//
//                                                Log.d("check_id", user_id)
//                                                Log.d("check_condition", token_fb)
//                                                Log.d("check_condition2",checkToken)
//
//                                                if(!user_id.isEmpty())
//                                                {
//                                                    val intent = Intent(this@Login, Start::class.java)
//                                                    intent.putExtra("showDialog",true)
////                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                                                    startActivityForResult(intent,2)
//                                                    user_id = ""
//                                                    token_fb = ""
//
//                                                }
//
//                                            }
//                                        }
//
//                                    }
//                                }
//
//
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                            TODO("Not yet implemented")
//                        }
//
//                    })
//                }





//                Toast.makeText(this@Login, "Welcome to Trade With Me application", Toast.LENGTH_SHORT).show()
////                FirebaseDatabase.getInstance().getReference("Users").child(user_id).setValue(user_google)
//                val intent = Intent(this@Login, Navigation::class.java)
//                startActivity(intent)
            } else {
//                Toast.makeText(this@Login, "Please Login", Toast.LENGTH_SHORT).show()
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
//                var loadingDialog = loadin_dialog(this@Login)
//                loadingDialog.startDialog()

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

        Log.d("check_code", requestCode.toString());

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
                    val phone_number:String = "The user have to edit first"
                    user_info = User(
                            firstname, lastname, email, phone_number
                    )
                    Log.d("userinfo", user_info.toString())

                    val mFirebaseUser = mFirebaseAuth?.currentUser
                    val user_id = mFirebaseUser?.uid

                    FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.hasChild(FirebaseAuth.getInstance().currentUser?.uid.toString())) {
                                Log.d("checck_In", "check_in")
                                FirebaseDatabase.getInstance().getReference("Users").child(user_id.toString()).setValue(user_info)
                                val userID = mFirebaseAuth!!.currentUser?.uid
                                val map = hashMapOf<String, String>()
                                val deviceToken = FirebaseInstanceId.getInstance().getToken()
                                map.put("token", deviceToken.toString())
//                                if (!snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).hasChild("token")) {
                                FirebaseDatabase.getInstance().getReference("Users").child(user_id.toString()).updateChildren(map as Map<String, Any>)
//                                } else if (snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).hasChild("token")) {
//                                    if (!snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("token").value?.equals(deviceToken)!!) {
//                                        FirebaseDatabase.getInstance().getReference("Users").child(userID.toString()).updateChildren(map as Map<String, Any>)
//                                    }
//                                }
                            } else if (snapshot.hasChild(FirebaseAuth.getInstance().currentUser?.uid.toString())) {

                                val userID = mFirebaseAuth!!.currentUser?.uid
                                val map = hashMapOf<String, String>()
                                val deviceToken = FirebaseInstanceId.getInstance().getToken()
                                map.put("token", deviceToken.toString())
                                Log.d("checck_In", deviceToken.toString())

//                                if (!snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).hasChild("token")) {
                                FirebaseDatabase.getInstance().getReference("Users").child(user_id.toString()).updateChildren(map as Map<String, Any>).addOnCompleteListener(OnCompleteListener {  Log.d("checck_In", "finish")})
//                                } else if (snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).hasChild("token")) {
//                                    if (!snapshot.child(FirebaseAuth.getInstance().currentUser?.uid.toString()).child("token").getValue()?.equals(deviceToken)!!) {
//                                        Log.d("checck_In", "check_in_again_2")
//                                        FirebaseDatabase.getInstance().getReference("Users").child(userID.toString()).updateChildren(map as Map<String, Any>)
//                                    }
//                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }


                    })




//                    user_ref.addValueEventListener(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if (!snapshot.child(user_id.toString()).hasChild("verification")) {
//                                val alert = AlertDialog.Builder(this@Login)
//                                alert.setCancelable(true)
//                                alert.setTitle("Notification")
//                                alert.setMessage("Please verify your Identity before direct to use the application")
//                                alert.setNegativeButton("Continue")
//                                { dialog, which ->
//                                    dialog.cancel()
//                                    val intent = Intent(this@Login, Identify_verification_id::class.java)
//                                    startActivity(intent)
//                                }
//
//                                alert.show()
//                            } else if (snapshot.child(user_id.toString()).hasChild("verification")) {
//                                if (snapshot.child(user_id.toString()).child("verification").value!!.equals("")) {
//                                    val alert = AlertDialog.Builder(this@Login)
//                                    alert.setCancelable(true)
//                                    alert.setTitle("Notification")
//                                    alert.setMessage("Please wait for our staff to contact you via email, when the verification was done")
//                                    alert.setNegativeButton("Dismiss")
//                                    { dialog, which ->
//                                        FirebaseAuth.getInstance().signOut()
//                                        LoginManager.getInstance().logOut()
//                                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
//                                        val googleSignInClient = GoogleSignIn.getClient(applicationContext, gso)
//                                        googleSignInClient.signOut()
//                                        dialog.cancel()
//                                    }
//
//                                    alert.show()
//                                } else if (snapshot.child(user_id.toString()).child("verification").value!!.equals("yes")) {
//                                    Toast.makeText(this@Login, "Welcome to Trade With Me application", Toast.LENGTH_SHORT).show()
////                FirebaseDatabase.getInstance().getReference("Users").child(user_id).setValue(user_google)
//                                    val intent = Intent(this@Login, Navigation::class.java)
//                                    startActivity(intent)
//                                }
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                            TODO("Not yet implemented")
//                        }
//
//                    })
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
//                    openProfile()
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
        Log.d("check_login", FirebaseAuth.getInstance().currentUser?.uid.toString())
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
            val phone_number: String = "The user have to edit first"


            user_info = User(
                    firstname, lastname, email, phone_number
            )

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
//            openProfile()
        }



//        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateFacebook!!)
    }

    override fun onStop() {
        super.onStop()
        if (mAuthStateListener !=null)
        {
            mFirebaseAuth?.removeAuthStateListener(mAuthStateListener!!)
        }

    }
}





