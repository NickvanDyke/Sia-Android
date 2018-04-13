package com.vandyke.sia.ui.node

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.StyleSpan
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.data.siad.SiadService
import com.vandyke.sia.data.siad.SiadSource
import com.vandyke.sia.data.siad.SiadStatus
import com.vandyke.sia.data.siad.SiadStatus.State.*
import com.vandyke.sia.getAppComponent
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.oneTimeTooltip
import com.vandyke.sia.util.rx.main
import com.vandyke.sia.util.rx.observe
import io.reactivex.disposables.Disposable
import it.sephiroth.android.library.tooltip.Tooltip
import kotlinx.android.synthetic.main.fragment_node_status.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NodeStatusFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_node_status
    override val title: String = "Node Status"

    private val df = SimpleDateFormat.getTimeInstance()

    @Inject
    lateinit var siadStatus: SiadStatus
    @Inject
    lateinit var siadSource: SiadSource

    private lateinit var subscription: Disposable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context!!.getAppComponent().inject(this)

        sia_output.movementMethod = ScrollingMovementMethod()

        sia_button.setOnClickListener {
            Prefs.siaManuallyStopped = !Prefs.siaManuallyStopped
            if (!Prefs.siaManuallyStopped)
                context!!.startService(Intent(context, SiadService::class.java))
        }

        siadStatus.state.observe(this) {
            sia_button.text = when (it) {
                COULDNT_COPY_BINARY -> "Couldn't copy Sia executable"
                SERVICE_STARTED -> "Service started"
                CRASHED -> "Crashed"
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

            sia_button.isEnabled = when (it) {
                MANUALLY_STOPPED -> true
                else -> true
            }
        }

        subscription = siadStatus.allSiadOutput
                .main()
                .subscribe {
                    val time = df.format(Date())
                    val str = SpannableString("$time $it\n")
                    str.setSpan(StyleSpan(Typeface.BOLD), 0, time.length, 0)
                    sia_output.append(str)
                }

        sia_button.oneTimeTooltip("This button will display the Sia node's state. Tapping it will manually stop the Sia node.", Tooltip.Gravity.TOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription.dispose()
    }
}