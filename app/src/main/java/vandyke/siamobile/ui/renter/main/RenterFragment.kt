/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.renter.main

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import kotlinx.android.synthetic.main.fragment_renter.*
import vandyke.siamobile.R
import vandyke.siamobile.ui.common.BaseFragment
import vandyke.siamobile.ui.renter.files.view.FilesFragment

class RenterFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_renter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pager.adapter = Adapter(fragmentManager!!)

        /* update pager when navigation bar item is selected */
        renterNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_renter_files -> {
                    pager.currentItem = 0
                    true
                }
                else -> false
            }
        }

        /* update navigation bar selected item when pager is swiped */
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        renterNavigation.selectedItemId = R.id.navigation_renter_files
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })
    }

    inner class Adapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        val fragments = listOf(FilesFragment())

        override fun getItem(position: Int) = fragments[position]

        override fun getCount() = fragments.size
    }
}