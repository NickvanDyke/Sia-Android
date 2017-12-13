/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_renter.*
import vandyke.siamobile.R
import vandyke.siamobile.data.data.renter.SiaDir
import vandyke.siamobile.data.data.renter.SiaFile
import vandyke.siamobile.ui.main.BaseFragment
import vandyke.siamobile.ui.renter.view.list.NodesAdapter
import vandyke.siamobile.ui.renter.viewmodel.RenterViewModel
import vandyke.siamobile.util.observe


class RenterFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_renter
    override val hasOptionsMenu = true

    lateinit var viewModel: RenterViewModel

    private var depth = 0
    private lateinit var adapter: NodesAdapter
    private var programmaticallySelecting = true

    var currentDir: SiaDir = SiaDir(ROOT_DIR_NAME, null) /* this initialization is 100% temporary, and so that currentDir can be non-nullable */
        set(value) {
            /* set this flag so that programmatically selecting a tab doesn't trigger the onTabSelectedListener */
            programmaticallySelecting = true
            val oldPath = field.path
            val newPath = value.path
            depth = newPath.size
            /* find the point at which the path differs */
            val breakpoint = (0 until maxOf(newPath.size, oldPath.size)).firstOrNull {
                it > oldPath.size - 1 || it > newPath.size - 1 || newPath[it] != oldPath[it]
            } ?: renterFilepath.tabCount
            /* select the appropriate tab if it already exists */
            if (newPath.size < oldPath.size)
                renterFilepath.getTabAt(newPath.size - 1)?.select()
            /* remove tabs that are past the breakpoint */
            for (i in breakpoint until renterFilepath.tabCount)
                renterFilepath.removeTabAt(breakpoint)
            /* add new tabs from the breakpoint to the end of the new path */
            for (i in breakpoint until newPath.size)
                renterFilepath.addTab(renterFilepath.newTab().setText(newPath[i].name), true)
            /* scroll the tabs all the way to the right */
            renterFilepath.postDelayed({ renterFilepath.fullScroll(TabLayout.FOCUS_RIGHT) }, 5)
            adapter.displayDir(value)
            programmaticallySelecting = false
            field = value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(RenterViewModel::class.java)
        filesList.addItemDecoration(DividerItemDecoration(filesList.context, (filesList.layoutManager as LinearLayoutManager).orientation))
        adapter = NodesAdapter(viewModel)
        filesList.adapter = adapter

        renterFilepath.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (!programmaticallySelecting)
                    viewModel.changeDir(currentDir.getParentDirAt(depth - renterFilepath.selectedTabPosition - 1))
            }
        })

        renterSwipeRefresh.setColorSchemeResources(R.color.colorAccent)
        renterSwipeRefresh.setOnRefreshListener {
            viewModel.refreshFiles()
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
        viewModel.rootDir.observe(this) {

        }

        viewModel.currentDir.observe(this) {
            this.currentDir = it
        }

        viewModel.error.observe(this) {
            it.snackbar(coordinator) // TODO: make FAB move up when snackbar appears
            renterSwipeRefresh.isRefreshing = false
        }

        viewModel.detailsItem.observe(this) {
            if (it is SiaDir)
                DirBottomSheetFragment().show(childFragmentManager, null)
            else if (it is SiaFile)
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
        viewModel.refreshFiles()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_renter, menu)
    }

    companion object {
        val ROOT_DIR_NAME = "home"
    }
}
