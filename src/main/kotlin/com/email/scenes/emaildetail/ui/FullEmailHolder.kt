package com.email.scenes.emaildetail.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.email.R

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailHolder(val view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

    private val context = view.context
    private val layout : FrameLayout
    private val contactView : TextView
    private val toView: TextView
    private val dateView: TextView
    private val bodyView: TextView
    private val layoutAttachment : RelativeLayout

    override fun onClick(p0: View?) {
    }

    init {
        layout = view.findViewById(R.id.open_full_mail_item_container)
        contactView = view.findViewById(R.id.contact)
        toView = view.findViewById(R.id.to)
        dateView = view.findViewById(R.id.date)
        bodyView = view.findViewById(R.id.body)
        layoutAttachment = view.findViewById(R.id.open_full_mail_attachment_container)
    }

}
