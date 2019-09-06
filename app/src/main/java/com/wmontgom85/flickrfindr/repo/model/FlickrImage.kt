package com.wmontgom85.flickrfindr.repo.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.ByteArrayOutputStream
import java.io.Serializable

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
    var imagebytes : ByteArray? = null

    fun getThumbnail() = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_m.jpg"

    fun getLargeImage() = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_b.jpg"

    fun setImageBytes(bitmap : Bitmap?) {
        bitmap?.let {
            try {
                val stream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, stream)
                this.imagebytes = stream.toByteArray()
            } catch (tx: Throwable) {
                this.imagebytes = null
            }
        } ?: run {
            this.imagebytes = null
        }
    }

    fun getImageFromByes() : Bitmap? {
        return imagebytes?.let {
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            } catch (tx: Throwable) {
                null
            }
        } ?: run {
            null
        }
    }

    fun getThumbnailFromLarge() : Bitmap? {
        val bm = getImageFromByes()

        return bm?.let {
            var newW = 225
            var newH = 225
            var newX = 0
            var newY = 0

            // thumbnail should be sized according to original dimensions and centercropped
            when {
                (it.width > it.height) -> {
                    newW = ((it.width.toDouble()/it.height.toDouble())*225).toInt()
                    newX = (newW-newH)/2
                }
                else -> {
                    newH = ((it.height.toDouble()/it.width.toDouble())*225).toInt()
                    newY = (newH-newW)/2
                }
            }

            println("resizing ${it.width}w x ${it.height}h to ${newW}w x ${newH}h cropping at ${newX}x and ${newY}y")

            // don't waste time scaling if the image is already square
            val scaledBM = when {
                newW != newH -> Bitmap.createScaledBitmap(bm, newW, newH, false)
                else -> bm
            }

            Bitmap.createBitmap(scaledBM, newX, newY, 225, 225)
        } ?: run {
            null
        }
    }
}