/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter

import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.TabLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import kotlinx.android.synthetic.main.fragment_renter.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.renter.RenterService
import vandyke.siamobile.backend.renter.SiaDir
import vandyke.siamobile.ui.renter.files.FilesAdapter


class RenterFragment : Fragment(), RenterService.FilesListener {
    private lateinit var connection: ServiceConnection
    private lateinit var renterService: RenterService
    private var bound = false

    private lateinit var adapter: FilesAdapter
    private var programmaticallySelecting = true

    var rootDir: SiaDir = SiaDir("home", null)

    var currentDir: SiaDir = rootDir
        set(value) {
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

    override fun onFilesUpdate(rootDir: SiaDir) {
        if (this.currentDir == this.rootDir) {
            currentDir = rootDir
        }
        this.rootDir = rootDir
        // TODO: check if currentDir is still valid
    }

    override fun onFilesError(error: SiaError) {
        error.snackbar(view)
    }

    fun goUpDir(): Boolean {
        if (currentDir.parent == null)
            return false
        currentDir = currentDir.parent!!
        return true
    }

    fun refreshService() {
        if (bound)
            renterService.refresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionRefresh -> refreshService()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            renterService.unregisterListener(this)
            if (isAdded) {
                activity.unbindService(connection)
                bound = false
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            activity.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_renter, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                renterService = (service as BaseMonitorService.LocalBinder).service as RenterService
                renterService.registerListener(this@RenterFragment)
                bound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                bound = false
            }
        }
        activity.bindService(Intent(activity, RenterService::class.java), connection, Context.BIND_AUTO_CREATE)
    }
}
