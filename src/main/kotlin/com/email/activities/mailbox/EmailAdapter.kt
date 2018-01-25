package com.email.activities.mailbox

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.activities.mailbox.data.EmailThread
import com.email.activities.mailbox.holders.MailHolder

/**
 * Created by sebas on 1/23/18.
 */

class EmailAdapter(private val mails : ArrayList<EmailThread>): RecyclerView.Adapter<MailHolder>() {
    override fun onBindViewHolder(holder: MailHolder?, position: Int) {
        val mail = mails[position]
        holder?.bindMail(mail)
    }

    override fun getItemCount(): Int {
        return mails.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MailHolder {
        val inflatedView = LayoutInflater.from(parent!!.context).inflate(R.layout.mail_item, parent, false)
        return MailHolder(inflatedView)
    }

}
