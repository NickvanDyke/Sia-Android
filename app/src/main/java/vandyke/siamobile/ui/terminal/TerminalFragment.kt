/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.terminal

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import kotlinx.android.synthetic.main.fragment_terminal.*
import vandyke.siamobile.R
import vandyke.siamobile.ui.BaseFragment
import vandyke.siamobile.util.observe

class TerminalFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_terminal

    private lateinit var viewModel: TerminalViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(TerminalViewModel::class.java)

        terminalText.movementMethod = ScrollingMovementMethod()

        input.setOnEditorActionListener { v, actionId, event ->
            viewModel.runSiacCommand(v.text.toString())
            v.text = ""
            true
        }

        viewModel.output.observe(this) {
            terminalText.append(it)
        }
    }
}
