/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.renter.files.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.vandyke.sia.R
import com.vandyke.sia.data.models.renter.Dir
import com.vandyke.sia.data.models.renter.SiaFile
import com.vandyke.sia.data.repository.FilesRepository.OrderBy
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.ui.common.RecyclerViewHideFabOnScrollListener
import com.vandyke.sia.ui.renter.files.view.list.NodesAdapter
import com.vandyke.sia.ui.renter.files.viewmodel.FilesViewModel
import com.vandyke.sia.util.*
import com.vandyke.sia.util.rx.observe
import io.github.tonnyl.light.Light
import kotlinx.android.synthetic.main.fragment_files.*
import javax.inject.Inject


class FilesFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_files
    override val hasOptionsMenu = true
    override val title: String = "Files"

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var vm: FilesViewModel
    @Inject
    lateinit var siadStatus: SiadStatus

    private var searchItem: MenuItem? = null
    private var searchView: SearchView? = null
    /** searchItem.isActionViewExpanded() doesn't always return the correct value? so need to keep track ourselves and use that */
    private var searchIsExpanded = false

    private var viewTypeItem: MenuItem? = null

    private var ascendingItem: MenuItem? = null
    private val orderByItems = mutableListOf<MenuItem>()

    private lateinit var spinnerView: Spinner
    private lateinit var pathAdapter: ArrayAdapter<String>

    private lateinit var nodesAdapter: NodesAdapter

    private var currentMultiMoveResId: Int = R.drawable.ic_edit_black

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context!!.getAppComponent().inject(this)

        vm = ViewModelProviders.of(this, factory).get(FilesViewModel::class.java)

        /* set up nodes list */
        nodesAdapter = NodesAdapter(this)
        nodes_list.adapter = nodesAdapter

        pathAdapter = ArrayAdapter(context, R.layout.spinner_selected_item_white)
        pathAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        /* set up path spinner */
        spinnerView = Spinner(context).apply {
            minimumWidth = 400
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    vm.goToIndexInPath(position)
                }
            }
            adapter = pathAdapter
            background.setColorFilter(context!!.getColorRes(android.R.color.white), PorterDuff.Mode.SRC_ATOP)
        }

        /* pull-to-refresh stuff */
        nodes_list_swiperefresh.setColors(context!!)
        nodes_list_swiperefresh.setOnRefreshListener(vm::refresh)
        nodes_list.addOnScrollListener(RecyclerViewHideFabOnScrollListener(fabFilesMenu))

        /* FAB stuff */
        fabAddFile.setOnClickListener {
            launchSafChooseFile()
            fabFilesMenu.close(true)
        }

        fabAddDir.setOnClickListener {
            fabFilesMenu.close(true)
            DialogUtil.editTextDialog(context!!,
                    "New directory",
                    "Create",
                    { vm.createDir(it) },
                    "Cancel",
                    editTextFunc = { hint = "Name" })
                    .showDialogAndKeyboard()
        }

        /* multi select stuff */
        multiMove.setOnClickListener {
            if (vm.allSelectedAreInCurrentDir) {
                /* depending on how many nodes are selected, creating all these dialogs can have
                 * a noticeable delay. It could probably also crash if enough are created -
                 * each dialog uses about 20MB of memory. Maybe limit and show warning instead if # is too high?
                 * I tried creating one dialog, and then modifying it between uses and then reshowing
                 * it, but calling show() wouldn't show it after pressing one of the buttons hid it. Not sure why. */
                vm.selectedNodes.value.forEach { node ->
                    DialogUtil.editTextDialog(context!!,
                            "Rename ${node.name}",
                            "Rename",
                            { newName ->
                                if (node is SiaFile)
                                    vm.renameFile(node, newName)
                                else if (node is Dir)
                                    vm.renameDir(node, newName)
                                vm.deselect(node)
                            },
                            "Cancel",
                            editTextFunc = {
                                setText(node.name)
                                selectAll()
                                hint = "Name"
                            })
                            .showDialogAndKeyboard()
                }
            } else {
                vm.moveSelectedToCurrentDir()
            }
        }

        multiDownload.setOnClickListener {
            downloadSelected()
        }

        multiDelete.setOnClickListener {
            AlertDialog.Builder(context!!)
                    .setTitle("Confirm delete")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes") { _, _ -> vm.deleteSelected() }
                    .setNegativeButton("No", null)
                    .show()
        }

        multiDeselect.setOnClickListener {
            vm.deselectAll()
        }

        /* observe viewModel stuff */
        vm.currentDir.observe(this) {
            // if I make the currentDir in the VM a flowable, then I probably need to do this
            // differently, by determining the breakpoint like before and adding/removing before/after it
            // instead of clearing it all
            pathAdapter.clear()
            /* need to handle it a bit differently since the root dir's name is "" */
            val path = if (it.path.isNotEmpty())
                it.path.split('/').toMutableList()
            else
                mutableListOf()
            path.add(0, "Home")
            pathAdapter.addAll(path)
            spinnerView.setSelection(path.size - 1)
            setSearchHint()
            setMultiMoveImage()
        }

        vm.displayedNodes.observe(this) {
            nodesAdapter.submitList(it)
        }

        vm.viewAsList.observe(this) {
            if (it) {
                viewTypeItem?.setIcon(R.drawable.ic_view_list_white)
                viewTypeItem?.title = "View as grid"
                nodes_list.layoutManager = LinearLayoutManager(context)
            } else {
                viewTypeItem?.setIcon(R.drawable.ic_view_module_white)
                viewTypeItem?.title = "View as list"
                nodes_list.layoutManager = GridLayoutManager(context, LayoutUtil.calculateNoOfColumns(context!!))
            }
            nodes_list.recycledViewPool.clear()
        }

        vm.selectedNodes.observe(this) {
            if (it.isEmpty()) {
                selectedMenu.collapse()
            } else {
                setMultiMoveImage()
                num_selected.text = "${it.size} ${"item".pluralize(it.size)}"
                selectedMenu.expand()
            }
        }

        vm.ascending.observe(this) {
            ascendingItem?.isChecked = it
        }

        vm.orderBy.observe(this) {
            setCheckedOrderByItem()
        }

        vm.searching.observe(this) {
            if (it && !searchIsExpanded)
                searchItem?.expandActionView()
            else if (!it && searchIsExpanded)
                searchItem?.collapseActionView()
        }

        vm.refreshing.observe(this, nodes_list_swiperefresh::setRefreshing)

        vm.activeTasks.observe(this) {
            progressBar.goneUnless(it > 0)
        }

        vm.success.observe(this) {
            Light.success(coordinator, it, Snackbar.LENGTH_SHORT).show()
        }

        vm.error.observe(this) {
            it.snackbar(coordinator, siadStatus.state.value!!)
        }

        siadStatus.state.observe(this) {
            if (it == SiadStatus.State.SIAD_LOADED)
                vm.refresh()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.data != null) { /* one item was selected */
                val uri = data.data
                val path = FileUtils.getPath(context!!, uri) // TODO: not sure if this will work for all sources of files. Might not for non-primary external storage
                if (path == null) {
                    Analytics.unsupportedDataSource(uri)
                    AlertDialog.Builder(context!!)
                            .setTitle("Unsupported")
                            .setMessage("Sia for Android doesn't currently support uploading files from that location, sorry.")
                            .setPositiveButton("Close", null)
                            .show()
                } else {
                    vm.uploadFile(path)
                }
            } else if (data.clipData != null) { /* multiple items selected */
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    val path = FileUtils.getPath(context!!, uri)
                    println(path)
                    if (path == null) {
                        Analytics.unsupportedDataSource(uri)
                        AlertDialog.Builder(context!!)
                                .setTitle("Unsupported")
                                .setMessage("Sia for Android doesn't currently support uploading files from that location, sorry.")
                                .setPositiveButton("Close", null)
                                .show()
                    } else {
                        vm.uploadFile(path)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_READ_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchSafChooseFile()
            } else {
                AlertDialog.Builder(context!!)
                        .setMessage("Sia needs read access to your storage in order to upload your chosen files to the Sia network")
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
        } else if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadSelected()
            } else {
                AlertDialog.Builder(context!!)
                        .setMessage("Sia needs write access to your storage in order to download your files from the Sia network to your device's storage")
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
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
                if (vm.searching.value)
                    vm.cancelSearch()
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchIsExpanded = true
                vm.search(searchView?.query?.toString() ?: "")
                return true
            }
        })
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                /* need to check because collapsing the SearchView will clear it's text and trigger this after it's collapsed */
                if (vm.searching.value)
                    vm.search(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                KeyboardUtil.hideKeyboard(activity!!)
                return true
            }
        })

        viewTypeItem = menu.findItem(R.id.viewType)
        viewTypeItem!!.setIcon(if (vm.viewAsList.value) R.drawable.ic_view_list_white else R.drawable.ic_view_module_white)

        ascendingItem = menu.findItem(R.id.ascendingToggle)
        ascendingItem!!.isChecked = vm.ascending.value

        /* must add the items in the same order as they appear in the enum values for the function after to work */
        orderByItems.clear()
        orderByItems.add(menu.findItem(R.id.orderByName))
        orderByItems.add(menu.findItem(R.id.orderBySize))
        setCheckedOrderByItem()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.ascendingToggle -> {
            item.isChecked = !item.isChecked
            vm.ascending.value = item.isChecked
            true
        }
        R.id.orderByName -> {
            vm.orderBy.value = OrderBy.PATH
            true
        }
        R.id.orderBySize -> {
            vm.orderBy.value = OrderBy.SIZE
            true
        }
        R.id.viewType -> {
            vm.viewAsList.value = !vm.viewAsList.value
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /* The order of the OrderBy enum values and the order of the sort by options in the list must be the same for this to work */
    private fun setCheckedOrderByItem() {
        val orderBy = vm.orderBy.value
        orderByItems.forEachIndexed { i, item ->
            if (i == orderBy.ordinal) {
                item.isChecked = true
                item.isCheckable = true
            } else {
                item.isChecked = false
                item.isCheckable = false
            }
        }
    }

    private fun setSearchHint() {
        searchView?.queryHint = "Search${if (vm.currentDirPath.isNotEmpty()) " ${vm.currentDir.value.name}" else ""}..."
    }

    private fun setMultiMoveImage() {
        val newResId = when {
            vm.allSelectedAreInCurrentDir -> R.drawable.ic_edit_black
            else -> R.drawable.ic_move_to_inbox_black
        }

        if (currentMultiMoveResId == newResId)
            return

        /* fade out, switch to new image, fade in */
        multiMove.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    multiMove.setImageResource(newResId)
                    currentMultiMoveResId = newResId
                    multiMove.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start()
                }
                .start()
    }

    private fun launchSafChooseFile() {
        if (!context!!.havePermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_PERMISSION)
            return
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                .putExtra(Intent.CATEGORY_OPENABLE, true)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Upload a file"), FILE_REQUEST_CODE)
    }

    private fun downloadSelected() {
        if (!context!!.havePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
        } else {
            vm.downloadSelected(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)
        }
    }

    override fun onBackPressed(): Boolean {
        return if (fabFilesMenu.isOpened) {
            fabFilesMenu.close(true)
            true
        } else {
            vm.goUpDir()
        }
    }

    override fun onShow() {
        toolbar.addView(spinnerView)
        actionBar.setDisplayShowTitleEnabled(false)
        progressBar.goneUnless(vm.activeTasks.value > 0)
    }

    override fun onHide() {
        toolbar.removeView(spinnerView)
        actionBar.setDisplayShowTitleEnabled(true)
    }

    companion object {
        private const val FILE_REQUEST_CODE = 5424
        private const val REQUEST_READ_PERMISSION = 1212
        private const val REQUEST_WRITE_PERMISSION = 1256
    }
}
