package com.wmontgom85.flickrfindr.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wmontgom85.flickrfindr.BuildConfig
import com.wmontgom85.flickrfindr.api.APIHandler
import com.wmontgom85.flickrfindr.api.APIRequest
import com.wmontgom85.flickrfindr.api.RequestType
import com.wmontgom85.flickrfindr.api.jsonadapter.FlickrJsonAdapter
import com.wmontgom85.flickrfindr.api.response.ImageSearchResponse
import com.wmontgom85.flickrfindr.sealed.Result
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class FlickrSearchViewModel : ViewModel() {
    private val parentJob = Job()

    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.IO

    private val scope = CoroutineScope(coroutineContext)

    // live data that will be populated as search results are returned
    val flickrImagesLiveData = MutableLiveData<ImageSearchResponse>()

    // error handler that will post errors that occur
    val errorHandler = MutableLiveData<String>()

    @Suppress("UNCHECKED_CAST")
    fun performSearch(term : String, perPage: Int = 25, currentPage: Int = 1) {
        // cancel any previous requests so we don't stack up request processing
        parentJob.cancelChildren()

        when {
            term.isNotBlank() -> {
                ///launch the job
                scope.launch {
                    val request = APIRequest().apply {
                        requestType = RequestType.POST
                        params = hashMapOf(
                            "api_key" to BuildConfig.FlickrApiKey,
                            "method" to "flickr.photos.search",
                            "text" to term,
                            "format" to "json",
                            "per_page" to "$perPage",
                            "page" to "$currentPage",
                            "nojsoncallback" to "1"
                        )
                    }

                    val result = APIHandler.apiCall(request, FlickrJsonAdapter())

                    // make sure the job wasn't cancelled. if so, we need not send the previous result
                    if (isActive) {
                        when (result) {
                            is Result.Success -> {
                                flickrImagesLiveData.postValue(result.data as ImageSearchResponse)
                            }
                            is Result.Error -> {
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
    fun cancelRequest() = scope.cancel()
}