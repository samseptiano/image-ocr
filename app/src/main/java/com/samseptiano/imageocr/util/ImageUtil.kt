package com.samseptiano.imageocr.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.samseptiano.imageocr.R
import java.io.File

fun Context.getTmpFileUri(): Uri {
    val tempImagesDir = File(
        applicationContext.filesDir,
        getString(R.string.temp_images_dir))

        tempImagesDir.mkdir()

    val tempImage = File(
        tempImagesDir,
        getString(R.string.temp_image))

    return FileProvider.getUriForFile(applicationContext,  getString(R.string.authorities), tempImage)
}