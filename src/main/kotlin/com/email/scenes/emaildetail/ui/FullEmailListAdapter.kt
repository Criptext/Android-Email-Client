package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.ui.holders.FooterViewHolder
import com.email.scenes.emaildetail.ui.holders.FullEmailHolder
import com.email.scenes.emaildetail.ui.holders.ParentEmailHolder
import com.email.scenes.emaildetail.ui.holders.PartialEmailHolder
import com.email.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */


class FullEmailListAdapter(private val mContext : Context,
                           var fullEmailListener : OnFullEmailEventListener?,
                           private val fullEmails: VirtualList<FullEmail>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private fun isPositionFooter(position: Int): Boolean {
        return position == fullEmails.size
    }

    override fun getItemViewType(position : Int) : Int{
        if(isPositionFooter(position)) {
            return EmailViewTypes.FOOTER.ordinal
        }

        val email = fullEmails[position]

        if(email.viewOpen) {
            return EmailViewTypes.fullEmail.ordinal
        }

        return EmailViewTypes.partialEmail.ordinal
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder?,
            position: Int) {
        when(holder){
            is ParentEmailHolder -> {
                val fullEmail = fullEmails[position]

                holder.bindFullMail(fullEmail)
                holder.setListeners(
                        fullEmail = fullEmail,
                        adapter = this,
                        emailListener = fullEmailListener,
                        position = position)
            }
            is FooterViewHolder -> {
                holder.setListeners(emailListener = fullEmailListener)
            }
        }
    }

    override fun getItemCount(): Int {
        return fullEmails.size + 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val mView: View

        return when(EmailViewTypes.values()[viewType]) {

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



    interface OnFullEmailEventListener{
        fun onToggleFullEmailSelection(context: Context, fullEmail: FullEmail, position: Int) // va esto(?)
        fun onReplyOptionSelected(
                fullEmail: FullEmail,
                position: Int,
                all: Boolean)
        fun onToggleReadOption(fullEmail: FullEmail,
                               position: Int,
                               markAsRead: Boolean)
        fun onDeleteOptionSelected(fullEmail: FullEmail,
                                   position: Int)

        fun ontoggleViewOpen(fullEmail: FullEmail, position: Int, viewOpen: Boolean)
        fun onForwardBtnClicked()
        fun onReplyBtnClicked()
        fun onReplyAllBtnClicked()
        fun onUnsendEmail(fullEmail: FullEmail, position: Int)
        fun onForwardOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean)
        fun onReplyAllOptionSelected(fullEmail: FullEmail, position: Int, all: Boolean)
    }

    private enum class EmailViewTypes {
        draft, fullEmail, fullSentEmail, partialEmail, FOOTER
    }
}
