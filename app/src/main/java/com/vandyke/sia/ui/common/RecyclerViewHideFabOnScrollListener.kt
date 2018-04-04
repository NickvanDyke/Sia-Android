package com.vandyke.sia.ui.common

import android.support.v7.widget.RecyclerView
import com.github.clans.fab.FloatingActionMenu

class RecyclerViewHideFabOnScrollListener(val fab: FloatingActionMenu) : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy > 20) {
            fab.hideMenuButton(true)
        } else if (dy < -20) {
            fab.showMenuButton(true)
        }
    }
}