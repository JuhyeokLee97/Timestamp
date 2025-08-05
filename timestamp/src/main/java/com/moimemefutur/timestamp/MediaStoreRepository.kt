package com.moimemefutur.timestamp

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.IOException

internal class MediaStoreRepository {

    fun saveToGallery(context: Context, imagePath: String, folderName: String? = null): Uri? {
        val file = File(imagePath)
        val fileName = file.name

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.IS_PENDING, 1)
            folderName?.let { put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/$it") }
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val imageUri = resolver.insert(collection, contentValues)

        if (imageUri != null) {
            try {
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)

                return imageUri
            } catch (e: IOException) {
                e.printStackTrace()
                resolver.delete(imageUri, null, null)
            }
        }

        return null
    }
}