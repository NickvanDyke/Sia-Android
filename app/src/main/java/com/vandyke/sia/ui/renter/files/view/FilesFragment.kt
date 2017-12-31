/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import com.vandyke.sia.R
import com.vandyke.sia.data.local.data.renter.Dir
import com.vandyke.sia.data.local.data.renter.File
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.renter.files.view.list.NodesAdapter
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.observe
import kotlinx.android.synthetic.main.fragment_renter_files.*


class FilesFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_renter_files

    lateinit var viewModel: FilesViewModel

    private lateinit var adapter: NodesAdapter
    private var programmaticallySelecting = true

    /** tracked in the view so that when the viewmodel's path updates, we can determine the differences for animating the path display */
    private var currentPath: List<String> = listOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(FilesViewModel::class.java)
        filesList.addItemDecoration(DividerItemDecoration(filesList.context, (filesList.layoutManager as LinearLayoutManager).orientation))
        adapter = NodesAdapter(viewModel)
        filesList.adapter = adapter

        renterFilepath.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (!programmaticallySelecting) {
                    viewModel.goToIndexInPath(tab.position)
                }
            }
        })

        renterSwipeRefresh.setColorSchemeResources(R.color.colorAccent)
        renterSwipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            renterSwipeRefresh.isRefreshing = false
        }

        /* FAB stuff */
        fabAddDir.setOnClickListener {
            fabMenu.collapse()
            val dialogView = layoutInflater.inflate(R.layout.fragment_renter_add_dir, null, false)
            val dialog = AlertDialog.Builder(context!!)
                    .setTitle("New directory")
                    .setView(dialogView)
                    .setPositiveButton("Create", { dialogInterface, i ->
                        viewModel.createNewDir(dialogView.findViewById<EditText>(R.id.newDirName).text.toString())
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
            dialog.show()
            dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        fabAddFile.setOnClickListener {
            // TODO
        }

        /* observe viewModel stuff */
        viewModel.displayedNodes.observe(this) {
            adapter.display(it)
        }

        viewModel.path.observe(this) {
            programmaticallySelecting = true
            val newPath = it.split("/")
            /* find the point at which the path differs */
            val breakpoint = (0 until maxOf(newPath.size, currentPath.size)).firstOrNull {
                it > currentPath.size - 1 || it > newPath.size - 1 || newPath[it] != currentPath[it]
            } ?: renterFilepath.tabCount
            /* select the appropriate tab if it already exists */
            if (newPath.size < currentPath.size)
                renterFilepath.getTabAt(newPath.size - 1)?.select()
            /* remove tabs that are past the breakpoint */
            for (i in breakpoint until renterFilepath.tabCount)
                renterFilepath.removeTabAt(breakpoint)
            /* add new tabs from the breakpoint to the end of the new path */
            for (i in breakpoint until newPath.size)
                renterFilepath.addTab(renterFilepath.newTab().setText(newPath[i]), true)
            /* set the first tab's text to Home, since otherwise it will just be a space due to the paths of stuff starting with a slash */
            renterFilepath.getTabAt(0)!!.text = "Home"
            /* scroll the tabs all the way to the right */
            renterFilepath.postDelayed({ renterFilepath.fullScroll(TabLayout.FOCUS_RIGHT) }, 5)
            programmaticallySelecting = false
            currentPath = newPath
        }

        viewModel.error.observe(this) {
            it.snackbar(coordinator) // TODO: make FAB move up when snackbar appears
            renterSwipeRefresh.isRefreshing = false
        }

        viewModel.detailsItem.observe(this) {
            if (it is Dir)
                DirBottomSheetFragment().show(childFragmentManager, null)
            else if (it is File)
                FileBottomSheetFragment().show(childFragmentManager, null)
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
        viewModel.refresh()
    }

    companion object {
        val ROOT_DIR_NAME = "/"
    }
}
