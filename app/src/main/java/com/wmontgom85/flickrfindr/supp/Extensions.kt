package com.wmontgom85.flickrfindr.supp

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

internal fun Int.dp(): Int {
    return (this / Resources.getSystem().displayMetrics.density).toInt()
}

internal fun Int.px(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}