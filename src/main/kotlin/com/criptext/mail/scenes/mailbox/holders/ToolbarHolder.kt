package com.criptext.mail.scenes.mailbox.holders


import android.view.View
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import com.criptext.mail.R

/**
 * Created by sebas on 2/6/18.
 */

class ToolbarHolder(val view: View) {
    private val title: TextView
    private val numberEmails: TextView
    private val plusBadge: TextView
    private val navButton: ImageView
    private val navButtonRing: ImageView
    private val backButton: ImageView
    private val separator: Space

    init {
        title = view.findViewById(R.id.mailbox_toolbar_title)
        numberEmails = view.findViewById(R.id.mailbox_number_emails)
        plusBadge = view.findViewById(R.id.plusBadge)
        navButton = view.findViewById(R.id.mailbox_nav_button)
        navButtonRing = view.findViewById(R.id.plusBadgeRing)
        backButton = view.findViewById(R.id.mailbox_back_button)
        separator = view.findViewById(R.id.mailbox_toolbar_multi_mode_separator)
    }

    fun showMultiModeBar(selectedThreadsQuantity: Int){
        navButton.visibility = View.GONE
        numberEmails.visibility = View.GONE
        backButton.visibility = View.VISIBLE
        separator.visibility = View.GONE
        title.text = selectedThreadsQuantity.toString()
    }

    fun hideMultiModeBar() {
        separator.visibility = View.VISIBLE
        navButton.visibility = View.VISIBLE
        numberEmails.visibility = View.VISIBLE
        backButton.visibility = View.GONE
    }

    fun updateToolbarTitle(title: String) {
        this.title.text = title.toUpperCase()
    }
    fun updateNumberOfMails(emailsSize: Int) {
        numberEmails.text = if(emailsSize == 0) "" else "($emailsSize)"
    }
    fun showPlusBadge(show: Boolean){
        if(show){
            plusBadge.visibility = View.VISIBLE
            navButtonRing.visibility = View.VISIBLE
        } else {
            plusBadge.visibility = View.INVISIBLE
            navButtonRing.visibility = View.INVISIBLE
        }
    }
}