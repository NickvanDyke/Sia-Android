/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.main

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.renter.allowance.RenterAllowanceFragment
import com.vandyke.sia.ui.renter.files.view.FilesFragment
import kotlinx.android.synthetic.main.fragment_renter.*

class RenterFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_renter

    lateinit var adapter: RenterFragmentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = RenterFragmentAdapter(childFragmentManager)
        pager.adapter = adapter

        /* update pager when navigation bar item is selected */
        renterNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_renter_files -> {
                    pager.currentItem = 0
                    true
                }
                R.id.navigation_renter_allowance -> {
                    pager.currentItem = 1
                    true
                }
                else -> false
            }
        }

        /* update navigation bar selected item when pager is swiped */
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                renterNavigation.selectedItemId = when (position) {
                    0 -> R.id.navigation_renter_files
                    1 -> R.id.navigation_renter_allowance
                    else -> throw Exception()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })
    }

    override fun onBackPressed(): Boolean {
        return adapter.getActiveFragment().onBackPressed()
    }

    inner class RenterFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        val fragments = listOf<BaseFragment>(FilesFragment(), RenterAllowanceFragment())

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size

        fun getActiveFragment() = fragments[pager.currentItem]
    }
}