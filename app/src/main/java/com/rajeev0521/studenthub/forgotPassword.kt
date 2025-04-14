package com.rajeev0521.studenthub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
            val email = emailField.text.toString().trim().lowercase()
            if(email.isEmpty()){
                emailField.error="Email Required"
                return@setOnClickListener
            }
            firebase.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val result = task.result?.signInMethods
                    if(result.isNullOrEmpty()){
                        Toast.makeText(this, "Email not registered", Toast.LENGTH_LONG).show()
                    }else{
                        firebase.sendPasswordResetEmail(email).addOnCompleteListener {
                            Toast.makeText(this,"Reset Link Sent to your Email",Toast.LENGTH_LONG).show()
                        }
                            .addOnFailureListener {
                                Toast.makeText(this, "Something went jadj wrong", Toast.LENGTH_LONG).show()
                            }
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