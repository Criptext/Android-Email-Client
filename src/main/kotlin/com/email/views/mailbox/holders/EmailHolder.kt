package com.email.views.mailbox.holders

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.email.DB.models.Email
import com.email.R
import com.email.views.mailbox.data.EmailThread

/**
 * Created by sebas on 1/24/18.
 */

class EmailHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

    private var email : Email? = null // cambiar esto. EmailThread*
    private var headerView : TextView? = null
    private var subjectView : TextView? = null
    private var previewView : TextView? = null
    private var dateView : TextView? = null
    private var countView : TextView? = null
    private var attachAlert : ImageView? = null
    private var multiselectIcon : ImageView? = null
    private var timerAlert : ImageView? = null
    private var criptextIconsLayout : View? = null
    private var regularAttachIcon : View? = null
    private var reFwIcon : ImageView? = null
    private val context = view.context

    init {
        view.setOnClickListener(this)
    }
    override fun onClick(p0: View?) {
    }


    fun bindMail(emailThread: EmailThread) {
         previewView!!.text = emailThread.headerPreview // ????
    }

    init {
        headerView = view.findViewById(R.id.email_header) as TextView?
        subjectView = view.findViewById(R.id.email_subject) as TextView?
        previewView = view.findViewById(R.id.email_preview) as TextView?
        dateView = view.findViewById(R.id.email_date) as TextView?
        countView = view.findViewById(R.id.email_count) as TextView?
        multiselectIcon = view.findViewById(R.id.multi_icon) as ImageView?
        criptextIconsLayout = view.findViewById(R.id.criptext_icons_layout)
        regularAttachIcon = view.findViewById(R.id.regular_attach_icon)
        reFwIcon = view.findViewById(R.id.re_fw_icon) as ImageView?

    }
}
