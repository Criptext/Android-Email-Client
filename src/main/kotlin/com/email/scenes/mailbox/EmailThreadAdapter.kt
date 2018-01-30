package com.email.scenes.mailbox

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.holders.EmailHolder

/**
 * Created by sebas on 1/23/18.
 */

class EmailThreadAdapter(val mContext : Context, var threadListener : OnThreadEventListener?): RecyclerView.Adapter<EmailHolder>() {

    var threads = ArrayList<EmailThread>()
    var recyclerView: RecyclerView? = null

    override fun onBindViewHolder(holder: EmailHolder?, position: Int) {
        val mail = threads[position]
        holder?.bindMail(mail)
    }

    override fun getItemCount(): Int {
        return threads.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EmailHolder {
        val inflatedView = LayoutInflater.from(parent!!.context).inflate(R.layout.mail_item, parent, false)
        return EmailHolder(inflatedView)
    }


    interface OnThreadEventListener{
        fun onThreadOpened(id : String)
    }
}
