package com.wmontgom85.flickrfindr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.wmontgom85.flickrfindr.repo.DBHelper
import com.wmontgom85.flickrfindr.repo.dao.FlickrImageDao
import com.wmontgom85.flickrfindr.repo.model.FlickrImage
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class FavoritesViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {
    private var job : Job? = null

    override val coroutineContext: CoroutineContext get() = Dispatchers.IO

    // flickr image data access object
    private val flickrImageDao : FlickrImageDao? by lazy { DBHelper.getInstance(application)?.flickrImageDao() }

    // live data for getting images from db
    val favImagesLiveData = MutableLiveData<List<FlickrImage>?>()

    fun getImages() {
        job = launch {
            flickrImageDao?.let { imgDao ->
                favImagesLiveData.postValue(imgDao.getImages())
            } ?: run {
                // eesh. something went wrong the the dao. return false
                favImagesLiveData.postValue(null)
            }
        }
    }

    // cancels coroutine scope and all children
    fun cancelRequest() = job?.cancel()
}