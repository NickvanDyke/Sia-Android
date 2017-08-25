/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.renter.files

import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_renter.*
import vandyke.siamobile.R
import vandyke.siamobile.backend.BaseMonitorService
import vandyke.siamobile.backend.networking.SiaError
import vandyke.siamobile.backend.renter.RenterService
import vandyke.siamobile.backend.renter.SiaDir


class RenterFragment : Fragment(), RenterService.FilesListener {
    private lateinit var connection: ServiceConnection
    private lateinit var renterService: RenterService
    private var bound = false

    private lateinit var adapter: FilesAdapter

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

    override fun onFilesUpdate(rootDir: SiaDir) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFilesError(error: SiaError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
}
