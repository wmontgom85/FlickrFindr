package com.wmontgom85.flickrfindr.ui.pagers

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.wmontgom85.flickrfindr.R
import com.wmontgom85.flickrfindr.ui.fragment.FavoritesFragment
import com.wmontgom85.flickrfindr.ui.fragment.FlickrSearchFragment

private val TAB_TITLES = arrayOf(
    R.string.search_tab,
    R.string.fav_tab
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> FlickrSearchFragment.newInstance()
            else -> FavoritesFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}