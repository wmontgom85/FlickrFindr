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
import com.wmontgom85.flickrfindr.sealed.APIResult
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class FlickrSearchViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {
    private var job : Job? = null

    override val coroutineContext: CoroutineContext get() = Dispatchers.IO

    // flickr image data access object
    private val flickrImageDao : FlickrImageDao? by lazy { DBHelper.getInstance(application)?.flickrImageDao() }

    // live data that will be populated as search results are returned
    val flickrImagesLiveData = MutableLiveData<ImageSearchResponse>()

    // error handler that will post errors that occur
    val errorHandler = MutableLiveData<String>()

    @Suppress("UNCHECKED_CAST")
    fun performSearch(term : String, perPage: Int = 25) {
        // cancel any previous requests so we don't stack up request processing
        job?.cancel()

        when {
            term.isNotBlank() -> {
                ///launch the job
                job = launch {
                    val request = APIRequest().apply {
                        requestType = "POST"
                        params = hashMapOf(
                            "api_key" to BuildConfig.FlickrApiKey,
                            "method" to "flickr.photos.search",
                            "text" to term,
                            "format" to "json",
                            "per_page" to "$perPage",
                            "nojsoncallback" to "1"
                        )
                    }

                    val result = APIHandler.apiCall(request, FlickrJsonAdapter())

                    // make sure the job wasn't cancelled. if so, we need not send the previous result
                    if (!job!!.isCancelled) {
                        when (result) {
                            is APIResult.Success -> {
                                flickrImagesLiveData.postValue(result.data as ImageSearchResponse)
                            }
                            is APIResult.Error -> {
                                errorHandler.postValue(result.exception.message ?: "An error has occurred.")
                            }
                        }
                    }
                }
            }
            else -> flickrImagesLiveData.postValue(null)
        }
    }


    // cancels coroutine scope and all children
    fun cancelRequest() = cancel()
}