package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.emaildetail.ui.holders.FullEmailHolder
import com.email.scenes.emaildetail.ui.holders.ParentEmailHolder
import com.email.scenes.emaildetail.ui.holders.PartialEmailHolder
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/12/18.
 */


class FullEmailListAdapter(private val mContext : Context,
                           var fullEmailListener : OnFullEmailEventListener?,
                           private val fullEmails: VirtualList<FullEmail>)
    : RecyclerView.Adapter<ParentEmailHolder>() {

    override fun getItemViewType(position : Int) : Int{
        val email = fullEmails[position]

        if(email.viewOpen) {
/* TODO(use this later)
            if(email.hasDraftLabel()){
                return EmailViewTypes.draft.ordinal
            }
*/

            return EmailViewTypes.fullEmail.ordinal
        }

        return EmailViewTypes.partialEmail.ordinal
    }

    override fun onBindViewHolder(
            holder: ParentEmailHolder?,
            position: Int) {
        val fullEmail = fullEmails[position]

        holder?.setListeners(
                fullEmail = fullEmail,
                adapter = this,
                emailListener = fullEmailListener,
                position = position)
    }

    override fun getItemCount() = fullEmails.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentEmailHolder {
        val mView: View

        return when(EmailViewTypes.values()[viewType]) {

            EmailViewTypes.fullEmail -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.open_full_mail_item, null)
                FullEmailHolder(mView)
            }
            EmailViewTypes.partialEmail -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.partial_email_holder, null)
                PartialEmailHolder(mView)
            }
            else -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.open_full_mail_item, null)
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

        fun onShowContactsToView(fullEmail: FullEmail)
    }

    private enum class EmailViewTypes {
        draft, fullEmail, fullSentEmail, partialEmail;
    }
}
