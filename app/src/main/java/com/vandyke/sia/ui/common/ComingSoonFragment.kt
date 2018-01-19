/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.os.Bundle
import android.view.View
import com.vandyke.sia.R
import com.vandyke.sia.util.GenUtil
import kotlinx.android.synthetic.main.fragment_coming_soon.*

class ComingSoonFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_coming_soon

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        comingSoonText.setOnClickListener {
            GenUtil.launchCustomTabs(context!!, "https://github.com/NickvanDyke/Sia-Android")
        }
    }
}