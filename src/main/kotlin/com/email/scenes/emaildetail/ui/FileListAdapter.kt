package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.db.AttachmentTypes
import com.email.db.models.CRFile
import com.email.db.models.FileDetail
import com.email.scenes.composer.data.ComposerAttachment
import com.email.scenes.composer.ui.holders.AttachmentViewHolder
import com.email.scenes.composer.ui.holders.AttachmentViewObserver
import com.email.utils.Utility
import com.email.utils.file.FileUtils

class FileListAdapter(private val mContext: Context, private val attachmentsList: List<FileDetail>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var observer: AttachmentViewObserver? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView = LayoutInflater.from(mContext).inflate(R.layout.attachment, parent, false)
        val attachmentViewHolder = AttachmentViewHolder(mView, observer)
        attachmentViewHolder.hideRemoveImage()
        return attachmentViewHolder
    }

    override fun getItemCount(): Int {
        return attachmentsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val attachment = attachmentsList[position]
        val mView = holder as AttachmentViewHolder
        if(attachment.status == 0)
            mView.setFields(name = attachment.name, size = attachment.size, type = AttachmentTypes.UNSEND)
        else
            mView.setFields(name = attachment.name, size = attachment.size, type = attachment.type)
        mView.setProgress(attachment.progress)
        mView.hideRemoveImage()
    }
}