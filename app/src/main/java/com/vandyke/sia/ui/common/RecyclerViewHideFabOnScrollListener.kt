package com.vandyke.sia.ui.common

import com.github.clans.fab.FloatingActionMenu

class RecyclerViewHideFabOnScrollListener(private val fab: FloatingActionMenu) : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
        if (dy > 20) {
            fab.hideMenuButton(true)
        } else if (dy < -20) {
            fab.showMenuButton(true)
        }
    }
}