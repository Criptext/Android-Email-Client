package com.criptext.mail.scenes.composer.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.ui.holders.AttachmentViewHolder
import com.criptext.mail.scenes.composer.ui.holders.AttachmentViewObserver
import com.criptext.mail.utils.file.FileUtils

class AttachmentListAdapter(private val mContext: Context, private val attachmentsList: List<ComposerAttachment>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var observer: AttachmentViewObserver? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView = LayoutInflater.from(mContext).inflate(R.layout.attachment, parent, false)
        return AttachmentViewHolder(mView, observer)
    }

    override fun getItemCount(): Int {
        return attachmentsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val attachment = attachmentsList[position]
        val mView = holder as AttachmentViewHolder
        mView.setFields(name = FileUtils.getName(attachment.filepath), size = attachment.size, type = attachment.type)
        mView.setProgress(attachment.uploadProgress)
    }

    interface OnAttachmentListener{
        fun onNewCamAttachmentRequested()
        fun onNewFileAttachmentRequested()
        fun onNewGalleryAttachmentRequested()
    }

}