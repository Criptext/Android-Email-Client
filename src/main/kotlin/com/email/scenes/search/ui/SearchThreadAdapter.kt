package com.email.scenes.search.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.db.models.Label
import com.email.email_preview.EmailPreview
import com.email.scenes.search.VirtualSearchThreadList
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.holders.EmailHolder
import com.email.utils.ui.EmptyViewHolder
import com.email.utils.virtuallist.VirtualListAdapter

/**
 * Created by sebas on 1/23/18.
 */

class SearchThreadAdapter(private val mContext : Context,
                          private val threadListener : OnThreadEventListener,
                          private val threadList: VirtualSearchThreadList)
    : VirtualListAdapter(threadList) {

    private fun toggleThreadSelection(mailThread: EmailThread, position: Int) {
        threadListener.onToggleThreadSelection(mailThread, position)
    }

    private fun goToMail(emailThread: EmailThread) {
        threadListener.onGoToMail(emailThread = emailThread)
    }

    override fun onApproachingEnd() {
        threadListener.onApproachingEnd()
    }

    private fun setEmailHolderListeners(holder: EmailHolder, position: Int, mail: EmailThread) {
        val itemClickListener = {
            toggleThreadSelection(mail, position)
        }
        holder.setOnAvatarClickedListener(itemClickListener)
        holder.setOnIconBackClickedListener(itemClickListener)

        holder.itemView.setOnClickListener {
            goToMail(emailThread = mail)
        }

        holder.itemView.setOnLongClickListener({
            toggleThreadSelection(mail, position)
            true
        })
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is EmailHolder -> {
                if (holder.itemView == null) return
                val mail = threadList[position]
                // TODO use email preview virtual list
                holder.bindEmailPreview(EmailPreview.fromEmailThread(mail))
                setEmailHolderListeners(holder, position, mail)
            }
        }
    }

    private fun createMailItemView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.mail_item, parent, false)
    }

    override fun createEmptyViewHolder(parent: ViewGroup): EmptyViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_no_search_results, parent,
                        false)
        return EmptyViewHolder(inflatedView)
    }

    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View = createMailItemView(parent)
        return EmailHolder(itemView)
    }

    override fun getActualItemViewType(position: Int): Int {
        return 1
    }

    interface OnThreadEventListener {
        fun onToggleThreadSelection(thread: EmailThread, position: Int)
        fun onGoToMail(emailThread: EmailThread)
        fun onApproachingEnd()
    }

    override fun getActualItemId(position: Int): Long {
        return threadList[position].id
    }
}
