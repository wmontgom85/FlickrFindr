package com.wmontgom85.flickrfindr.ui.fragment

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.wmontgom85.flickrfindr.R
import com.wmontgom85.flickrfindr.api.response.ImageSearchResponse
import com.wmontgom85.flickrfindr.repo.model.FlickrImage
import com.wmontgom85.flickrfindr.supp.*
import com.wmontgom85.flickrfindr.ui.activity.ImageViewActivity
import com.wmontgom85.flickrfindr.viewmodel.FlickrSearchViewModel
import kotlinx.android.synthetic.main.fragment_flickr_search.*
import kotlinx.android.synthetic.main.fragment_flickr_search.view.*
import kotlinx.coroutines.MainScope
import java.text.DecimalFormat

/**
 * A placeholder fragment containing a simple view.
 */
class FlickrSearchFragment : Fragment(), NumberPicker.OnValueChangeListener {
    val FAVORITED_IMAGE_RESULT = 100

    private var listener: OnFragmentInteractionListener? = null

    private lateinit var flickrSearchViewModel: FlickrSearchViewModel

    private lateinit var adapter : SearchImageAdapter
    private lateinit var suggestAdapter : ArrayAdapter<String>

    private lateinit var imageList : RecyclerView
    private lateinit var searchInput : AutoCompleteTextView

    private lateinit var suggestions : HashSet<String>

    private var loadRequested = false

    // the list of images from search
    private var images : List<FlickrImage>? = null

    // pagination controls
    private var itemsPerPage = 25
    private var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flickrSearchViewModel = ViewModelProvider(this).get(FlickrSearchViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_flickr_search, container, false)

        imageList = root.images_list
        searchInput = root.search_input

        val layoutManager = FlexboxLayoutManager(activity)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.SPACE_AROUND
        imageList.layoutManager = layoutManager

        // create the image adapter
        adapter = SearchImageAdapter()
        imageList.adapter = adapter

        // build search suggestions
        buildSearchSuggestions()

        // create the live data observer
        flickrSearchViewModel.flickrImagesLiveData.observe(this, Observer {
            refreshList(it)
        })

        // observe errors
        flickrSearchViewModel.errorHandler.observe(this, Observer {
            loading.visibility = View.GONE
            activity?.showMessage("Whoops!", it)
        })

