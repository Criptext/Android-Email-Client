package com.email.scenes.mailbox.holders

import android.graphics.Typeface
import android.support.v4.widget.Space
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.email.R
import com.email.utils.ui.Tint

/**
 * Created by sebas on 2/6/18.
 */

class ToolbarHolder(val view: View) {
    private val title: TextView
    private val numberEmails: TextView
    private val navButton: ImageView
    private val backButton: ImageView
    private val separator: Space
    init {
        title = view.findViewById(R.id.mailbox_toolbar_title)
        numberEmails = view.findViewById(R.id.mailbox_number_emails)
        navButton = view.findViewById(R.id.mailbox_nav_button)
        backButton = view.findViewById(R.id.mailbox_back_button)
        separator = view.findViewById(R.id.mailbox_toolbar_multi_mode_separator)

        Tint.addTintToImage(view.context, backButton)
    }

    fun showMultiModeBar(selectedThreadsQuantity: Int){
        navButton.visibility = View.GONE
        numberEmails.visibility = View.GONE
        backButton.visibility = View.VISIBLE
        separator.visibility = View.VISIBLE
        title.text = selectedThreadsQuantity.toString()
    }

    fun hideMultiModeBar() {
        separator.visibility = View.GONE
        navButton.visibility = View.VISIBLE
        numberEmails.visibility = View.VISIBLE
        backButton.visibility = View.GONE
        title.text = "INBOX"
        title.typeface = Typeface.DEFAULT_BOLD
    }

    fun updateToolbarTitle(title: String) {
        this.title.text = title
    }
    fun updateNumberOfMails(emailsSize: Int) {
        numberEmails.text = "($emailsSize)"
    }
}