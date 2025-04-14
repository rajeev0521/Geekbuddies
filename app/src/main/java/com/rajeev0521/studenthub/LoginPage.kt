package com.rajeev0521.studenthub

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.rajeev0521.studenthub.databinding.ActivityLoginPageBinding

class LoginPage : AppCompatActivity() {
    private lateinit var binding: ActivityLoginPageBinding
    private lateinit var firebase: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>


    companion object {
        private const val RC_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_login_page)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        binding= ActivityLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebase = FirebaseAuth.getInstance()

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.let {
                        firebaseAuthWithGoogle(it.idToken!!)
                    }
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_LONG).show()
                }
            }
        }

        var github_btn:ImageView = findViewById(R.id.github_btn)
        var linkedin_btn:ImageView = findViewById(R.id.linkedin_btn)
        var google_btn:ImageView = findViewById(R.id.google_btn)
        google_btn.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // From google-services.json
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
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

    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebase.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Sign-In Successful", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_LONG).show()
                }
            }
    }

}