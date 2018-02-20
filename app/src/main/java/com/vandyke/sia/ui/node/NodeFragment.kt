package com.vandyke.sia.ui.node

import android.os.Bundle
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.appComponent
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.rx.observe
import kotlinx.android.synthetic.main.fragment_node.*
import javax.inject.Inject

class NodeFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_node
    override val title: String = "Node"

    @Inject
    lateinit var siadStatus: SiadStatus
    @Inject
    lateinit var siadSource: SiadSource

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appComponent.inject(this)

        siaButton.setOnClickListener {
            Prefs.siaManuallyStopped = !Prefs.siaManuallyStopped
        }

        siadSource.allConditionsGood.observe(this) {
            if (it) {
                siaButton.isEnabled = true
                siaButton.text = "Loading..."
            } else {
                siaButton.text = if (Prefs.siaManuallyStopped) {
                    siaButton.isEnabled = true
                    "Manually stopped"
                } else {
                    siaButton.isEnabled = false
                    "Stopped - run conditions not met"
                }
            }
        }

        siadStatus.mostRecentSiadOutput.observe(this) {
            if (!siadStatus.isSiadLoaded.value)
                siaButton.text = it
        }

        siadStatus.isSiadLoaded.observe(this) {
            if (it)
                siaButton.text = "Running..."
        }
    }
}