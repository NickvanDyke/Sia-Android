package com.vandyke.sia.ui.node

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.data.siad.SiadStatus.State.*
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.rx.main
import com.vandyke.sia.util.rx.observe
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_node_status.*
import javax.inject.Inject

class NodeStatusFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_node_status
    override val title: String = "Node Status"

    @Inject
    lateinit var siadStatus: SiadStatus
    @Inject
    lateinit var siadSource: SiadSource

    private lateinit var subscription: Disposable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context!!.getAppComponent().inject(this)

        siaOutput.movementMethod = ScrollingMovementMethod()

        siaButton.setOnClickListener {
            if (siadStatus.state.value == CRASHED)
                siadSource.signalRestart()
            else
                Prefs.siaManuallyStopped = !Prefs.siaManuallyStopped
        }

        siadStatus.state.observe(this) {
            siaButton.text = when (it) {
                COULDNT_COPY_BINARY -> "Couldn't copy Sia executable"
                SERVICE_STARTED -> "Service started"
                CRASHED -> "Crashed - tap to restart"
                STARTING_PROCESS -> "Starting Sia process..."
                SIAD_LOADING -> "Sia is loading..."
                SIAD_LOADED -> "Running..."
                SERVICE_STOPPED -> "Service isn't running"
                UNMET_CONDITIONS -> "Run conditions not met. Changeable in Node > Settings."
                RESTARTING -> "Restarting..."
                MANUALLY_STOPPED -> "Manually stopped"
                COULDNT_START_PROCESS -> "Couldn't start process"
                WORKING_DIRECTORY_DOESNT_EXIST -> "Set working directory doesn't exist"
                EXTERNAL_STORAGE_ERROR -> "Error with external storage"
            }

            siaButton.isEnabled = when (it) {
                MANUALLY_STOPPED -> true
                else -> true
            }
        }

        subscription = siadStatus.allSiadOutput
                .main()
                .subscribe {
                    siaOutput.append(it + "\n")
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription.dispose()
    }
}