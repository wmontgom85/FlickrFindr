package com.wmontgom85.flickrfindr.sealed

sealed class Result<out T: Any> {
    data class Success<out T : Any>(val data: Any) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}