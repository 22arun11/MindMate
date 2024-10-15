package com.example.applicationlogin

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.applicationlogin.databinding.ActivityHomeBinding
import com.example.applicationlogin.ml.FacialEmotionModel
import com.google.firebase.auth.FirebaseAuth
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import android.view.MenuItem

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var bitmap: Bitmap
    private lateinit var selectImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var labels: List<String>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            auth = FirebaseAuth.getInstance()
            setupNavigationDrawer()
            binding.btnLogout.setOnClickListener {
                auth.signOut()
                Intent(this, LoginActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                    Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show()
                }
            }

            // Load labels from assets
            try {
                labels = application.assets.open("labels.txt").bufferedReader().readLines()
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error loading labels.txt", e)
                Toast.makeText(this, "Error loading labels.txt: ${e.message}", Toast.LENGTH_LONG).show()
                return
            }

            selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val uri = result.data?.data
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                        val inputStream = contentResolver.openInputStream(uri!!)
                        val exif = ExifInterface(inputStream!!)
                        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                        bitmap = rotateBitmap(bitmap, orientation)
                        binding.imageIllustrationHome.setImageBitmap(bitmap)
                    } catch (e: IOException) {
                        Log.e("HomeActivity", "Error loading image", e)
                        Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
                    bitmap = imageBitmap
                    binding.imageIllustrationHome.setImageBitmap(bitmap)
                }
            }

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(48, 48, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(TransformToGrayscaleOp()) // Convert to grayscale if the model expects a single channel
                .build()

            binding.btnSelect.setOnClickListener {
                showImageSourceDialog()
            }

            binding.btnPredict.setOnClickListener {
                try {
                    val tensorImage = TensorImage(DataType.FLOAT32)
                    tensorImage.load(bitmap)
                    val processedImage = imageProcessor.process(tensorImage)

                    val model = FacialEmotionModel.newInstance(this)
                    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 48, 48, 1), DataType.FLOAT32)
                    inputFeature0.loadBuffer(processedImage.buffer)
                    val outputs = model.process(inputFeature0)
                    val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray
                    model.close()

                    var maxIdx = 0
                    outputFeature0.forEachIndexed { idx, fl ->
                        if (fl > outputFeature0[maxIdx]) {
                            maxIdx = idx
                        }
                    }
                    binding.resview.text = labels[maxIdx]
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Error during prediction", e)
                    Toast.makeText(this, "Prediction error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "Error during initialization", e)
            Toast.makeText(this, "Initialization error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Select from Gallery", "Capture from Camera")
        AlertDialog.Builder(this)
            .setTitle("Choose Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                        }
                        selectImageLauncher.launch(Intent.createChooser(intent, "Select Picture"))
                    }
                    1 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraLauncher.launch(intent)
                    }
                }
            }
            .show()
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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