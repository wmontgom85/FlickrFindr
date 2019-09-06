package com.wmontgom85.flickrfindr.repo.dao

import androidx.room.*
import com.wmontgom85.flickrfindr.repo.model.FlickrImage

/**
 * Flickr Image Data Access Object
 */
@Dao
public interface FlickrImageDao {
    @Query("SELECT * FROM FlickrImage")
    fun getImages(): List<FlickrImage>?

    @Query("SELECT * FROM FlickrImage WHERE id = :id")
    fun getImage(id: String) : FlickrImage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(image: FlickrImage) : Long

    @Query("DELETE FROM FlickrImage WHERE id = :pId")
    fun delete(pId: String)

    @Delete
    fun delete(image: FlickrImage)

    @Update
    fun update(image: FlickrImage) : Int

    @Query("DELETE FROM FlickrImage")
    fun deleteAll()
}