package com.wmontgom85.flickrfindr.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.wmontgom85.flickrfindr.R
import com.wmontgom85.flickrfindr.ui.fragment.FavoritesFragment
import com.wmontgom85.flickrfindr.ui.fragment.FlickrSearchFragment
import com.wmontgom85.flickrfindr.ui.pagers.SectionsPagerAdapter

class MainActivity : AppCompatActivity(),
    FavoritesFragment.OnFragmentInteractionListener,
    FlickrSearchFragment.OnFragmentInteractionListener
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter

        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }
}
