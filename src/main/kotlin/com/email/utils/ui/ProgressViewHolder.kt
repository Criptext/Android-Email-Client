package com.email.utils.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import com.email.R

class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    var progressBar: ProgressBar

    init {
        progressBar = v.findViewById(R.id.progressBar)
        progressBar.isIndeterminate = true
    }
}