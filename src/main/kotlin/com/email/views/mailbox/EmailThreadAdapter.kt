package com.email.views.mailbox

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.views.mailbox.data.EmailThread
import com.email.views.mailbox.holders.EmailHolder

/**
 * Created by sebas on 1/23/18.
 */

class EmailThreadAdapter(private val mails : ArrayList<EmailThread>): RecyclerView.Adapter<EmailHolder>() {
    override fun onBindViewHolder(holder: EmailHolder?, position: Int) {
        val mail = mails[position]
        holder?.bindMail(mail)
    }

    override fun getItemCount(): Int {
        return mails.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EmailHolder {
        val inflatedView = LayoutInflater.from(parent!!.context).inflate(R.layout.mail_item, parent, false)
        return EmailHolder(inflatedView)
    }

}
