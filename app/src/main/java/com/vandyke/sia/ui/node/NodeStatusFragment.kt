package com.vandyke.sia.ui.node

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.appComponent
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.data.siad.SiadStatus.State.*
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.rx.main
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_node_status.*
import javax.inject.Inject

class NodeStatusFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_node_status
    override val title: String = "Node Status"

    @Inject
    lateinit var siadStatus: SiadStatus

    private lateinit var subscription: Disposable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appComponent.inject(this)

        siaOutput.movementMethod = ScrollingMovementMethod()

        siaButton.setOnClickListener {
            Prefs.siaManuallyStopped = !Prefs.siaManuallyStopped
        }

        siadStatus.state.observe(this) {
            siaButton.text = when (it) {
                STOPPING -> "Stopping..."
                STOPPED -> {
                    if (Prefs.siaManuallyStopped) {
                        "Manually stopped"
                    } else {
                        "Stopped - run conditions not met"
                    }
                }
                PROCESS_STARTING -> "Starting..."
                SIAD_LOADING -> "Loading..."
                SIAD_LOADED -> "Running..."
            }

            siaButton.isEnabled = when (it) {
                STOPPED -> Prefs.siaManuallyStopped
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