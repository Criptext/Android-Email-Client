package com.email.scenes.mailbox.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.holders.EmailHolder
import com.email.utils.VirtualList
import com.email.utils.ui.ProgressViewHolder

/**
 * Created by sebas on 1/23/18.
 */

class EmailThreadAdapter(private val mContext : Context,
                         var threadListener : OnThreadEventListener?,
                         private val threadList: VirtualList<EmailThread?>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_ITEM = 1
    private val VIEW_PROGRESS = 0
    var isMultiSelectMode = false

    fun toggleThreadSelection(mContext: Context,
                              mailThread: EmailThread,
                              position: Int) {
        threadListener?.onToggleThreadSelection(mContext, mailThread, position)
    }

    fun goToMail(emailThread: EmailThread) {
        threadListener?.onGoToMail(emailThread = emailThread)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(holder) {
            is EmailHolder? -> {
                if (holder?.itemView == null) return
                val mail = threadList[position]!!
                holder.bindMail(mail)
                val itemClickListener = {
                    toggleThreadSelection(mContext, mail, position)
                }
                holder.setOnAvatarClickedListener(itemClickListener)
                holder.setOnIconBackClickedListener(itemClickListener)

                if (isMultiSelectMode) {
                    holder.itemView.setOnClickListener {
                    }
                } else {
                    holder.itemView.setOnClickListener {
                        goToMail(emailThread = mail)
                    }
                }

                holder.itemView.setOnLongClickListener(
                        {
                            toggleThreadSelection(mContext, mail, position)
                            true
                        })

                holder.toggleStatus(mail.isSelected, mail.unread)
            }
        }
    }

    override fun getItemCount() = threadList.size


    private fun createMailItemView(): View {
        val mailItemView = View.inflate(mContext, R.layout.mail_item, null)
        return mailItemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_ITEM) {
            val itemView: View = createMailItemView()
            return EmailHolder(itemView)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.progress_item,
                    parent,
                    false)

            return ProgressViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(threadList[position] != null) VIEW_ITEM else VIEW_PROGRESS
    }

    interface OnThreadEventListener {
        fun onToggleThreadSelection(context: Context, thread: EmailThread, position: Int)
        fun onGoToMail(emailThread: EmailThread)
    }

}
