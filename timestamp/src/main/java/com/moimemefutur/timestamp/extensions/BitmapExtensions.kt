package com.moimemefutur.timestamp.extensions

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.core.graphics.scale
import java.io.File

internal fun Bitmap.rotateBitmap(imageFile: File): Bitmap {
    val exif = ExifInterface(imageFile.absolutePath)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotate(270f)
        else -> this
    }
}

internal fun Bitmap.rotate(degree: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degree) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

internal fun Bitmap.scaleToFillCenter(targetWidth: Int, targetHeight: Int): Bitmap {
    val bitmapRatio = width.toFloat() / height
    val targetRatio = targetWidth.toFloat() / targetHeight

    val scaleFactor = if (bitmapRatio > targetRatio) {
        targetHeight.toFloat() / height
    } else {
        targetWidth.toFloat() / width
    }

    val scaledWidth = (width * scaleFactor).toInt()
    val scaledHeight = (height * scaleFactor).toInt()

    val scaledBitmap = scale(scaledWidth, scaledHeight)
    val startX = (scaledWidth - targetWidth) / 2
    val startY = (scaledHeight - targetHeight) / 2

    return Bitmap.createBitmap(scaledBitmap, startX, startY, targetWidth, targetHeight)
}