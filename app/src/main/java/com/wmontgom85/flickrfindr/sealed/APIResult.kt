package com.wmontgom85.flickrfindr.sealed

sealed class APIResult<out T: Any> {
    data class Success<out T : Any>(val data: Any) : APIResult<T>()
    data class Error(val exception: Exception) : APIResult<Nothing>()
}