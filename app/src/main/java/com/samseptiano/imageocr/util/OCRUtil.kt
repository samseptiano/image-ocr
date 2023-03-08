package com.samseptiano.imageocr.util

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.samseptiano.imageocr.R


fun Context.readOCRBitmapToString(image: Bitmap) : String {
        var resultValue = ""
        val recognizer = TextRecognizer.Builder(this).build()
        if (!recognizer.isOperational) {
            Toast.makeText(this, getString(R.string.label_error), Toast.LENGTH_SHORT)
                .show()
        } else {
            val frame = Frame.Builder().setBitmap(image).build()
            val items = recognizer.detect(frame)
            val sb = StringBuilder()
            for (i in 0 until items.size()) {
                val myItem = items.valueAt(i)
                sb.append(myItem.value+"\n")
            }
            resultValue =  sb.toString()
        }
        return resultValue
    }