        // listen to search query changes
        val onTextChange: (String) -> Unit = debounce(750L, MainScope(), this::performSearch)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // throttle the search submission
                onTextChange(s.toString())
            }
        })

        // per page listener
        val onPerPageSelected: (View) -> Unit = throttleFirst(500L, MainScope(), this::changePerPage)
        root.per_page.setOnClickListener(onPerPageSelected)

        // next page listener
        val nextPageSelected: (View) -> Unit = throttleFirst(500L, MainScope(), this::nextPage)
        root.next_page.setOnClickListener(nextPageSelected)

        // per page listener
        val prevPageSelected: (View) -> Unit = throttleFirst(500L, MainScope(), this::prevPage)
        root.prev_page.setOnClickListener(prevPageSelected)

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
        flickrSearchViewModel.cancelRequest()
    }

    /**
     * Builds the search suggestion set
     */
    private fun buildSearchSuggestions() {
        val preferences = activity?.getSharedPreferences("search_suggestions", Context.MODE_PRIVATE)
        suggestions = preferences?.getStringSet("suggestions", HashSet<String>()) as HashSet
        updateSuggestionAdapter()
    }

    /**
     * Resets the search suggestion adaptoins
     */
    private fun updateSuggestionAdapter() {
        context?.let {
            suggestAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, suggestions.toList())
            searchInput.setAdapter(suggestAdapter)
        }
    }

    /**
     * Stored search history in shared prefs
     */
    private fun storeSuggestion(suggestion: String) {
        if (suggestion.isBlank()) return

        // make sure it's not already in the suggestion list
        if (!suggestions.contains(suggestion)) {
            suggestions.add(suggestion)
            if (suggestions.size > 20) {
                // remove the last entry
                suggestions.remove(suggestions.last())
            }
            val preferences = activity?.getSharedPreferences("search_suggestions", Context.MODE_PRIVATE)
            preferences?.edit()?.let {
                it.putStringSet("suggestions", suggestions)
                it.commit()
            }

            updateSuggestionAdapter()
        }
    }

    /**
     * Updates the per page count
     */
    private fun changePerPage(v : View) {
        activity?.let {
            val numberPicker = NumberPickerDialog()
            numberPicker.valueChangeListener = this
            numberPicker.show(it.supportFragmentManager, "per_page_picker")
        }
    }

    /**
     * Sends the user to the next page
     */
    private fun nextPage(v : View) {
        loading.visibility = View.VISIBLE
        ++currentPage
        search(searchInput.text.toString())
    }

    /**
     * Sends the user to the previous page
     */
    private fun prevPage(v : View) {
        loading.visibility = View.VISIBLE
        --currentPage
        search(searchInput.text.toString())
    }

    /**
     * Value change listener for per page selection
     */
    override fun onValueChange(p0: NumberPicker?, p1: Int, p2: Int) {
        val newPerPage = when (p1) {
            0 -> 10
            1 -> 25
            2 -> 50
            else -> 100
        }

        if (newPerPage != itemsPerPage) {
            // per page updated. trigger query
            itemsPerPage = newPerPage
            per_page.text = "$newPerPage"

            if (searchInput.text.isNotBlank()) {
                performSearch(searchInput.text.toString())
            }
        }
    }

    /**
     * Fire off the image search
     */
    private fun performSearch(term: String) {
        loading.visibility = View.VISIBLE

        // user triggered a new search. reset the page number.
        currentPage = 1

        // call search
        search(term)
    }

    private fun search(term: String) {
        flickrSearchViewModel.performSearch(term, itemsPerPage, currentPage)
    }

    /**
     * Refreshes the images list with the new data
     */
    private fun refreshList(imageResponse : ImageSearchResponse?) {
        imageResponse?.let {
            images = it.photos // set the images list to the response images
            image_total.text = when (it.total) {
                1L -> "1 result"
                else -> {
                    val formatter = DecimalFormat("#,###,###");
                    "${formatter.format(it.total)} results"
                }
            }
            image_total.visibility = View.VISIBLE

            when {
                it.pages > 0 -> {
                    when (it.page > 1) {
                        true -> prev_page.visibility = View.VISIBLE
                        else -> prev_page.visibility = View.INVISIBLE
                    }
                    when (it.page < it.pages) {
                        true -> next_page.visibility = View.VISIBLE
                        else -> next_page.visibility = View.INVISIBLE
                    }
                    page_num.text = "Page ${it.page} of ${it.pages}"
                    pagination.visibility = View.VISIBLE
                }
                else -> pagination.visibility = View.GONE
            }

            // save the term as a future suggestion
            storeSuggestion(searchInput.text.toString())
        } ?: run {
            images = null
            image_total.text = "0 results"
            image_total.visibility = View.INVISIBLE
            pagination.visibility = View.GONE
        }

        loading.visibility = View.INVISIBLE
        adapter.notifyDataSetChanged()
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnFragmentInteractionListener {
        fun reloadFavorites()
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
                val cb = fun(_ : View) {
                    // if the user taps two cards at once, it could potentially load both, causing
                    // the transition back to fail and a blank card to be seen checking if
                    // a load was requeste first before loading the image guarantees we're only
                    // loading once, even when two cards are tapped
                    if (!loadRequested) {
                        loadRequested = true

                        if (loading.visibility == View.VISIBLE) {
                            // we're loading an image already. halt execution
                            return
                        }

                        loading.visibility = View.VISIBLE

                        // attempt image preload
                        preloadThroughGlide(holder)
                    }
                }

                // throttle card clicks
                val cardClick: (View) -> Unit = throttleFirst(1000L, MainScope(), cb)
                holder.itemView.setOnClickListener(cardClick) // bind click action to avatar

                holder.populate()
            }
        }
    }

    /**
     * Attempts to preload an image through Glide so that it can be displayed more quickly when tapping
     * an image card
     */
    private fun preloadThroughGlide(holder: SearchImageViewHolder) {
        // attempt preload of large image for quicker rendering
        Glide.with(holder.imageview)
            .load(holder.image?.getLargeImage())
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loading.visibility = View.INVISIBLE

                    activity?.showMessage("Whoops!", "The image failed to load. Please try again")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // image is now preloaded and cached through Glide. send user to activity
                    val i = Intent(activity, ImageViewActivity::class.java)
                    i.putExtra("image", holder.image)
                    val options = ActivityOptions.makeSceneTransitionAnimation(activity, holder.imageview,
                        "image_to_full_transition")
                    startActivityForResult(i, FAVORITED_IMAGE_RESULT, options.toBundle())

                    loading.visibility = View.INVISIBLE

                    return true
                }
            }).preload()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        loadRequested = false

        when (requestCode) {
            FAVORITED_IMAGE_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                        if (it.getBooleanExtra("status_changed", false)) {
                            listener?.reloadFavorites()
                        }
                    }
                }
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
                Glide.with(imageview)
                    .asBitmap()
                    .load(it.getThumbnail())
                    .centerCrop()
                    .placeholder(R.mipmap.default_thumb)
                    .error(R.mipmap.default_thumb)
                    .fallback(R.mipmap.default_thumb)
                    .into(imageview)

                name.text = it.title
            } ?: run {
                imageview.setImageResource(R.mipmap.default_thumb)
                name.text = ""
            }
        }
    }
}