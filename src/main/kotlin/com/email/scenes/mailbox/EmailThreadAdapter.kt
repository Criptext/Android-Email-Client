package com.email.scenes.mailbox

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.holders.EmailHolder

/**
 * Created by sebas on 1/23/18.
 */

class EmailThreadAdapter(val mContext : Context,
                         var threadListener : OnThreadEventListener?,
                         val threadListHandler: MailboxActivity.ThreadListHandler)
    : RecyclerView.Adapter<EmailHolder>() {

    var isMultiSelectMode = false

    fun toggleThreadSelection(mContext: Context,
                              mailThread: EmailThread,
                              position: Int) {
        threadListener?.onToggleThreadSelection(mContext, mailThread, position)
    }

    fun goToMail(){
        TODO("GO TO MAIL")
    }

    override fun onBindViewHolder(holder: EmailHolder?, position: Int) {
        if(holder?.itemView == null) return
        val mail = threadListHandler.getThreadFromIndex(position)
        holder.bindMail(mail)
        holder.avatarView.setOnClickListener{
            toggleThreadSelection(mContext, mail, position)
        }
        holder.iconBack.setOnClickListener{
            toggleThreadSelection(mContext, mail, position)
        }

        if (isMultiSelectMode) {
            holder.itemView.setOnClickListener {
            }
        } else {
            holder.itemView.setOnClickListener{
                goToMail()
            }
        }

        holder.itemView.setOnLongClickListener(
                {
                    toggleThreadSelection(mContext, mail, position)
                    true
                })

        holder.toggleSelectedStatus(mail.isSelected)
    }

    override fun getItemCount(): Int {
        return threadListHandler.getEmailThreadsCount()
    }


    private fun createMailItemView(): View {
        val mailItemView = View.inflate(mContext, R.layout.mail_item, null)
        return mailItemView
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailHolder {
        val itemView : View = createMailItemView()
        return EmailHolder(itemView)
    }



    interface OnThreadEventListener{
        fun onToggleThreadSelection(context: Context, thread: EmailThread, position: Int)
    }
}
