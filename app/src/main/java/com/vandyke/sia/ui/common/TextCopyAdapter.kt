/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import com.vandyke.sia.R

class TextCopyAdapter(var data: List<String> = listOf()) : androidx.recyclerview.widget.RecyclerView.Adapter<TextHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextHolder {
        return TextHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_text_copy, parent, false))
    }

    override fun onBindViewHolder(holder: TextHolder, position: Int) {
        holder.text.text = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }
}