/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.view

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.data.renter.SiaDir
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.ui.renter.model.RenterModelHttp
import vandyke.siamobile.ui.renter.presenter.IRenterPresenter
import vandyke.siamobile.ui.renter.presenter.RenterPresenter
import vandyke.siamobile.ui.renter.view.files.FilesAdapter


class RenterFragment : Fragment(), IRenterView {

    private val presenter: IRenterPresenter = RenterPresenter(this, RenterModelHttp())

    private lateinit var adapter: FilesAdapter
    private var programmaticallySelecting = true

    var rootDir: SiaDir = SiaDir("home", null)

    var currentDir: SiaDir = rootDir
        set(value) { // TODO: should move this logic to the presenter
            programmaticallySelecting = true
            val oldPath = field.fullPath
            val newPath = value.fullPath
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
            field = value
            adapter.notifyDataSetChanged()
            programmaticallySelecting = false
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_renter, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(activity)
        filesList.layoutManager = layoutManager
//        filesList.addItemDecoration(new DividerItemDecoration(filesList.getContext(), layoutManager.getOrientation()));
        adapter = FilesAdapter(this)
        filesList.adapter = adapter

        renterFilepath.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (!programmaticallySelecting)
                    currentDir = currentDir.fullPath[renterFilepath.selectedTabPosition]
            }
        })
    }

    override fun onRootDirUpdate(dir: SiaDir) {
        this.rootDir = dir
    }

    override fun onCurrentDirUpdate(dir: SiaDir) {
        // TODO: check if currentDir is still valid
        currentDir = dir
    }

    override fun onError(error: SiaError) {
        error.snackbar(view)
    }

    override fun goUpDir(): Boolean {
        if (currentDir.parent == null)
            return false
        currentDir = currentDir.parent!!
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionRefresh -> presenter.refresh()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            activity.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_renter, menu)
    }
}
