package com.email.scenes.mailbox

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.holders.EmailHolder
import com.email.utils.FlipAnimator
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by sebas on 1/23/18.
 */

class EmailThreadAdapter(val mContext : Context,
                         var threadListener : OnThreadEventListener?,
                         val threadListHandler: MailboxActivity.ThreadListHandler)
    : RecyclerView.Adapter<EmailHolder>() {

    var isMultiSelectMode = false

    override fun onBindViewHolder(holder: EmailHolder?, position: Int) {
        if(holder == null) return
        val mail = threadListHandler.getThreadFromIndex(position)
        holder.bindMail(mail)

        holder.itemView?.findViewById<CircleImageView>(R.id.mail_item_left_name)?.setOnClickListener({
            threadListener?.onToggleThreadSelection(mContext, mail, holder, position)
        })

        if (isMultiSelectMode) {
            holder.itemView?.setOnClickListener({
                threadListener?.onToggleThreadSelection(mContext, mail, holder, position)
            })
        } else {
            holder.itemView?.setOnClickListener({
                TODO("GO TO MAIL")
            })
        }

        holder.itemView?.setOnLongClickListener(
                {
                    threadListener?.onToggleThreadSelection(mContext, mail, holder, position)
                    true
                })

        if (isMultiSelectMode) {
            holder.toggleMultiselect(mail.isSelected)
        } else {
            holder.hideMultiselect()
        }

        if (mail.isSelected) {
            holder.avatarView?.visibility = View.GONE
            holder.iconBack?.visibility = View.VISIBLE
        } else {
            holder.avatarView?.visibility = View.VISIBLE
            holder.iconBack?.visibility = View.GONE
        }
    }

    fun applyIconAnimation(holder: EmailHolder, mail: EmailThread, mContext: Context) {
        if (mail.isSelected) {
            holder.avatarView.visibility = View.GONE
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE)
            holder.iconBack.setAlpha(1.toFloat())
            FlipAnimator.flipView(mContext,
                    holder.iconBack,
                    holder.avatarView,
                    true);
        } else if(!mail.isSelected){
            holder.iconBack.setVisibility(View.GONE)
            resetIconYAxis(holder.avatarView)
            holder.avatarView.setVisibility(View.VISIBLE);
            FlipAnimator.flipView(mContext, holder.iconBack, holder.avatarView, false);

        }
    }

    private fun resetIconYAxis(view : View) {
        if (view.rotationY != 0.toFloat() ) {
            view.setRotationY(0.toFloat());
        }
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
        fun onToggleThreadSelection(context: Context, thread: EmailThread, emailHolder: EmailHolder, position: Int)
    }
}
