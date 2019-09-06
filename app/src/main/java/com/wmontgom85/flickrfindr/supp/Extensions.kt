package com.wmontgom85.flickrfindr.supp

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Int.px(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun Activity.showMessage(title: String, message: String) {
    val builder = AlertDialog.Builder(this)

    builder.setTitle(title)
    builder.setMessage(message)
    builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

    val dialog: AlertDialog = builder.create()

    dialog.show()
}
