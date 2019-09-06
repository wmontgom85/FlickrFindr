package com.wmontgom85.flickrfindr.api.response

import com.wmontgom85.flickrfindr.repo.model.FlickrImage

data class ImageSearchResponse(
    val page: Int,
    val pages: Int,
    val perPage: Int,
    val total: Long,
    val photos: ArrayList<FlickrImage>
)