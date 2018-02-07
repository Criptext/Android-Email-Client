package com.email.scenes.mailbox.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.email.R

/**
 * Created by sebas on 2/6/18.
 */

class ToolbarHolder(val view: View) {
    private val title: TextView
    private val numberEmails: TextView
    private val navButton: ImageView
    init {
        title = view.findViewById(R.id.mailbox_toolbar_title)
        numberEmails = view.findViewById(R.id.mailbox_number_emails)
        navButton = view.findViewById(R.id.mailbox_nav_button)
    }

    fun showMultiModeBar(selectedThreadsQuantity: Int){
        navButton.visibility = View.GONE
        numberEmails.visibility = View.GONE
        title.text = selectedThreadsQuantity.toString()
    }

    fun hideMultiModeBar() {

        navButton.visibility = View.VISIBLE
        numberEmails.visibility = View.VISIBLE
        title.text = "INBOX"
    }

    fun updateToolbarTitle(title: String) {
        this.title.text = title
    }
}