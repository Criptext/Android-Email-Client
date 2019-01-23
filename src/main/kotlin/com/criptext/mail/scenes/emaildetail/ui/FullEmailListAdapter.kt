package com.criptext.mail.scenes.emaildetail.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
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
                           private val isStarred: Boolean,
                           private val shouldOpenExpanded: Boolean)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private val MAX_SIZE_FOR_COLLAPSE = 5
    var isExpanded = shouldOpenExpanded


    private lateinit var headerHolder:HeaderViewHolder

    private fun isPositionFooter(position: Int): Boolean {
        return if(isExpanded)
            position == fullEmails.size + 1
        else
            MAX_SIZE_FOR_COLLAPSE - 1 == position
    }

    override fun getItemViewType(position : Int) : Int{

        if(!isExpanded){
            if (isPositionFooter(position)) {
                return EmailViewTypes.FOOTER.ordinal
            }

            if (position == 0)
                return EmailViewTypes.HEADER.ordinal


            if(position == 2){
                return EmailViewTypes.collapsedEmail.ordinal
            }

            val email = if(position == 1)
                    fullEmails[position - 1]
                else
                    fullEmails[fullEmails.size - 1]


            if (email.viewOpen) {
                return EmailViewTypes.fullEmail.ordinal
            }

            return EmailViewTypes.partialEmail.ordinal
        }else {
            if (isPositionFooter(position)) {
                return EmailViewTypes.FOOTER.ordinal
            }

            if (position == 0)
                return EmailViewTypes.HEADER.ordinal


            val email = fullEmails[position - 1]

            if (email.viewOpen) {
                return EmailViewTypes.fullEmail.ordinal
            }

            return EmailViewTypes.partialEmail.ordinal

        }
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int) {
        when(holder){
            is HeaderViewHolder -> {
                headerHolder = holder
                headerHolder.setListeners(emailListener = fullEmailListener)
            }
            is CollapsedViewHolder ->{
                holder.setListeners(fullEmailListener)
                holder.setNumber(fullEmails.size - 2)
            }
            is ParentEmailHolder -> {
                var newPosition = position
                val fullEmail = if(isExpanded) fullEmails[position - 1]
                else {
                    if(position == 1)
                        fullEmails[position - 1]
                    else {
                        newPosition = MAX_SIZE_FOR_COLLAPSE - 2
                        fullEmails[fullEmails.size - 1]
                    }
                }

                holder.bindFullMail(fullEmail)
                holder.setListeners(
                        fullEmail = fullEmail,
                        adapter = this,
                        emailListener = fullEmailListener,
                        position = newPosition,
                        fileDetails = fileDetails[fullEmail.email.id] ?: emptyList())
                if(!isExpanded){
                    if(position == 1) {
                        holder.setBottomMargin(0)
                        holder.setBackground(
                                ContextCompat.getDrawable(mContext, R.drawable.partial_email_drawable_top_collapsed)!!
                        )
                    }else if(position == MAX_SIZE_FOR_COLLAPSE - 2){
                        holder.setBackground(
                                ContextCompat.getDrawable(mContext, R.drawable.partial_email_drawable_bottom_collapsed)!!
                        )
                    }
                }else{
                    if(position == 1) {
                        holder.setBottomMargin(20)
                        holder.setBackground(
                                ContextCompat.getDrawable(mContext, R.drawable.partial_email_drawable)!!
                        )
                    }else if(position == itemCount - 2){
                        holder.setBackground(
                                ContextCompat.getDrawable(mContext, R.drawable.partial_email_drawable)!!
                        )
                    }
                }
            }
            is FooterViewHolder -> {
                holder.setListeners(emailListener = fullEmailListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return if(isExpanded) fullEmails.size + 2 else MAX_SIZE_FOR_COLLAPSE
    }

    override fun getItemId(position: Int): Long {
        if(isPositionFooter(position)) {
            return -1
        }

        if(position == 0)
            return -2

        if (!isExpanded && position == 2)
            return -3

        val email = if(isExpanded) fullEmails[position - 1]
        else {
            if(position == 1)
                fullEmails[position - 1]
            else
                fullEmails[fullEmails.size - 1]
        }

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
            EmailViewTypes.collapsedEmail -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.collapsed_email_holder, parent, false)
                CollapsedViewHolder(mView)
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
        fun onSourceOptionSelected(fullEmail: FullEmail)
        fun onRetrySendOptionSelected(fullEmail: FullEmail, position: Int)
        fun ontoggleViewOpen(fullEmail: FullEmail, position: Int, viewOpen: Boolean)
        fun onCollapsedClicked()
        fun onForwardBtnClicked()
        fun onReplyBtnClicked()
        fun onReplyAllBtnClicked()
        fun onUnsendEmail(fullEmail: FullEmail, position: Int)
        fun onStarredButtonPressed(isStarred: Boolean)
        fun onForwardOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean)
        fun onReplyAllOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean)
        fun onContinueDraftOptionSelected(fullEmail: FullEmail)
        fun onAttachmentSelected(emailPosition: Int, attachmentPosition: Int)
        fun onResourceLoaded(cid: String)
        fun showStartGuideMenu(view: View)
        fun showStartGuideEmailIsRead(view: View)
    }

    private enum class EmailViewTypes {
        HEADER, draft, fullEmail, collapsedEmail, fullSentEmail, partialEmail, FOOTER
    }
}
