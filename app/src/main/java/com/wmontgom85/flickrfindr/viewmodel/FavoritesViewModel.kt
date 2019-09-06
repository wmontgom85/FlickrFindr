package com.wmontgom85.flickrfindr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.wmontgom85.flickrfindr.BuildConfig
import com.wmontgom85.flickrfindr.api.APIHandler
import com.wmontgom85.flickrfindr.api.APIRequest
import com.wmontgom85.flickrfindr.api.jsonadapter.FlickrJsonAdapter
import com.wmontgom85.flickrfindr.api.response.ImageSearchResponse
import com.wmontgom85.flickrfindr.repo.DBHelper
import com.wmontgom85.flickrfindr.repo.dao.FlickrImageDao
import com.wmontgom85.flickrfindr.repo.model.FlickrImage
import com.wmontgom85.flickrfindr.sealed.Result
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class FavoritesViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {
    private var job : Job? = null

    override val coroutineContext: CoroutineContext get() = Dispatchers.IO

    // flickr image data access object
    private val flickrImageDao : FlickrImageDao? by lazy { DBHelper.getInstance(application)?.flickrImageDao() }

    // live data for getting images from db
    val favImagesLiveData = MutableLiveData<List<FlickrImage>?>()

    // error handler that will post errors that occur
    val errorHandler = MutableLiveData<String>()

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