package com.wmontgom85.flickrfindr.ui.fragment

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.squareup.picasso.Picasso
import com.wmontgom85.flickrfindr.R
import com.wmontgom85.flickrfindr.repo.model.FlickrImage
import com.wmontgom85.flickrfindr.supp.debounce
import com.wmontgom85.flickrfindr.supp.inflate
import com.wmontgom85.flickrfindr.supp.throttleFirst
import com.wmontgom85.flickrfindr.ui.activity.ImageViewActivity
import com.wmontgom85.flickrfindr.viewmodel.FlickrSearchViewModel
import kotlinx.android.synthetic.main.fragment_flickr_search.*
import kotlinx.android.synthetic.main.fragment_flickr_search.view.*
import kotlinx.coroutines.MainScope

/**
 * A placeholder fragment containing a simple view.
 */
class FlickrSearchFragment : Fragment() {
    val FAVORITED_IMAGE_RESULT = 100

    private var listener: FavoritesFragment.OnFragmentInteractionListener? = null

    private lateinit var flickrSearchViewModel: FlickrSearchViewModel

    private lateinit var adapter : SearchImageAdapter

    private lateinit var imageList : RecyclerView

    private var images : List<FlickrImage>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flickrSearchViewModel = ViewModelProvider(this).get(FlickrSearchViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_flickr_search, container, false)

        imageList = root.images_list

        val layoutManager = FlexboxLayoutManager(activity)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.SPACE_AROUND

        imageList.layoutManager = layoutManager

        adapter = SearchImageAdapter()
        imageList.adapter = adapter

        val onTextChange: (String) -> Unit = debounce(300L, MainScope(), this::performSearch)
        root.search_input.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                loading.visibility = View.VISIBLE

                // allow empty strings to go through so we can cancel any previous requests
                onTextChange(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean = true
        })

        // create the live data observer
        flickrSearchViewModel.flickrImagesLiveData.observe(this, Observer {
            it?.let {
                images = it.photos // set the images list to the response images
            } ?: run {
                images = null
            }

            refreshList()
        })

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FavoritesFragment.OnFragmentInteractionListener) {
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
        flickrSearchViewModel.cancelRequest()
    }

    /**
     * Fire off the image search
     */
    private fun performSearch(term: String) {
        flickrSearchViewModel.performSearch(term)
    }

    /**
     * Refreshes the images list with the new data
     */
    private fun refreshList() {
        loading.visibility = View.GONE
        adapter.notifyDataSetChanged()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        // fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(): FlickrSearchFragment {
            return FlickrSearchFragment()
        }
    }

    /**
     * Image Search RecyclerView Adapter
     */
    inner class SearchImageAdapter : RecyclerView.Adapter<SearchImageViewHolder>() {
        override fun getItemCount(): Int {
            return images?.size ?: 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchImageViewHolder {
            val inflatedView = parent.inflate(R.layout.view_holder_image, false)
            return SearchImageViewHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: SearchImageViewHolder, position: Int) {
            val p = images?.get(position)

            p.let {
                holder.image = it

                // create launch function for click action
                val cb = fun(v: View) {
                    var i : Intent = Intent(activity, ImageViewActivity::class.java)
                    i.putExtra("image", holder.image)
                    val options = ActivityOptions.makeSceneTransitionAnimation(activity, holder.imageview,
                        "image_to_full_transition")
                    activity?.startActivityForResult(i, FAVORITED_IMAGE_RESULT, options.toBundle())
                }

                val menuAction: (View) -> Unit = throttleFirst(1000L, MainScope(), cb)
                holder.imageview.setOnClickListener(menuAction) // bind click action to avatar

                holder.populate()
            }
        }
    }

    /**
     * PersonHolder class
     */
    inner class SearchImageViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var image : FlickrImage? = null
        val imageview : ImageView = v.findViewById(R.id.image_thumb)
        val name : TextView = v.findViewById(R.id.name)

        fun populate() {
            image?.let {
                Picasso.get()
                    .load(it.getThumbnail())
                    .placeholder(R.mipmap.default_image)
                    .error(R.mipmap.default_image)
                    .into(imageview)
                name.text = it.title
            } ?: run {
                imageview.setImageResource(R.mipmap.default_image)
                name.text = ""
            }
        }
    }
}