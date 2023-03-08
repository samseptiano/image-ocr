package com.samseptiano.imageocr.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.samseptiano.imageocr.R
import com.samseptiano.imageocr.databinding.ActivityMainBinding
import com.samseptiano.imageocr.firebase.FirebaseDbHelper
import com.samseptiano.imageocr.model.DistanceMatrix
import com.samseptiano.imageocr.network.NetworkRequest.Companion.createApi
import com.samseptiano.imageocr.util.*
import com.samseptiano.imageocr.util.Constants.INTENT_OCR_VALUE
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception



class MainActivity : AppCompatActivity() , LocationListener {

    private lateinit var binding: ActivityMainBinding
    private var latestTmpUri: Uri? = null
    private var isTextOnly = true
    private var stringValue = ""
    private var currentAddress = ""
    private var currentLat = ""
    private var currentLong = ""
    private lateinit var locationManager: LocationManager
    private lateinit var firebaseHelper: FirebaseDbHelper

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this)
            }
        }
    }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            CropImage.activity(it)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
        }
    }

    private val requestLocationPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getLocation()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    Log.i("DEBUG", "permission fine location denied")
                }
            }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            var isAllGranted  = true
            isGranted.map {
                if(!it.value){
                    isAllGranted = false
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    Log.i("DEBUG", "permission camera denied")
                }

            }
            if (isAllGranted) {
                takeImage()
                Log.i("DEBUG", "permission camera granted")
            }
        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                selectImageFromGallery()
                Log.i("DEBUG", "permission read storage granted")
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                Log.i("DEBUG", "permission read storage denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        firebaseHelper = FirebaseDbHelper()

        setupToolbar()
        setupViews()
        getLocation()
    }

    private fun setupToolbar(){
        val actionBar = supportActionBar
        actionBar!!.subtitle = getString(R.string.label_toolbar_subtitle)
    }

    private fun setupViews() = with(binding){
        btnResult.setOnClickListener{
            if(stringValue.isNotEmpty()) {
                getDistance()
            }
            else{
                Toast.makeText(this@MainActivity, getString(
                    R.string.label_error_select_image
                ), Toast.LENGTH_SHORT).show()
            }
        }

        rbCalculator.isChecked = true

        rbCalculator.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isTextOnly = false
            }
        }
        rbTextOnly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isTextOnly = true
            }
        }
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addImage -> showImageImportDialog()
            R.id.about -> dialogAbout()
        }
        return true
    }

    private fun dialogAbout() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.label_about))
                .setMessage(getString(R.string.label_about_desc))
                .setPositiveButton(getString(R.string.label_close)) { dialog, which -> dialog.dismiss() }
                .show()
    }

    private fun showImageImportDialog() {
        val items = arrayOf(getString(R.string.label_camera), getString(R.string.label_gallery))
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.label_select_image))

        dialog.setItems(items) { _, which ->

            when(which){
                0 ->{
                    if (!checkCameraPermission()) {
                        requestCameraPermission()
                    } else {
                        takeImage()
                    }
                }
                1->{
                    if (!checkStoragePermission()) {
                        requestStoragePermission()
                    } else {
                        selectImageFromGallery()
                    }
                }
            }
        }
        dialog.create().show()
    }

    private fun requestLocationPermission() {
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun requestStoragePermission() {
        requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestCameraPermission.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun getLocation() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!checkLocationPermission()) {
            requestLocationPermission()
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    override fun onLocationChanged(p0: Location) {
        currentLat = p0.latitude.toString()
        currentLong = p0.longitude.toString()
        currentAddress = getLocationAddress(p0.latitude, p0.longitude)
    }

    @SuppressLint("SetTextI18n")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri

                writeMetadataLocation(resultUri.path.toString(), currentLat,currentLong)

                binding.imageIv.setImageURI(resultUri)

                readOCRBitmapToString((binding.imageIv.drawable as BitmapDrawable).bitmap).apply {
                    stringValue = if(!isTextOnly){
                        val exp = this.replace("\n","")
                        val mathResult =
                            try{
                                Expressions().eval(exp)
                            }catch (e:Exception){
                                ""
                            }
                        "$exp = $mathResult"
                    } else {
                       this
                    }
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDistance() {
        val origins = if(currentLat.isNotEmpty() && currentLong.isNotEmpty()) "$currentLat,$currentLong" else null
        val call = createApi().getDistance(origins = origins)
        call.enqueue(object : Callback<DistanceMatrix> {
            override fun onResponse(
                call: Call<DistanceMatrix>,
                response: Response<DistanceMatrix>
            ) {
                val distMatrix = response.body()

                firebaseHelper.writeDataDistance(
                    stringValue,
                    distMatrix?.rows?.get(0)?.elements?.get(0)?.distance?.text.orEmpty(),
                    distMatrix?.rows?.get(0)?.elements?.get(0)?.duration?.text.orEmpty()
                )

                startActivity(Intent(this@MainActivity, ResultActivity::class.java))
            }

            override fun onFailure(
                call: Call<DistanceMatrix>,
                t: Throwable
            ) {
                Toast.makeText(this@MainActivity, "Error network API", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@MainActivity, ResultActivity::class.java).apply {
                    putExtra(INTENT_OCR_VALUE, stringValue)
                })
            }

        })
    }
}