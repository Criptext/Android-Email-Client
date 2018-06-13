package com.email.scenes.composer.ui.holders

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import com.email.R
import com.email.db.AttachmentTypes
import com.email.utils.Utility

class AttachmentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    val progressBar : ProgressBar = view.findViewById(R.id.attachment_progress_bar)
    val filename: TextView = view.findViewById(R.id.attachment_filename)
    val statusView: ImageView = view.findViewById(R.id.status_view)
    val typeView: ImageView = view.findViewById(R.id.attachment_type_image)
    val containerView: RelativeLayout = view.findViewById(R.id.attachment_container)

    init {
        statusView.visibility = View.GONE
    }

    fun setFields(name: String, type: AttachmentTypes){
        filename.text = name
        typeView.setImageResource(Utility.getDrawableAttachmentFromType(type))
    }

    fun setProgress(progress: Int){
        progressBar.progress = progress
        if (progress == 100){
            progressBar.visibility = View.GONE
            statusView.visibility = View.VISIBLE
            containerView.alpha = 1f
        } else {
            progressBar.visibility = View.VISIBLE
            statusView.visibility = View.GONE
            containerView.alpha = 0.6f
        }
    }

}