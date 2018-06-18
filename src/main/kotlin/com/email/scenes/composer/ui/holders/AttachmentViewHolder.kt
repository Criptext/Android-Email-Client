package com.email.scenes.composer.ui.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import com.email.R
import com.email.db.AttachmentTypes
import com.email.utils.file.FileUtils

class AttachmentViewHolder(val view: View, val observer: AttachmentViewObserver?) : RecyclerView.ViewHolder(view) {

    val progressBar : ProgressBar = view.findViewById(R.id.attachment_progress_bar)
    val filename: TextView = view.findViewById(R.id.attachment_filename)
    val filesize: TextView = view.findViewById(R.id.attachment_size)
    val statusView: ImageView = view.findViewById(R.id.status_view)
    val typeView: ImageView = view.findViewById(R.id.attachment_type_image)
    val containerView: RelativeLayout = view.findViewById(R.id.attachment_container)
    val removeButton: Button = view.findViewById(R.id.attachment_remove)

    init {
        statusView.visibility = View.GONE
        view.setOnClickListener {
            observer?.onViewClick(adapterPosition)
        }
        removeButton.setOnClickListener {
            observer?.onRemoveAttachmentClicked(adapterPosition)
        }
    }

    fun setFields(name: String, size: Long, type: AttachmentTypes){
        filename.text = name
        filesize.text = FileUtils.readableFileSize(size)
        typeView.setImageResource(type.getDrawableImage())
    }

    fun setProgress(progress: Int){
        progressBar.progress = progress
        when {
            progress < 0 -> {
                progressBar.visibility = View.GONE
                statusView.visibility = View.GONE
                containerView.alpha = 1f
            }
            progress == 100 -> {
                progressBar.visibility = View.GONE
                statusView.visibility = View.VISIBLE
                containerView.alpha = 1f
            }
            else -> {
                progressBar.visibility = View.VISIBLE
                statusView.visibility = View.GONE
                containerView.alpha = 0.6f
            }
        }
    }

}