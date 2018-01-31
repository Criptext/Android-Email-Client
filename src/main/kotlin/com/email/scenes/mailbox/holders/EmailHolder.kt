package com.email.scenes.mailbox.holders

import android.content.Context
import android.media.Image
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.email.DB.models.Email
import com.email.R
import com.email.scenes.MailItemHolder
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/24/18.
 */

class EmailHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener , MailItemHolder {

    private val headerView : TextView
    private val subjectView : TextView
    private val checkedItemImageView: ImageView
    private val previewView : TextView
    private val dateView : TextView
    private val countView : TextView
    private val context : Context

    init {
        view.setOnClickListener(this)
    }
    override fun onClick(p0: View?) {
    }


    fun bindMail(emailThread: EmailThread) {
         previewView.text = emailThread.headerPreview
        headerView.text = emailThread.headerPreview
    }

    override fun fillIcons() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drawInMultiSelectMode() {
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
    }

    override fun drawInNormalMode() {
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
    }

    fun toggleMultiselect(selected : Boolean){
        if(selected) {
            headerView.text  = "selected..."
        }else {
            headerView.text = "not chosen"
        }
    }

    fun hideMultiselect() {
        headerView.text = "not chosen"
    }

    init {
        headerView = view.findViewById(R.id.email_header) as TextView
        subjectView = view.findViewById(R.id.email_subject) as TextView
        previewView = view.findViewById(R.id.email_preview) as TextView
        checkedItemImageView = view.findViewById(R.id.mail_item_checked) as ImageView
        dateView = view.findViewById(R.id.email_date) as TextView
        countView = view.findViewById(R.id.email_count) as TextView
        context = view.context
    }
}
