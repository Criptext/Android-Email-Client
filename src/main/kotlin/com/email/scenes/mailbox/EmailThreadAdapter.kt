package com.email.scenes.mailbox

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.holders.EmailHolder
import com.email.utils.VirtualList

/**
 * Created by sebas on 1/23/18.
 */

class EmailThreadAdapter(val mContext : Context,
                         var threadListener : OnThreadEventListener?,
                         val threadList: VirtualList<EmailThread>)
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
        val mail = threadList[position]
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

    override fun getItemCount() = threadList.size


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
