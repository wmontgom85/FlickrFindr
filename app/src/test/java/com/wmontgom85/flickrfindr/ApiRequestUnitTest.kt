package com.wmontgom85.flickrfindr

import com.wmontgom85.flickrfindr.api.APIRequest
import com.wmontgom85.flickrfindr.api.RequestType
import org.junit.Test

import org.junit.Assert.*

/**
 * Checks that an APIRequest can be successfully built based on default params
 */
class ApiRequestUnitTest {
    @Test
    fun api_request_can_build() {
        val request = APIRequest().apply {
            requestType = RequestType.POST
            params = hashMapOf(
                "api_key" to BuildConfig.FlickrApiKey,
                "method" to "flickr.photos.search",
                "format" to "json"
            )
        }
        
        assertEquals("POST", request.requestType.value)
        assertEquals(RequestType.POST, request.requestType)
        assertEquals("https://api.flickr.com/services/rest/", request.restUrl)
    }
}
