package com.wmontgom85.flickrfindr.ui.fragment

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

import com.wmontgom85.flickrfindr.R
import com.wmontgom85.flickrfindr.repo.model.FlickrImage
import com.wmontgom85.flickrfindr.supp.inflate
import com.wmontgom85.flickrfindr.supp.throttleFirst
import com.wmontgom85.flickrfindr.ui.activity.ImageViewActivity
import com.wmontgom85.flickrfindr.viewmodel.FavoritesViewModel
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.view.*
import kotlinx.coroutines.MainScope

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FavoritesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class FavoritesFragment : Fragment() {
    val UNFAVORITED_IMAGE_RESULT = 100

    private var listener: OnFragmentInteractionListener? = null

    private lateinit var favViewModel: FavoritesViewModel

    private lateinit var adapter : FavImageAdapter

    private lateinit var imageList : RecyclerView

    private var favImages : List<FlickrImage>? = null

    private var loadRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favViewModel = ViewModelProvider(this).get(FavoritesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_favorites, container, false)

        imageList = root.fav_images_list

        val layoutManager = FlexboxLayoutManager(activity)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.SPACE_AROUND

        imageList.layoutManager = layoutManager

        adapter = FavImageAdapter()
        imageList.adapter = adapter

        // create the live data observer
        favViewModel.favImagesLiveData.observe(this, Observer {
            favImages = it
            refreshList()
        })

        favViewModel.getImages()

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FavoritesFragment.OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroy() {
        super.onDestroy()

        // cancel the coroutine scope
        favViewModel.cancelRequest()
    }

    /**
     * Reloads the images RecyclerView
     */
    fun reloadImages() {
        favViewModel.getImages()
    }

    /**
     * Updates images after unfavoriting so that the autolayout animation can occur
     */
    fun updateImages(position : Int, imageId : String?) {
        favImages = favImages?.filter { image -> image.id != imageId }
        when (position > -1) {
            true -> adapter.notifyItemRemoved(position)
            else -> adapter.notifyDataSetChanged()
        }
    }

    /**
     * Refreshes the images list with the new data
     */
    private fun refreshList() {
        when (favImages?.isNotEmpty()) {
            true -> empty_favorites.visibility = View.GONE
            else -> empty_favorites.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnFragmentInteractionListener {
        // @TODO put any future communication methods here
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }

    /**
     * Image Search RecyclerView Adapter
     */
    inner class FavImageAdapter : RecyclerView.Adapter<ImageViewHolder>() {
        override fun getItemCount(): Int {
            return favImages?.size ?: 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val inflatedView = parent.inflate(R.layout.view_holder_image, false)
            return ImageViewHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val p = favImages?.get(position)

            p.let {
                holder.image = it

                // create launch function for click action
                val cb = fun(_: View) {
                    // if the user taps two cards at once, it could potentially load both, causing
                    // the transition back to fail and a blank card to be seen checking if
                    // a load was requeste first before loading the image guarantees we're only
                    // loading once, even when two cards are tapped
                    if (!loadRequested) {
                        loadRequested = true

                        val i = Intent(activity, ImageViewActivity::class.java)

                        i.putExtra("image", holder.image)
                        i.putExtra("imagePosition", holder.layoutPosition)
                        val options = ActivityOptions.makeSceneTransitionAnimation(
                            activity, holder.imageview,
                            "image_to_full_transition"
                        )
                        startActivityForResult(i, UNFAVORITED_IMAGE_RESULT, options.toBundle())
                    }
                }

                val menuAction: (View) -> Unit = throttleFirst(1000L, MainScope(), cb)
                holder.imageview.setOnClickListener(menuAction) // bind click action to avatar

                holder.populate()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // user returned from full image view. set load flag to false
        loadRequested = false

        when (requestCode) {
            UNFAVORITED_IMAGE_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                        if (it.getBooleanExtra("status_changed", false)) {
                            if (it.getBooleanExtra("favorited", false)) {
                                reloadImages()
                            } else {
                                updateImages(it.getIntExtra("imagePosition", -1), it.getStringExtra("imageId"))
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * PersonHolder class
     */
    inner class ImageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var image : FlickrImage? = null
        val imageview : ImageView = v.findViewById(R.id.image_thumb)
        val name : TextView = v.findViewById(R.id.name)

        fun populate() {
            image?.let {
                // load from object's stored bytes
                val bm = it.getThumbnailFromLarge()

                bm?.let { bitmap ->
                    imageview.setImageBitmap(bitmap)
                } ?: run {
                    // attempt to get from network
                    Glide.with(imageview)
                        .asBitmap()
                        .load(it.getThumbnail())
                        .centerCrop()
                        .placeholder(R.mipmap.default_thumb)
                        .error(R.mipmap.default_thumb)
                        .fallback(R.mipmap.default_thumb)
                        .into(imageview)
                }

                name.text = it.title
            } ?: run {
                imageview.setImageResource(R.mipmap.default_thumb)
                name.text = ""
            }
        }
    }
}
