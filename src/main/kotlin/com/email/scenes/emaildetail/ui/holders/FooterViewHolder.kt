package com.email.scenes.emaildetail.ui.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import com.email.R

/**
 * Created by sebas on 3/19/18.
 */


class FooterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val context = view.context
    val forward : Button
    val reply: Button
    val replyAll: Button

    init {
        forward = view.findViewById(R.id.forward)
        reply = view.findViewById(R.id.reply)
        replyAll = view.findViewById(R.id.reply_all)
    }

}
