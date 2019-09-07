package com.wmontgom85.flickrfindr.ui.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.wmontgom85.flickrfindr.BuildConfig
import com.wmontgom85.flickrfindr.R
import com.wmontgom85.flickrfindr.api.APIRequest
import com.wmontgom85.flickrfindr.supp.hideKeyboard
import com.wmontgom85.flickrfindr.ui.fragment.FavoritesFragment
import com.wmontgom85.flickrfindr.ui.fragment.FlickrSearchFragment


class MainActivity : AppCompatActivity(),
    FavoritesFragment.OnFragmentInteractionListener,
    FlickrSearchFragment.OnFragmentInteractionListener
{
    private lateinit var sectionsPagerAdapter : SectionsPagerAdapter

    private val TAB_TITLES = arrayOf(
        R.string.search_tab,
        R.string.fav_tab
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                viewPager.hideKeyboard()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                viewPager.hideKeyboard()
            }

            override fun onPageSelected(position: Int) {
                viewPager.hideKeyboard()
            }
        })

        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    /**
     * Users to signal a reload needs to occur with the favorites fragment
     */
    override fun reloadFavorites() {
        supportFragmentManager.fragments.forEach {
            if (it is FavoritesFragment) it.reloadImages()
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
    {
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
}
