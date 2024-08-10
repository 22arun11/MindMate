package com.example.applicationlogin

import android.annotation.SuppressLint
import com.jakewharton.rxbinding3.widget.changeEvents
import io.reactivex.Observable
import android.content.Intent
//import android.database.Observable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import androidx.core.content.ContextCompat
import com.example.applicationlogin.databinding.ActivityLoginBinding
import com.example.applicationlogin.databinding.ActivityRegisterBinding
import com.jakewharton.rxbinding3.widget.textChanges

@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

// Full Name validation
        val nameStream = binding.etFullname.textChanges()
            . skipInitialValue()
            . map { name ->
                name.isEmpty()
            }
        nameStream.subscribe {
            showNameExistAlert(it)
        }
// Email Validation
        val emailStream = binding.etEmail.textChanges()

            . skipInitialValue ( )
            . map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showEmailValidAlert(it)
        }
// Username Validation
        val usernameStream = binding.etUsername.textChanges()
            .skipInitialValue()
            .map{ username ->
                username.length <6
            }
        usernameStream.subscribe {
            showTextMinimalAlert(it,"Username")
        }

// Password Validation
        val passwordStream = binding.etPassword.textChanges()
            .skipInitialValue()
            .map{ password ->
                password.length <8
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it,"Password")
        }

// Confirm Password Validation
        val passwordConfirmStream = Observable.merge(
            binding.etPassword.textChanges()
                . skipInitialValue ( )
                . map { password ->
                    password.toString() != binding.etConfirmPassword.text.toString()},

            binding.etConfirmPassword.textChanges()
                . skipInitialValue ( )
                . map { confirmPassword ->
                    confirmPassword.toString() != binding.etPassword.text.toString()
                }
        )
        passwordConfirmStream.subscribe {
            showPasswordConfirmAlert(it)
        }

        // Button Enable True or False
        val invalidFieldStream = Observable.combineLatest(
            nameStream,
            emailStream,
            usernameStream,
            passwordStream,
            passwordConfirmStream
        ) { nameInvalid: Boolean, emailInvalid: Boolean, usernameInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmInvalid: Boolean ->
            !nameInvalid && !emailInvalid && !usernameInvalid && !passwordInvalid && !passwordConfirmInvalid
        }


        invalidFieldStream.subscribe{ isValid->
            if (isValid){
                binding.register.isEnabled=true
                binding.register.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            }
            else{
                binding.register.isEnabled=false
                binding.register.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            }
        }

// Click
        binding.register.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
        }
        binding.tvHaveAccount.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }

    private fun showNameExistAlert(isNotValid: Boolean){
        binding.etFullname.error = if (isNotValid) "Name already exists" else null
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text:String){
        if (text=="Username")
            binding.etFullname.error = if (isNotValid) "$text Must be atleast 6 letters" else null
        if (text=="Password")
            binding.etPassword.error = if (isNotValid) "$text Must be atleast 8 letters" else null
    }

    private fun showEmailValidAlert(isNotValid : Boolean){
        binding.etEmail.error = if (isNotValid) "Invalid Email Address" else null
    }
    private fun showPasswordConfirmAlert(isNotValid: Boolean){
        binding.etConfirmPassword.error = if (isNotValid) "Password MisMatch" else null
    }

}
