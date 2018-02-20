/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.terminal

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.ui.common.BaseFragment
import com.vandyke.sia.util.rx.observe
import kotlinx.android.synthetic.main.fragment_terminal.*

class TerminalFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_terminal
    override val title: String = "Terminal"

    private lateinit var viewModel: TerminalViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(TerminalViewModel::class.java)

        terminalText.movementMethod = ScrollingMovementMethod()

        input.setOnEditorActionListener { v, _, _ ->
            viewModel.runSiacCommand(v.text.toString())
            v.text = ""
            true
        }

        viewModel.output.observe(this) {
            terminalText.append(it)
        }
    }
}
