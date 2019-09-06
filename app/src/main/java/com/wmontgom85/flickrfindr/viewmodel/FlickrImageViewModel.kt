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

class FlickrImageViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {
    private var job : Job? = null

    override val coroutineContext: CoroutineContext get() = Dispatchers.IO

    // flickr image data access object
    private val flickrImageDao : FlickrImageDao? by lazy { DBHelper.getInstance(application)?.flickrImageDao() }

    // live data for checking if the image has been favorited
    val imageIsFavorited = MutableLiveData<Boolean>()

    // live data for performing favoriting of the item
    val imageFavResult = MutableLiveData<Boolean>()

    // live data for performing unfavoriting of the item
    val imageUnfavResult = MutableLiveData<Boolean>()

    // error handler that will post errors that occur
    val errorHandler = MutableLiveData<String>()

    fun getImageIsFavorited(id: String) {
        launch {
            flickrImageDao?.let { imgDao ->
                imgDao.getImage(id)?.let {
                    // it's there. post true
                    imageIsFavorited.postValue(true)
                } ?: run {
                    // it's not there. post false.
                    imageIsFavorited.postValue(false)
                }
            } ?: run {
                // eesh. something went wrong the the dao. return false
                imageIsFavorited.postValue(false)
            }
        }
    }

    fun favoriteImage(img : FlickrImage) {
        job?.cancel()

        job = launch {
            flickrImageDao?.let { imgDao ->
                // remove from db
                imgDao.insert(img)

                // make sure it was inserted into the db
                imageFavResult.postValue(imgDao.getImage(img.id)?.let { true } ?: run { false })
            } ?: run {
                // eesh. something went wrong the the dao. return false
                errorHandler.postValue("An error has occurred while attempting to favorite this image.")
            }
        }
    }

    fun unfavoriteImage(img : FlickrImage) {
        job?.cancel()

        job = launch {
            flickrImageDao?.let { imgDao ->
                // remove from db
                imgDao.delete(img)

                // make sure it was removed into the db
                imageUnfavResult.postValue(imgDao.getImage(img.id)?.let { false } ?: run { true })
            } ?: run {
                // eesh. something went wrong the the dao. return false
                errorHandler.postValue("An error has occurred while attempting to unfavorite this image.")
            }
        }
    }

    // cancels coroutine scope and all children
    fun cancelRequest() = job?.cancel()
}