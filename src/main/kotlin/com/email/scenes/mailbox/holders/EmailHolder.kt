package com.email.scenes.mailbox.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.email.DB.models.Email
import com.email.R
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/24/18.
 */

class EmailHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

    private val headerView : TextView
    private val subjectView : TextView
    private val previewView : TextView
    private val dateView : TextView
    private val countView : TextView
    private val regularAttachIcon : View
    private val reFwIcon : ImageView
    private val multiselectIcon : ImageView

    init {
        view.setOnClickListener(this)
    }
    override fun onClick(p0: View?) {
    }


    fun bindMail(emailThread: EmailThread) {
         previewView.text = emailThread.headerPreview // ????
    }

    init {
        headerView = view.findViewById(R.id.email_header) as TextView
        subjectView = view.findViewById(R.id.email_subject) as TextView
        previewView = view.findViewById(R.id.email_preview) as TextView
        dateView = view.findViewById(R.id.email_date) as TextView
        countView = view.findViewById(R.id.email_count) as TextView
        multiselectIcon = view.findViewById(R.id.multi_icon) as ImageView
        regularAttachIcon = view.findViewById(R.id.regular_attach_icon)
        reFwIcon = view.findViewById(R.id.re_fw_icon) as ImageView

    }
}
