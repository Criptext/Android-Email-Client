package com.email.scenes.composer.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.scenes.composer.data.ComposerAttachment
import com.email.scenes.composer.ui.holders.AttachmentViewHolder

class AttachmentListAdapter(private val mContext: Context, private val attachmentsList: LinkedHashMap<String, ComposerAttachment>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val mView = LayoutInflater.from(mContext).inflate(R.layout.attachment, parent, false)
        return AttachmentViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return attachmentsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val attachment = attachmentsList.toList().get(position).second ?: return
        var mView = holder as AttachmentViewHolder ?: return
        mView.filename.text = attachment.filepath.split("/").last()
        mView.progressBar.progress = attachment.uploadProgress
    }

}