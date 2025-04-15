package com.rajeev0521.studenthub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.rajeev0521.studenthub.databinding.ActivityLoginPageBinding
import okhttp3.Callback
import okhttp3.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.FormBody
import okhttp3.Call
import org.json.JSONObject
import java.io.IOException

class LoginPage : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var firebase: FirebaseAuth
    private val RC_SIGN_IN = 1001
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        const val RC_SIGN_IN = 1001
        const val GITHUB_SIGN_IN_REQUEST_CODE = 1002

        const val LINKEDIN_CLIENT_ID = "86mg2vhweewaw8"
        const val LINKEDIN_CLIENT_SECRET = "WPL_AP1.t7kciRvMtfnq7nAn.T7DQfg=="
        const val LINKEDIN_REDIRECT_URI = "https://student_hub.com/linkedin-callback"
        const val LINKEDIN_SCOPE = "r_liteprofile%20r_emailaddress"
        const val LINKEDIN_AUTH_URL = "https://www.linkedin.com/oauth/v2/authorization"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebase = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleSignInButton:ImageView = findViewById(R.id.google_btn)
        googleSignInButton.setOnClickListener{
            signInWithGoogle()
        }

        var github_btn:ImageView = findViewById(R.id.github_btn)
        github_btn.setOnClickListener {
            signInWithGitHub()
        }

        var linkedin_btn:ImageView = findViewById(R.id.linkedin_btn)
        linkedin_btn.setOnClickListener{
            val service = LinkedInOAuthHelper.getService()
            val authUrl = service.authorizationUrl
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val username = binding.usernameText.text.toString()
            val pass = binding.passwordText.text.toString()
            if(username.isNotEmpty() && pass.isNotEmpty()){
                firebase.signInWithEmailAndPassword(username,pass)
                    .addOnCompleteListener(this) {task ->
                        if(task.isSuccessful){
                            Toast.makeText(this,"Login Successful",Toast.LENGTH_LONG).show()
                            val intent = Intent(this,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this,"Login Failed",Toast.LENGTH_LONG).show()
                        }
                    }
            }else{
                Toast.makeText(this,"Please Enter username and password",Toast.LENGTH_LONG).show()
            }
        }
        binding.singupText.setOnClickListener {
            startActivity(Intent(this,sign_up_page::class.java))
            finish()
        }
        binding.forgetText.setOnClickListener {
            val intent = Intent(this,forgotPassword::class.java)
            startActivity(intent)
        }
    }

//    Google Sign In
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebase.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebase.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_LONG).show()
                    // Navigate to home/main activity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Firebase Sign-In failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    //    For Github Source
    private fun signInWithGitHub() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GitHubBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        startActivityForResult(signInIntent, GITHUB_SIGN_IN_REQUEST_CODE)
    }

//    Linkedin Sign-In
    private fun startLinkedInLogin() {
        val authUrl = "$LINKEDIN_AUTH_URL?" +
                "response_type=code&" +
                "client_id=$LINKEDIN_CLIENT_ID&" +
                "redirect_uri=$LINKEDIN_REDIRECT_URI&" +
                "scope=$LINKEDIN_SCOPE"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)))
    }

    private fun exchangeCodeForToken(code: String) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", LINKEDIN_REDIRECT_URI)
            .add("client_id", LINKEDIN_CLIENT_ID)
            .add("client_secret", LINKEDIN_CLIENT_SECRET)
            .build()

        val request = Request.Builder()
            .url("https://www.linkedin.com/oauth/v2/accessToken")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginPage, "Profile fetch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body!!.string())
                val accessToken = json.getString("access_token")
                fetchLinkedInProfile(accessToken)
            }
        })
    }

    private fun fetchLinkedInProfile(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.linkedin.com/v2/me")
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginPage, "Profile fetch failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val userJson = JSONObject(response.body!!.string())
                val firstName = userJson.optString("localizedFirstName", "User")
                runOnUiThread {
                    Toast.makeText(this@LoginPage, "Welcome $firstName", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@LoginPage, MainActivity::class.java))
                    finish()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

//        For Google Source Code
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GOOGLE_SIGN_IN", "Google sign in failed", e)
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle GitHub Sign-In response
        if (requestCode == GITHUB_SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            Log.d("GITHUB_SIGN_IN", "Response: ${response?.error?.message}")
            if (resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, "GitHub Sign-In successful: ${user?.displayName}", Toast.LENGTH_LONG).show()
                updateUI(user)
            } else {
                Toast.makeText(this, "GitHub Sign-In failed", Toast.LENGTH_LONG).show()
                Log.e("GITHUB_SIGN_IN", "Error: ${response?.error?.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val uri = intent?.data
        if (uri != null && uri.toString().startsWith(LINKEDIN_REDIRECT_URI)) {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                exchangeCodeForToken(code)
            } else {
                Toast.makeText(this, "Authorization code is missing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Navigate to the home screen after successful login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

}