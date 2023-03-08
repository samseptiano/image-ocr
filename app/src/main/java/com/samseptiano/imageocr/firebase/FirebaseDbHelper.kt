package com.samseptiano.imageocr.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samseptiano.imageocr.model.DistanceGet

class FirebaseDbHelper {

    private val database = FirebaseDatabase.getInstance()
    private val distanceMatrixRef = database.getReference("distanceMatrix")

    private var distance = ArrayList<DistanceGet>()

    fun readDataDistance(status : DataStatus) {
        distanceMatrixRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.w("DATABASE_ERROR", "Error while reading data", error.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    data.getValue(DistanceGet::class.java)?.let { distance.add(it) }
                }

                status.DataIsLoaded(distance.last())
            }

        })
    }

    fun writeDataDistance(text : String, distance : String, duration: String) {
        val key = distanceMatrixRef.push().key.orEmpty()
        distanceMatrixRef.child(key).setValue(DistanceGet(text, distance, duration))
    }

    interface DataStatus {
        fun DataIsLoaded(distance : DistanceGet) = Unit
    }
}