package com.criptext.mail.utils.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import com.criptext.mail.R

class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private val progressBar: ProgressBar = v.findViewById(R.id.progressBar)

    init {
        progressBar.isIndeterminate = true
    }
}