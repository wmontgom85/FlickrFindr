package com.wmontgom85.flickrfindr.ui.activity

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.wmontgom85.flickrfindr.R
import com.wmontgom85.flickrfindr.repo.model.FlickrImage
import com.wmontgom85.flickrfindr.supp.throttleFirst
import com.wmontgom85.flickrfindr.viewmodel.FlickrImageViewModel

import kotlinx.android.synthetic.main.activity_image_view.*
import kotlinx.android.synthetic.main.content_image_view.*
import kotlinx.coroutines.MainScope

class ImageViewActivity : AppCompatActivity() {
    private lateinit var image : FlickrImage

    private var imageIsChecked = false
    private var imageIsLoaded = false
    private var isFavorited = false
    private var favoriteStatusChanged = false

    private lateinit var imageViewModel: FlickrImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        setSupportActionBar(toolbar)

        imageViewModel = ViewModelProvider(this).get(FlickrImageViewModel::class.java)

        try {
            image = intent.getSerializableExtra("image") as FlickrImage

            // show the loading indicator
            loading_image.visibility = View.VISIBLE

            // create the observables
            createObservations()

            // callback for image processing completion
            val imageCB = {
                imageIsLoaded = true
                if (imageIsChecked)
                    loading_image.visibility = View.GONE
            }

            // load the iamge using glide
            Glide.with(this)
                .asBitmap()
                .load(image.getLargeImage())
                .centerInside()
                .listener(object: RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("Glide", "message: ${e?.message}")
                        imageCB()
                        showMessage("Oh no!", "An error occurred while loading this image.")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        image.setImageBytes(resource)
                        imageCB()
                        return false
                    }
                })
                .into(full_image)

            // image.setImageBytes(bitmap)

            // enable the home button in the action bar and set the title
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.title = image.title
            }

            // create the click listener for the fav fab button, throttled of course
            val favAction: (View) -> Unit = throttleFirst(600, MainScope(), this::toggleFav)
            fav_image.setOnClickListener(favAction) // bind click action to avatar

            imageViewModel.getImageIsFavorited(image.id)
        } catch (tx: Throwable) {
            // something bad happened. abandon
            Log.d("ImageViewActivity", "message: ${tx.message}")
            finish()
        }
    }

    /**
     * Fires up our observers
     */
    private fun createObservations() {
        // observes checks against whether the image has been favorited
        imageViewModel.imageIsFavorited.observe(this, Observer {
            isFavorited = it
            when (it) {
                true -> moveFabToOn() // toggle the fav button to an "on" state
                else -> moveFabToOff() // toggle the fav button to an "off" state
            }

            imageIsChecked = true
            if (imageIsLoaded) // if the image is still loading, we don't want to hide the loading indicator just yet
                loading_image.visibility = View.GONE
        })

        // observes the favoriting action result
        imageViewModel.imageFavResult.observe(this, Observer {
            isFavorited = it
            if (it) {
                moveFabToOn() // only toggle the fab on if the action succeeded
                favoriteStatusChanged = !favoriteStatusChanged;
            }
            loading_image.visibility = View.GONE
        })

        // observes the unfavoriting action result
        imageViewModel.imageUnfavResult.observe(this, Observer {
            isFavorited = !it

            if (it) {
                moveFabToOff() // only toggle the fab on if the action succeeded
                favoriteStatusChanged = !favoriteStatusChanged;
            }
            loading_image.visibility = View.GONE
        })

        // observes errors in actions
        imageViewModel.errorHandler.observe(this, Observer {
            loading_image.visibility = View.GONE
            showMessage("Whoops!", it)
        })
    }

    private fun toggleFav(v: View) {
        loading_image.visibility = View.VISIBLE

        when (isFavorited) {
            true -> imageViewModel.unfavoriteImage(image)
            else -> imageViewModel.favoriteImage(image)
        }
    }

    /**
     * Toggle the FAB on
     */
    private fun moveFabToOn() {
        fav_image.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        fav_image.setImageResource(R.drawable.ic_fav_filled)
    }

    /**
     * Toggles the FAB off
     */
    private fun moveFabToOff() {
        fav_image.backgroundTintList = null
        fav_image.setImageResource(R.drawable.ic_fav)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // shows a message to the user
    private fun showMessage(title: String, message: String) {
        val builder = AlertDialog.Builder(this@ImageViewActivity)

        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        // if the image fav status was changed, we need to signal the main activity to update the favorites list
        if (favoriteStatusChanged) {

        }
    }
}
