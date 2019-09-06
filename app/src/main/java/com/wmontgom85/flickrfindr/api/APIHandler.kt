package com.wmontgom85.flickrfindr.api

import android.util.Log
import com.wmontgom85.flickrfindr.api.jsonadapter.Parser
import com.wmontgom85.flickrfindr.sealed.APIResult
import org.xml.sax.helpers.ParserAdapter

object APIHandler {
    fun apiCall(request: APIRequest, parser: Parser): APIResult<Any> {
        val result: APIResult<Any> = APIConnection(request).makeRequest()

        when (result) {
            is APIResult.Success -> {
                try {
                    // attempt parsing and set data as object
                    parser.readFrom(result.data as String)?.let {
                        return APIResult.Success(it)
                    } ?: run {
                        // parsing error
                        return APIResult.Error(Exception("An error has occurred while parsing. Error code AH001"))
                    }
                } catch (tx : Throwable) {
                    Log.d("1.APIHandler", tx.message)

                    // fatal error
                    return APIResult.Error(Exception("An error has occurred while parsing. Error code AH002"))
                }
            }
        }

        return result
    }
}