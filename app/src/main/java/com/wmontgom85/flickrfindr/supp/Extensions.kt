package com.wmontgom85.flickrfindr.supp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import android.graphics.Bitmap


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

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Blurs a bitmap
 * @return Bitmap
 */
fun Bitmap.blurTo(scale: Int) : Bitmap {
    val newImage = Bitmap.createScaledBitmap(this, width/scale, height/scale, false)

    return when (newImage.width > newImage.height) {
        true -> newImage.centercrop(newImage.width/2)
        false -> newImage.centercrop(newImage.height/2)
    }
}


/**
 * Builds a thumbnail version of the large image
 * @return Bitmap
 */
fun Bitmap.centercrop(imgDimen : Int) : Bitmap {
    if (width > imgDimen || height > imgDimen) {
        var newW = imgDimen
        var newH = imgDimen
        var newX = 0
        var newY = 0

        // thumbnail should be sized according to original dimensions and centercropped
        when {
            (width > height) -> {
                newW = ((width.toDouble() / height.toDouble()) * imgDimen).toInt()
                newX = (newW - newH) / 2
            }
            (width < height) -> {
                newH = ((height.toDouble() / width.toDouble()) * imgDimen).toInt()
                newY = (newH - newW) / 2
            }
        }

        // don't waste time scaling if the image is already square
        val scaledBM = when {
            newW != newH -> Bitmap.createScaledBitmap(this, newW, newH, false)
            else -> this
        }

        return Bitmap.createBitmap(scaledBM, newX, newY, imgDimen, imgDimen)
    } else {
        // the image is already smaller than the provided dimension
        return this
    }
}