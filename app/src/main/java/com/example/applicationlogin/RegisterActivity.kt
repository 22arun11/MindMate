package com.example.applicationlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.applicationlogin.databinding.ActivityLoginBinding
import com.example.applicationlogin.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.register.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
        }
        binding.tvHaveAccount.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }


}