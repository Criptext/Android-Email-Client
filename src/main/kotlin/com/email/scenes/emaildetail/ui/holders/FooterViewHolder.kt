package com.email.scenes.emaildetail.ui.holders

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.email.R
import com.email.scenes.emaildetail.ui.FullEmailListAdapter

/**
 * Created by sebas on 3/19/18.
 */

class FooterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    val forward : Button
    val reply: Button
    val replyAll: Button

    init {
        forward = view.findViewById(R.id.forward)
        reply = view.findViewById(R.id.reply)
        replyAll = view.findViewById(R.id.reply_all)
    }

    fun setListeners(emailListener: FullEmailListAdapter.OnFullEmailEventListener?) {

        forward.setOnClickListener{
            emailListener?.onForwardBtnClicked()
        }

        reply.setOnClickListener{
            emailListener?.onReplyBtnClicked()
        }

        replyAll.setOnClickListener{
            emailListener?.onReplyAllBtnClicked()
        }
    }

}
