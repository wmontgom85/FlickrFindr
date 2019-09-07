package com.wmontgom85.flickrfindr

import android.net.Uri
import com.wmontgom85.flickrfindr.api.APIRequest
import org.junit.AfterClass
import org.junit.Test

import org.junit.Assert.*
import org.junit.BeforeClass

/**
 * Checks that an APIRequest can be successfully built based on default params
 */
class ApiRequestUnitTest {
    @Test
    fun api_request_can_build() {
        val request = APIRequest().apply {
            requestType = "POST"
            params = hashMapOf(
                "api_key" to BuildConfig.FlickrApiKey,
                "method" to "flickr.photos.search",
                "format" to "json"
            )
        }
        
        assertEquals("POST", request.requestType)
        assertEquals("https://api.flickr.com/services/rest/", request.restUrl)
    }
}
