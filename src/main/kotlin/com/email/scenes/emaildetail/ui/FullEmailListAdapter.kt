package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.db.models.FullEmail
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/12/18.
 */


class FullEmailListAdapter(private val mContext : Context,
                           var fullEmailListener : OnFullEmailEventListener?,
                           private val fullEmails: VirtualList<FullEmail>)
    : RecyclerView.Adapter<FullEmailHolder>() {

    fun toggleFullEmailSelection(mContext: Context,
                                 fullEmail: FullEmail,
                                 position: Int) {
        fullEmailListener?.onToggleFullEmailSelection(mContext, fullEmail, position)
    }

    override fun getItemViewType(position : Int) : Int{
        val email = fullEmails[position]
        if(email.hasDraftLabel()){
            return EmailViewTypes.draft.ordinal
        }

        return EmailViewTypes.fullEmail.ordinal
    }

    override fun onBindViewHolder(
            holder: FullEmailHolder?,
            position: Int) {
        val fullEmail = fullEmails[position]

        holder?.setListeners(
                fullEmail = fullEmail,
                adapter = this,
                emailListener = fullEmailListener,
                position = position)
    }

    override fun getItemCount() = fullEmails.size


    private fun createFullEmailItemView(): View {
        val mailItemView = View.inflate(mContext, R.layout.open_full_mail_item, null)
        return mailItemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullEmailHolder {
        val mView: View
        val fullEmailHolder : FullEmailHolder

        when(EmailViewTypes.values()[viewType]) {

            EmailViewTypes.fullEmail -> {
                mView = LayoutInflater.from(mContext).inflate(R.layout.open_full_mail_item, null)
                fullEmailHolder = FullEmailHolder(mView)
            } else -> { // change this when all the cases appear
            mView = LayoutInflater.from(mContext).inflate(R.layout.open_full_mail_item, null)
            fullEmailHolder = FullEmailHolder(mView)
            }
        }
        return fullEmailHolder
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

    }

    private enum class EmailViewTypes {
        draft, fullEmail, fullSentEmail, partialEmail;
    }
}
