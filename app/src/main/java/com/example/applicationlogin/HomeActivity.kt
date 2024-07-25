package com.example.applicationlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.applicationlogin.databinding.ActivityHomeBinding
import com.example.applicationlogin.databinding.ActivityMainBinding
import com.example.applicationlogin.databinding.ActivityRegisterBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }
}