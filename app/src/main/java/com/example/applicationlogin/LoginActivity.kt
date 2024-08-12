package com.example.applicationlogin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.applicationlogin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//Authentication
        auth = FirebaseAuth.getInstance()


// Username Validation
        val usernameStream = binding.etEmail.textChanges()
            .skipInitialValue()
            .map{ username ->
                username.isEmpty()
            }
        usernameStream.subscribe {
            showTextMinimalAlert(it,"Username")
        }

// Password Validation
        val passwordStream = binding.etPassword.textChanges()
            .skipInitialValue()
            .map{ password ->
                password.isEmpty()
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it,"Password")
        }


// Button Enable True or False
        val invalidFieldStream = Observable.combineLatest(
            usernameStream,
            passwordStream,
        ) { usernameInvalid: Boolean, passwordInvalid: Boolean->
            !usernameInvalid && !passwordInvalid
        }

        invalidFieldStream.subscribe{ isValid->
            if (isValid){
                binding.btnLogin.isEnabled=true
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            }
            else{
                binding.btnLogin.isEnabled=false
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            }
        }
//click
        binding.btnLogin.setOnClickListener{
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            loginUser(email, password)
        }
        binding.tvHaventAccount.setOnClickListener{
            startActivity(Intent(this,RegisterActivity::class.java))
        }
    }




    private fun showTextMinimalAlert(isNotValid: Boolean, text:String){
        if (text=="Email/Username")
            binding.etEmail.error = if (isNotValid) "$text Invalid Email" else null
        if (text=="Password")
            binding.etPassword.error = if (isNotValid) "$text Must be at least 8 letters" else null
    }

    private fun loginUser(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ login ->
                if(login.isSuccessful){
                    Intent(this, HomeActivity::class.java).also{
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                    }
                } else{
                    Toast.makeText(this, login.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}