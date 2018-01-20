/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.vandyke.sia.R
import com.vandyke.sia.appComponent
import com.vandyke.sia.data.repository.FilesRepository.SortBy
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.renter.files.view.list.NodesAdapter
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.GenUtil
import com.vandyke.sia.util.observe
import com.vandyke.sia.util.snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_renter_files.*
import javax.inject.Inject


class FilesFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_renter_files
    override val hasOptionsMenu = true

    @Inject lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: FilesViewModel
    @Inject lateinit var siadSource: SiadSource

    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null
    /** searchItem.isActionViewExpanded() doesn't always return the correct value? so need to keep track ourselves and use that */
    private var searchIsExpanded = false

    private var ascendingItem: MenuItem? = null
    private val orderByItems = mutableListOf<MenuItem>()

    private lateinit var spinnerView: Spinner
    private lateinit var pathAdapter: ArrayAdapter<String>

    private lateinit var nodesAdapter: NodesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, factory).get(FilesViewModel::class.java)

        /* set up nodes list */
        nodesAdapter = NodesAdapter(viewModel)
        nodesList.adapter = nodesAdapter

        pathAdapter = ArrayAdapter(context, R.layout.spinner_selected_item)
        pathAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        /* set up path spinner */
        spinnerView = Spinner(context)
        spinnerView.minimumWidth = 400
        spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.goToIndexInPath(position)
            }
        }
        spinnerView.adapter = pathAdapter
        spinnerView.background.setColorFilter(ContextCompat.getColor(context!!, android.R.color.white), PorterDuff.Mode.SRC_ATOP)

        /* pull-to-refresh stuff */
        nodesListRefresh.setColorSchemeResources(R.color.colorAccent)
        nodesListRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        val array = context!!.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        val backgroundColor = array.getColor(0, 0xFF00FF)
        array.recycle()
        nodesListRefresh.setProgressBackgroundColorSchemeColor(backgroundColor)

        /* FAB stuff */
        fabAddFile.setOnClickListener {
            fabFilesMenu.close(true)
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            intent.putExtra(Intent.CATEGORY_OPENABLE, true) // should I have this set?
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "Upload a file"), FILE_REQUEST_CODE)
        }

        fabAddDir.setOnClickListener {
            fabFilesMenu.close(true)
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
        viewModel.currentDir.observe(this) {
            pathAdapter.clear()
            val path = it.path.split('/')
            path.forEach {
                pathAdapter.add(it)
            }
            spinnerView.setSelection(path.size - 1)
            setSearchHint()
        }

        viewModel.displayedNodes.observe(this) {
            nodesAdapter.display(it)
        }

        viewModel.ascending.observe(this) {
            ascendingItem?.isChecked = it
        }

        viewModel.sortBy.observe(this) {
            setCheckedOrderByItem()
        }

        viewModel.searching.observe(this) {
            if (it && !searchIsExpanded)
                searchItem?.expandActionView()
            else if (!it && searchIsExpanded)
                searchItem?.collapseActionView()
        }

        viewModel.refreshing.observe(this) {
            nodesListRefresh.isRefreshing = it
        }

        viewModel.error.observe(this) {
            it.snackbar(coordinator) // TODO: make FAB move up when snackbar appears
            nodesListRefresh.isRefreshing = false
        }

        siadSource.isSiadLoaded.observe(this) {
            if (it)
                viewModel.refresh()
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

        /* set up search stuff */
        searchItem = menu.findItem(R.id.search)
        searchView = searchItem!!.actionView as SearchView
        setSearchHint()
        /* have to use this to listen for it being closed, because closelistener doesn't work for whatever reason */
        searchItem!!.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchIsExpanded = false
                if (viewModel.searching.value)
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
                /* need to check because collapsing the SearchView will clear it's text and trigger this after it's collapsed */
                if (viewModel.searching.value)
                    viewModel.search(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                GenUtil.hideSoftKeyboard(activity)
                return true
            }
        })

        ascendingItem = menu.findItem(R.id.ascendingToggle)
        ascendingItem!!.isChecked = viewModel.ascending.value

        /* must add the items in the same order as they appear in the enum values for the function after to work */
        orderByItems.clear()
        orderByItems.add(menu.findItem(R.id.orderByName))
        orderByItems.add(menu.findItem(R.id.orderBySize))
        orderByItems.add(menu.findItem(R.id.orderByModified))
        setCheckedOrderByItem()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.ascendingToggle -> {
            item.isChecked = !item.isChecked
            viewModel.ascending.value = item.isChecked
            false
        }
        R.id.orderByName -> {
            viewModel.sortBy.value = SortBy.NAME
            false
        }
        R.id.orderBySize -> {
            viewModel.sortBy.value = SortBy.SIZE
            false
        }
        R.id.orderByModified -> {
            viewModel.sortBy.value = SortBy.MODIFIED
            false
        }
        else -> super.onOptionsItemSelected(item)
    }

    /** The order of the SortBy enum values and the order of the sort by options in the list must be the same for this to work */
    private fun setCheckedOrderByItem() {
        val sortBy = viewModel.sortBy.value
        orderByItems.forEachIndexed { i, item ->
            if (i == sortBy.ordinal) {
                item.isChecked = true
                item.isCheckable = true
            } else {
                item.isChecked = false
                item.isCheckable = false
            }
        }
    }

    private fun setSearchHint() {
        if (viewModel.currentDirPath == "root" || viewModel.currentDir.value == null)
            searchView?.queryHint = "Search..."
        else
            searchView?.queryHint = "Search ${viewModel.currentDir.value!!.name}..."
    }

    override fun onBackPressed(): Boolean {
        return if (viewModel.searching.value) {
            viewModel.cancelSearch()
            true
        } else {
            viewModel.goUpDir()
        }
    }

    override fun onShow() {
        viewModel.refresh()
        activity!!.toolbar.addView(spinnerView)
        setActionBarTitleDisplayed(false)
    }

    override fun onHide() {
        activity!!.toolbar.removeView(spinnerView)
        setActionBarTitleDisplayed(true)
    }

    companion object {
        val FILE_REQUEST_CODE = 5424
    }

    private fun setActionBarTitleDisplayed(visible: Boolean) {
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowTitleEnabled(visible)
    }
}
