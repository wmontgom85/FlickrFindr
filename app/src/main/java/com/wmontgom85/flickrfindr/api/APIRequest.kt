package com.wmontgom85.flickrfindr.api

import android.net.Uri

/**
 * Class for building API Requests
 */
class APIRequest {
    val restUrl : String by lazy { "https://api.flickr.com/services/rest/" }

    var requestType : String = "GET"

    var params : HashMap<String, String>? = null

    var timeout : Int = 15000

    fun buildQuery() : String? {
        try {
            var builder = Uri.Builder()

            params?.let {
                it.forEach {
                    builder.appendQueryParameter(it.key, it.value)
                }
            }

            return builder.build().encodedQuery
        } catch (tx: Throwable) {}

        return null
    }
}