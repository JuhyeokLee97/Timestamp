package com.moimemefutur.timestamp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.widget.FrameLayout
import androidx.core.graphics.createBitmap
import com.moimemefutur.timestamp.extensions.rotateBitmap
import com.moimemefutur.timestamp.extensions.scaleToFillCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

internal class ImageCompositor {

    suspend fun composite(
        templateParentView: FrameLayout,
        photoFile: File
    ): String {
        val photoBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            .rotateBitmap(photoFile)
            .scaleToFillCenter(templateParentView.width, templateParentView.height)

        val templateBitmap = createBitmap(templateParentView.width, templateParentView.height).also { bitmap ->
            val canvas = Canvas(bitmap)
            templateParentView.draw(canvas)
        }

        val composedBitmap = createBitmap(templateParentView.width, templateParentView.height).also { bitmap ->
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(photoBitmap, 0f, 0f, null)
            canvas.drawBitmap(templateBitmap, 0f, 0f, null)
        }

        return withContext(Dispatchers.IO) {
            val compositeFile = File(photoFile.parent, "composite_${photoFile.name}")
            FileOutputStream(compositeFile).use { out ->
                composedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            composedBitmap.recycle()
            photoFile.delete()

            compositeFile.absolutePath
        }
    }
}