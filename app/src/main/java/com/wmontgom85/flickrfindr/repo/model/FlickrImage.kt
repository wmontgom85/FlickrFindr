package com.wmontgom85.flickrfindr.repo.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.content.ContextWrapper
import java.io.*


/**
 * Flickr Image data class
 */
@Entity
data class FlickrImage (
    @PrimaryKey val id: String,
    val owner: String,
    val secret: String,
    val server: Int,
    val farm: Int,
    val title: String,
    val isPublic: Int,
    val isFriend: Int,
    val isFamily: Int
) : Serializable {
    var imagePath : String? = null

    fun getThumbnail() = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_m.jpg"

    fun getLargeImage() = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_b.jpg"

    fun storeImage(context: Context, bitmap : Bitmap?) {
        bitmap?.let {
            val contextWrapper = ContextWrapper(context)

            // image director
            val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)

            // Ccreate filepath
            val path = File(directory, "$id.png")

            var fileOutputStream: FileOutputStream? = null

            try {
                fileOutputStream = FileOutputStream(path)

                // compress and write to disk
                it.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

                imagePath = directory.absolutePath
            } catch (tx: Throwable) {
                Log.d("FlickrImage.storeImage", "message: ${tx.message}")
            } finally {
                fileOutputStream?.close()
            }
        }
    }

    fun getImage() : Bitmap? {
        return try {
            val file = File(imagePath, "$id.png")
            BitmapFactory.decodeStream(FileInputStream(file))
        } catch (tx: Throwable) {
            Log.d("FlickrImage.storeImage", "message: ${tx.message}")
            null
        }
    }

    fun getThumbnailFromLarge() : Bitmap? {
        val bm = getImage()

        return bm?.let {
            if (it.width > 225 || it.height > 225) {
                var newW = 225
                var newH = 225
                var newX = 0
                var newY = 0

                // thumbnail should be sized according to original dimensions and centercropped
                when {
                    (it.width > it.height) -> {
                        newW = ((it.width.toDouble() / it.height.toDouble()) * 225).toInt()
                        newX = (newW - newH) / 2
                    }
                    else -> {
                        newH = ((it.height.toDouble() / it.width.toDouble()) * 225).toInt()
                        newY = (newH - newW) / 2
                    }
                }

                // don't waste time scaling if the image is already square
                val scaledBM = when {
                    newW != newH -> Bitmap.createScaledBitmap(bm, newW, newH, false)
                    else -> bm
                }

                Bitmap.createBitmap(scaledBM, newX, newY, 225, 225)
            } else {
                // the image is already smaller than 225 (the thumbnail size)
                bm
            }
        } ?: run {
            null
        }
    }
}