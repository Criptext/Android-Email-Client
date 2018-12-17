package com.criptext.mail.scenes.emaildetail.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.criptext.mail.R
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.composer.ui.holders.AttachmentViewObserver
import com.criptext.mail.scenes.emaildetail.ui.holders.*
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */


class FullEmailListAdapter(private val mContext : Context,
                           var fullEmailListener : OnFullEmailEventListener?,
                           private val fullEmails: VirtualList<FullEmail>,
                           private val fileDetails: Map<Long, List<FileDetail>>,
                           private val labels: VirtualList<Label>,
                           private val isStarred: Boolean )
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private lateinit var headerHolder:HeaderViewHolder

    private fun isPositionFooter(position: Int): Boolean {
        return position == fullEmails.size + 1
    }

    override fun getItemViewType(position : Int) : Int{
        if(isPositionFooter(position)) {
            return EmailViewTypes.FOOTER.ordinal
        }

        if(position == 0)
            return EmailViewTypes.HEADER.ordinal


        val email = fullEmails[position - 1]

        if (email.viewOpen) {
            return EmailViewTypes.fullEmail.ordinal
        }

        return EmailViewTypes.partialEmail.ordinal
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int) {
        when(holder){
            is HeaderViewHolder -> {
                headerHolder = holder
                headerHolder.setListeners(emailListener = fullEmailListener)
            }
            is ParentEmailHolder -> {
                val fullEmail = fullEmails[position - 1]

                holder.bindFullMail(fullEmail)
                holder.setListeners(
                        fullEmail = fullEmail,
                        adapter = this,
                        emailListener = fullEmailListener,
                        position = position,
                        fileDetails = fileDetails[fullEmail.email.id] ?: emptyList())
            }
            is FooterViewHolder -> {
                holder.setListeners(emailListener = fullEmailListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return fullEmails.size + 2
    }

    override fun getItemId(position: Int): Long {
        if(isPositionFooter(position)) {
            return -1
        }

        if(position == 0)
            return -2


        val email = fullEmails[position - 1]

        return email.email.id
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView: View

        return when(EmailViewTypes.values()[viewType]) {

            EmailViewTypes.HEADER -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.layout_header_email_detail, parent, false)
                val subject = if (fullEmails[0].email.subject.isEmpty())
                        mContext.getString(R.string.nosubject)
                    else fullEmails[0].email.subject
                HeaderViewHolder(mView, subject, labels, isStarred)
            }

            EmailViewTypes.FOOTER -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.layout_btns_email_detail, parent, false)
                FooterViewHolder(mView)
            }

            EmailViewTypes.fullEmail -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.open_full_mail_item, parent, false)
                FullEmailHolder(mView)
            }
            EmailViewTypes.partialEmail -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.partial_email_holder, parent, false)
                PartialEmailHolder(mView)
            }
            else -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.open_full_mail_item, parent, false)
                FullEmailHolder(mView)
            }
        }
    }

    fun notifyLabelsChanged(updatedLabels: VirtualList<Label>, updatedHasStar: Boolean){
        headerHolder.notifyLabelsChanged(updatedLabels, updatedHasStar)
    }



    interface OnFullEmailEventListener{
        fun onReplyOptionSelected(
                fullEmail: FullEmail,
                position: Int,
                all: Boolean)
        fun onToggleReadOption(fullEmail: FullEmail,
                               position: Int,
                               markAsRead: Boolean)
        fun onDeleteOptionSelected(fullEmail: FullEmail,
                                   position: Int)
        fun onSpamOptionSelected(fullEmail: FullEmail,
                                   position: Int)
        fun onPrintOptionSelected(fullEmail: FullEmail)
        fun ontoggleViewOpen(fullEmail: FullEmail, position: Int, viewOpen: Boolean)
        fun onForwardBtnClicked()
        fun onReplyBtnClicked()
        fun onReplyAllBtnClicked()
        fun onUnsendEmail(fullEmail: FullEmail, position: Int)
        fun onStarredButtonPressed(isStarred: Boolean)
        fun onForwardOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean)
        fun onReplyAllOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean)
        fun onContinueDraftOptionSelected(fullEmail: FullEmail)
        fun onAttachmentSelected(emailPosition: Int, attachmentPosition: Int)
        fun showStartGuideMenu(view: View)
        fun showStartGuideEmailIsRead(view: View)
    }

    private enum class EmailViewTypes {
        HEADER, draft, fullEmail, fullSentEmail, partialEmail, FOOTER
    }
}
