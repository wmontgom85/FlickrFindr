package com.wmontgom85.flickrfindr.repo.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
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
    var thumbnailBytes : ByteArray? = null
    var imagebytes : ByteArray? = null

    fun getThumbnail() = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_m.jpg"

    fun getLargeImage() = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_b.jpg"

    fun setThumbnailByes(bitmap : Bitmap) {

    }

    fun setImageBytes(bitmap : Bitmap) {

    }
}