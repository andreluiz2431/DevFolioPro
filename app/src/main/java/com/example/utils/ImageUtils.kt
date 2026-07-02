package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Converts a media URI to a compressed, resized JPEG Base64 string.
     * Keeps the image light (max 250x250) for fast sync and low database footprint.
     */
    fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 250): String? {
        return try {
            val contentResolver = context.contentResolver
            
            // Step 1: Decode dimensions only
            var inputStream: InputStream? = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Step 2: Calculate the best sample size
            var sampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / sampleSize) >= maxDimension && (halfWidth / sampleSize) >= maxDimension) {
                    sampleSize *= 2
                }
            }

            // Step 3: Decode with sampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Step 4: Resize accurately
            val width = originalBitmap.width
            val height = originalBitmap.height
            val maxLen = Math.max(width, height)
            val finalBitmap = if (maxLen > maxDimension) {
                val ratio = maxDimension.toFloat() / maxLen
                val targetWidth = (width * ratio).toInt()
                val targetHeight = (height * ratio).toInt()
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            // Step 5: Compress JPEG & convert to Base64
            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            
            if (finalBitmap != originalBitmap) {
                finalBitmap.recycle()
            }
            originalBitmap.recycle()

            val bytes = outputStream.toByteArray()
            val base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64Data"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
