package com.wmontgom85.flickrfindr.api

import com.wmontgom85.flickrfindr.api.jsonadapter.Parser
import com.wmontgom85.flickrfindr.sealed.Result

object APIHandler {
    /**
     * Makes the API call and returns the Result
     * @return Result<Any>
     */
    fun apiCall(request: APIRequest, parser: Parser): Result<Any> {
        val result: Result<Any> = APIConnection(request).makeRequest()

        when (result) {
            is Result.Success -> {
                try {
                    // attempt parsing and set data as object
                    parser.readFrom(result.data as String)?.let {
                        return Result.Success(it)
                    } ?: run {
                        // parsing error
                        return Result.Error(Exception("An error has occurred while parsing. Error code AH001"))
                    }
                } catch (tx : Throwable) {
                    tx.printStackTrace()

                    // fatal error
                    return Result.Error(Exception("An error has occurred while parsing. Error code AH002"))
                }
            }
        }

        return result
    }
}