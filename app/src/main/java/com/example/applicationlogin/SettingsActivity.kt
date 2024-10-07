package com.example.applicationlogin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val textView = findViewById<TextView>(R.id.textViewSettings)
        textView.text = "This is settings page"
    }
}