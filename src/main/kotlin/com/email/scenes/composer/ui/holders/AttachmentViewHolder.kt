package com.email.scenes.composer.ui.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.email.R

class AttachmentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    val progressBar : ProgressBar = view.findViewById(R.id.attachment_progress_bar)
    val filename: TextView = view.findViewById(R.id.attachment_filename)

}