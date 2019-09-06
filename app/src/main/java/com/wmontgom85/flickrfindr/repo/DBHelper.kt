package com.wmontgom85.flickrfindr.repo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.wmontgom85.flickrfindr.repo.dao.FlickrImageDao
import com.wmontgom85.flickrfindr.repo.model.FlickrImage

/**
 * Database for persistent storage
 */
@Database(entities = [
    FlickrImage::class
], version = 1)
abstract class DBHelper : RoomDatabase() {
    abstract fun flickrImageDao(): FlickrImageDao

    companion object {
        private var INSTANCE: DBHelper? = null

        fun getInstance(context: Context): DBHelper? {
            if (INSTANCE == null) {
                synchronized(DBHelper::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        DBHelper::class.java, "flickr.db"
                    ).build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    fun cleanDB() {
        flickrImageDao().deleteAll()
    }
}