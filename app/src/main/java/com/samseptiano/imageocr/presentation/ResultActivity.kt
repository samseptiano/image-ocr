package com.samseptiano.imageocr.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.samseptiano.imageocr.databinding.ActivityResultBinding
import com.samseptiano.imageocr.firebase.FirebaseDbHelper
import com.samseptiano.imageocr.model.DistanceGet

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var firebaseHelper: FirebaseDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseHelper = FirebaseDbHelper()

        showFirebaseData()
    }

    private fun showFirebaseData(){
        firebaseHelper.readDataDistance(object : FirebaseDbHelper.DataStatus {
            override fun DataIsLoaded(distance: DistanceGet) {
                with(binding){
                    resultEt.setText(distance.textWritten.orEmpty())
                    resultDistanceEt.setText("Estimate Distance :${distance.distance.orEmpty()}")
                    resultTimeEt.setText("Estimate Time:${distance.duration.orEmpty()}")
                }
            }
        })
    }
}