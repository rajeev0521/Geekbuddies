package com.rajeev0521.studenthub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.rajeev0521.studenthub.databinding.ActivitySignUpPageBinding

class sign_up_page : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpPageBinding
    private lateinit var firebase: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebase = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")



        binding.signupButton.setOnClickListener {
            val name = binding.nameText.text.toString()
            val email = binding.emailText.text.toString()
            val sysId = binding.systemIdText.text.toString()
            val phoneNo = binding.phoneText.text.toString()
            val pass = binding.passwordText.text.toString()
            val rePass = binding.rePasText.text.toString()

            if(name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && sysId.isNotEmpty() && rePass.isNotEmpty()){
                if(pass.length<6 && rePass.length<6){
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show()
                }else if (pass != rePass){
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                else{
                    firebase.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this) {task ->
                        if(task.isSuccessful){
                            val userId = firebase.currentUser?.uid
                            val user = User(name,sysId,phoneNo,email)

                            userId?.let {
                                usersRef.child(sysId).setValue(user).addOnSuccessListener {
                                    Log.d("FIREBASE_WRITE", "User data saved")
                                    Toast.makeText(this,"Sign Up Successful",Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this,LoginPage::class.java))
                                    finish()
                                }
                                    .addOnFailureListener{
                                        Log.e("FIREBASE_WRITE", "Write failed: ${it.message}")
                                        Toast.makeText(this, "Database Error: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else{
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

            } else{
                Toast.makeText(this,"Please Enter All Value",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }
        binding.loginText.setOnClickListener {
            startActivity(Intent(this,LoginPage::class.java))
            finish()
        }
        var isPasswordVisible = false
        binding.passwordToggle1.setOnClickListener{
            isPasswordVisible = !isPasswordVisible
            if(isPasswordVisible){
                binding.passwordText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.passwordToggle1.setImageResource(R.drawable.ic_eye_open)
            }else{
                binding.passwordText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.passwordToggle1.setImageResource(R.drawable.ic_eye_closed)
            }
            binding.passwordText.setSelection(binding.passwordText.text.length)
        }
    }
}