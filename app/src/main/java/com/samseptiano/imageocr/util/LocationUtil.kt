package com.samseptiano.imageocr.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.media.ExifInterface
import java.io.IOException
import java.util.*


fun Context.getLocationAddress(lat: Double, long: Double):String {
        val addresses: List<Address>
        val geocoder = Geocoder(this, Locale.getDefault())
        addresses = geocoder.getFromLocation(
            lat,
            long,
            5
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        val address = addresses[0].getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

        return address.toString()
}

fun writeMetadataLocation(path: String, currentLat:String, currentLong:String) {
    try {
        val exif = ExifInterface(path)
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, currentLat)
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, currentLong)
        exif.saveAttributes()
    } catch (e: IOException) {
        // handle the error
    }
}