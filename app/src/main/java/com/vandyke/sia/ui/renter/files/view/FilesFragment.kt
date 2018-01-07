/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.EditText
import com.vandyke.sia.R
import com.vandyke.sia.data.local.models.renter.Dir
import com.vandyke.sia.data.models.renter.RenterFileData
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.renter.files.view.list.NodesAdapter
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.observe
import kotlinx.android.synthetic.main.fragment_renter_files.*


class FilesFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_renter_files
    override val hasOptionsMenu: Boolean = true

    lateinit var viewModel: FilesViewModel

    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null
    /** searchItem.isActionViewExpanded() doesn't always return the correct value, so need to keep track ourselves and use that */
    private var searchIsExpanded = false

    private var tabsHeight: Int = 0

    private lateinit var adapter: NodesAdapter
    private var programmatic = true

    /** tracked in the view so that when the viewmodel's path updates, we can determine the differences for animating the path display */
    private var currentPath: List<String> = listOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(FilesViewModel::class.java)
        filesList.addItemDecoration(DividerItemDecoration(filesList.context, (filesList.layoutManager as LinearLayoutManager).orientation))
        adapter = NodesAdapter(viewModel)
        filesList.adapter = adapter

        filepathTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (!programmatic) {
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
        fabAddFile.setOnClickListener {
            fabMenu.collapse()
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intent.putExtra(Intent.CATEGORY_OPENABLE, true) // should I have this set?
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "Upload a file"), FILE_REQUEST_CODE)
        }

        fabAddDir.setOnClickListener {
            fabMenu.collapse()
            val dialogView = layoutInflater.inflate(R.layout.fragment_renter_add_dir, null, false)
            val dialog = AlertDialog.Builder(context!!)
                    .setTitle("New directory")
                    .setView(dialogView)
                    .setPositiveButton("Create", { dialogInterface, i ->
                        viewModel.createDir(dialogView.findViewById<EditText>(R.id.newDirName).text.toString())
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
            dialog.show()
            dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }


        /* observe viewModel stuff */
        viewModel.displayedNodes.observe(this) {
            adapter.display(it)
        }

        viewModel.searching.observe(this) {
            if (it && !searchIsExpanded)
                searchItem?.expandActionView()
            else if (!it && searchIsExpanded)
                searchItem?.collapseActionView()

            if (it) {
                // when using resize animation, the tab resets its height after the animation if finished for whatever reason
                filepathTabs.visibility = View.GONE
                top_shadow.visibility = View.GONE
            } else {
                filepathTabs.visibility = View.VISIBLE
                top_shadow.visibility = View.VISIBLE
            }
        }

        viewModel.currentDir.observe(this) {
            programmatic = true
            val newPath = it.path.split("/")
            /* find the last point at which the path is the same */
            val breakpoint = ((0 until maxOf(newPath.size, currentPath.size)).firstOrNull {
                it > currentPath.size - 1 || it > newPath.size - 1 || newPath[it] != currentPath[it]
            } ?: 0) - 1
            /* select the appropriate tab if it already exists */
            if (breakpoint >= 0)
                filepathTabs.getTabAt(breakpoint)?.select()
            /* remove tabs that are past the breakpoint */
            for (i in breakpoint + 1 until filepathTabs.tabCount)
                filepathTabs.removeTabAt(breakpoint + 1)
            /* add new tabs from the breakpoint to the end of the new path */
            for (i in breakpoint + 1 until newPath.size)
                filepathTabs.addTab(filepathTabs.newTab().setText(newPath[i]), true)
            /* select the rightmost tab */
            /* scroll the tabs all the way to the right */
            filepathTabs.postDelayed({
                filepathTabs.getTabAt(newPath.size - 1)!!.select()
                filepathTabs.fullScroll(TabLayout.FOCUS_RIGHT)
            }, 20)
            currentPath = newPath
            setSearchHint()
            programmatic = false
        }

        viewModel.error.observe(this) {
            it.snackbar(coordinator) // TODO: make FAB move up when snackbar appears
            renterSwipeRefresh.isRefreshing = false
        }

        viewModel.detailsItem.observe(this) {
            if (it is Dir)
                DirBottomSheetFragment().show(childFragmentManager, null)
            else if (it is RenterFileData)
                FileBottomSheetFragment().show(childFragmentManager, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            println(uri)
            println(uri.path)
            // TODO: call uploadFile() or whatever on the VM using this info
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_files, menu)

        searchItem = menu.findItem(R.id.search)
        searchView = searchItem!!.actionView as SearchView
        setSearchHint()

        /* have to use this to listen for it being closed, because closelistener doesn't work for whatever reason */
        searchItem!!.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchIsExpanded = false
                if (viewModel.searching.value == true)
                    viewModel.cancelSearch()
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchIsExpanded = true
                viewModel.searching.value = true
                    viewModel.search(searchView?.query?.toString() ?: "")
                return true
            }
        })
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                println("onQueryTextChange")
                /* need to check because collapsing the SearchView will clear it's text and trigger this after it's collapsed */
                if (viewModel.searching.value == true)
                    viewModel.search(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                GenUtil.hideSoftKeyboard(activity)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.search -> true
        else -> super.onOptionsItemSelected(item)
    }

    private fun setSearchHint() {
        if (viewModel.currentDirPath == "root" || viewModel.currentDir.value == null)
            searchView?.queryHint = "Search..."
        else
            searchView?.queryHint = "Search ${viewModel.currentDir.value!!.name}..."
    }

    override fun onBackPressed(): Boolean {
        return if (viewModel.searching.value == true) {
            viewModel.cancelSearch()
            true
        } else {
            viewModel.goUpDir()
        }
    }

    override fun onShow() {
        viewModel.refresh()
    }

    companion object {
        val FILE_REQUEST_CODE = 5424
    }
}
