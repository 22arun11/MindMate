package com.example.applicationlogin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.applicationlogin.databinding.ActivityProfileBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase. firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        loadUserData()

        binding.btnSaveProfile.setOnClickListener {
            saveUserData()
        }
        setupNavigationDrawer()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        binding.etName.setText(document.getString("name") ?: "")
                        binding.etAge.setText(document.getLong("age")?.toString() ?: "")
                        binding.etMajor.setText(document.getString("major") ?: "")
                        binding.etYear.setText(document.getLong("year")?.toString() ?: "")
                        binding.etStressLevel.setText(document.getLong("stressLevel")?.toString() ?: "")
                    } else {
                        Toast.makeText(this, "No existing profile data found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            // Redirect to login if user is not authenticated
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userData = hashMapOf(
                "name" to binding.etName.text.toString(),
                "age" to binding.etAge.text.toString().toIntOrNull(),
                "major" to binding.etMajor.text.toString(),
                "year" to binding.etYear.text.toString().toIntOrNull(),
                "stressLevel" to binding.etStressLevel.text.toString().toIntOrNull()
            )

            db.collection("users").document(userId).set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            // Redirect to login if user is not authenticated
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupNavigationDrawer() {
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        // This is crucial - it enables the hamburger menu
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_realtime -> {
                    startActivity(Intent(this, RealtimeActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_chatbot -> {
                    startActivity(Intent(this, ChatbotActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        auth.signOut()
        Intent(this, LoginActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show()
        }
    }
}