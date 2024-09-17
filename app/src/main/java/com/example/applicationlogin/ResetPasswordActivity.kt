package com.example.applicationlogin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.applicationlogin.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding3.widget.textChanges

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

    // Auth
        auth = FirebaseAuth.getInstance()
    // Email Validation
        val emailStream = binding.etEmail.textChanges()

            . skipInitialValue ( )
            . map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showEmailValidAlert(it)
        }

    //Reset Password
        binding.btnResetPw.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this){reset ->
                    if(reset.isSuccessful){
                        Intent(this, LoginActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                            Toast.makeText(this, "Reset Password Email Sent", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "${reset.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    //Click
        binding.tvBackLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    private fun showEmailValidAlert(isNotValid : Boolean){
        if (isNotValid){
            binding.tilEmail.error = "Email is not valid"
            binding.btnResetPw.isEnabled=false
            binding.btnResetPw.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
        } else {
            binding.etEmail.error = null
            binding.btnResetPw.isEnabled=true
            binding.btnResetPw.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
        }
    }
}