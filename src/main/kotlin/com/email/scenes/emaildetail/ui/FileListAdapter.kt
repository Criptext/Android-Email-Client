package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.db.models.CRFile
import com.email.scenes.composer.data.ComposerAttachment
import com.email.scenes.composer.ui.holders.AttachmentViewHolder
import com.email.scenes.composer.ui.holders.AttachmentViewObserver
import com.email.utils.Utility
import com.email.utils.file.FileUtils

class FileListAdapter(private val mContext: Context, private val attachmentsList: List<CRFile>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var observer: AttachmentViewObserver? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val mView = LayoutInflater.from(mContext).inflate(R.layout.attachment, parent, false)
        return AttachmentViewHolder(mView, observer)
    }

    override fun getItemCount(): Int {
        return attachmentsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val attachment = attachmentsList[position]
        val mView = holder as AttachmentViewHolder
        val type = FileUtils.getAttachmentTypeFromPath(attachment.name)
        mView.setFields(name = attachment.name, size = attachment.size, type = type)
        mView.setProgress(attachment.progress)
        mView.hideRemoveImage()
    }

}