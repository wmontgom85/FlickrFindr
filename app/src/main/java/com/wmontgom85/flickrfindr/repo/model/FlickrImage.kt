package com.wmontgom85.flickrfindr.repo.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.content.ContextWrapper
import com.wmontgom85.flickrfindr.supp.centercrop
import com.wmontgom85.flickrfindr.supp.px
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

    /**
     * Builds the thumbnail image URL based on Flickr documentation
     * @return String
     */
    fun getThumbnail() = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_m.jpg"

    /**
     * Builds the large image URL based on Flickr documentation
     * @return String
     */
    fun getLargeImage() = "https://farm${farm}.staticflickr.com/${server}/${id}_${secret}_b.jpg"

    /**
     * Stores the image on disk
     * @param context Context
     * @param bitmap Bitmap?
     */
    fun storeImage(context: Context, bitmap : Bitmap) {
        bitmap.let {
            val contextWrapper = ContextWrapper(context)

            // image director
            val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)

            // create filepath
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

    /**
     * Deletes the file from internal storage
     */
    fun deleteImage() {
        try {
            val file = File(imagePath, "$id.png")
            file.delete()
        } catch (tx: Throwable) {
            tx.printStackTrace()
        }
    }

    /**
     * Retrieves the image stored internally
     * @return Bitmap
     */
    fun getImage() : Bitmap? {
        return try {
            val file = File(imagePath, "$id.png")
            BitmapFactory.decodeStream(FileInputStream(file))
        } catch (tx: Throwable) {
            tx.printStackTrace()
            null
        }
    }

    /**
     * Builds a thumbnail version of the large image
     * @return Bitmap
     */
    fun getThumbnailFromLarge() : Bitmap? {
        val bm = getImage()
        return bm?.centercrop(150.px())
    }
}