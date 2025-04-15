package com.rajeev0521.studenthub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class forgotPassword : AppCompatActivity() {
    private lateinit var firebase: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        var forgotBtn:Button = findViewById(R.id.resetButton)
        var emailField:EditText = findViewById(R.id.emailResetText)
        var backLogin:TextView = findViewById(R.id.backLogin)

        firebase = FirebaseAuth.getInstance()

        forgotBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            if(email.isEmpty()){
                emailField.error="Email Required"
                return@setOnClickListener
            }
            firebase.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val result = task.result?.signInMethods
                    Log.d("FirebaseDebug", "Fetched sign-in methods: $result")
                    if (result != null && result.contains("password")) {
                        // Send the password reset email
                        firebase.sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Reset Link Sent to your Email", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                            }
                    }else{
                        // Handle failure in fetching sign-in methods
                        Toast.makeText(this, "Failed to check email registration", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            }
        }
        backLogin.setOnClickListener {
            startActivity( Intent(this,LoginPage::class.java))
            finish()
        }
    }
}