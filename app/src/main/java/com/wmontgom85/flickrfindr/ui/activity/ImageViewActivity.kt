package com.wmontgom85.flickrfindr.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.wmontgom85.flickrfindr.R
import com.wmontgom85.flickrfindr.repo.model.FlickrImage

import kotlinx.android.synthetic.main.activity_image_view.*
import kotlinx.android.synthetic.main.content_image_view.*
import java.lang.Exception


class ImageViewActivity : AppCompatActivity() {
    private lateinit var image : FlickrImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        setSupportActionBar(toolbar)

        try {
            image = intent.getSerializableExtra("image") as FlickrImage

            // show the loading indicator
            loading_image.visibility = View.VISIBLE

            
            // load the iamge using picasso
            Picasso.get()
                .load(image.getLargeImage())
                .placeholder(R.mipmap.default_image)
                .error(R.mipmap.default_image)
                .into(full_image, object: Callback { // once the image is loaded, we can hide the loading indicator
                    override fun onSuccess() {
                        loading_image.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        loading_image.visibility = View.GONE
                    }
                })

            // enable the home button in the action bar and set the title
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                it.title = image.title
            }

            // create the click listener for the fav fab button
            fav_image.setOnClickListener { view ->
                toggleFav()
            }

        } catch (tx: Throwable) {
            // something bad happened. abandon
            Log.d("ImageViewActivity", "message: ${tx.message}")
            finish()
        }
    }

    private fun toggleFav() {

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
}
