/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.fragment_renter.*
import vandyke.siamobile.R
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.ui.BaseFragment
import vandyke.siamobile.ui.renter.view.list.RenterAdapter
import vandyke.siamobile.ui.renter.viewmodel.RenterViewModel
import vandyke.siamobile.util.observe


class RenterFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_renter
    override val hasOptionsMenu = true

    lateinit var viewModel: RenterViewModel

    private var depth = 0
    private lateinit var adapter: RenterAdapter
    private var programmaticallySelecting = true

    var currentDir = SiaDir("home", null)
        set(value) {
            programmaticallySelecting = true
            val oldPath = field.fullPath
            val newPath = value.fullPath
            depth = newPath.size
            val breakpoint = (0 until maxOf(newPath.size, oldPath.size)).firstOrNull {
                it > oldPath.size - 1 || it > newPath.size - 1 || newPath[it] != oldPath[it]
            } ?: renterFilepath.tabCount
            if (newPath.size < oldPath.size)
                renterFilepath.getTabAt(newPath.size - 1)?.select()
            for (i in breakpoint until renterFilepath.tabCount)
                renterFilepath.removeTabAt(breakpoint)
            for (i in breakpoint until newPath.size)
                renterFilepath.addTab(renterFilepath.newTab().setText(newPath[i].name), true)
            renterFilepath.postDelayed({ renterFilepath.fullScroll(TabLayout.FOCUS_RIGHT) }, 5)
            adapter.changeDir(value)
            programmaticallySelecting = false
            field = value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(RenterViewModel::class.java)
//        filesList.addItemDecoration(new DividerItemDecoration(filesList.getContext(), layoutManager.getOrientation()));
        adapter = RenterAdapter(viewModel)
        filesList.adapter = adapter

        renterFilepath.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (!programmaticallySelecting)
                    currentDir = currentDir.getParentDirAt(depth - renterFilepath.selectedTabPosition - 1)
            }
        })

        renterSwipeRefresh.setOnRefreshListener {
            viewModel.refreshFiles()
            renterSwipeRefresh.isRefreshing = false
        }
        renterSwipeRefresh.setColorSchemeResources(R.color.colorAccent)

        /* observe viewModel stuff */
        viewModel.rootDir.observe(this) {

        }

        viewModel.currentDir.observe(this) {
            this.currentDir = it
        }

        viewModel.error.observe(this) {
            it.snackbar(view)
            renterSwipeRefresh.isRefreshing = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed(): Boolean {
        return viewModel.goUpDir()
    }

    override fun onShow() {
        viewModel.refreshFiles()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_renter, menu)
    }
}
