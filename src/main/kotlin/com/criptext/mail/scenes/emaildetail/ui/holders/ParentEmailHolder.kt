package com.criptext.mail.scenes.emaildetail.ui.holders

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter

/**
 * Created by sebas on 3/14/18.
 */

abstract class ParentEmailHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val context = view.context
    val headerView : TextView
    val dateView: TextView
    val bodyView: TextView

    init {
        headerView = view.findViewById(R.id.email_header)
        dateView = view.findViewById(R.id.email_date)
        bodyView = view.findViewById(R.id.email_preview)
    }

    abstract fun bindFullMail(fullEmail: FullEmail)
    abstract fun setListeners(fullEmail: FullEmail, fileDetails: List<FileDetail>,
                              emailListener: FullEmailListAdapter.OnFullEmailEventListener?,
                              adapter: FullEmailListAdapter, position: Int)
}
