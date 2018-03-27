package com.email.scenes.composer.ui

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.email.db.models.Contact
import com.email.R
import com.tokenautocomplete.TokenCompleteTextView

/**
 * Created by gabriel on 2/28/18.
 */
class ContactCompletionView : TokenCompleteTextView<Contact> {

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun getViewForObject(contact: Contact): View {
        val l = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = l.inflate(R.layout.contact_token, parent as ViewGroup, false) as TextView
        view.text = contact.email

        if (contact is Contact.Invalid) {
            view.setTextColor(ContextCompat.getColor(context, R.color.unsend_button_red))
        }

        return view
    }

    override fun defaultObject(completionText: String): Contact {
        val isValidEmailAddress = android.util.Patterns.EMAIL_ADDRESS
                                  .matcher(completionText).matches()
        if (isValidEmailAddress) {
            val index = completionText.indexOf('@')
            val name = completionText.substring(0, index)
            return Contact(name = name, email = completionText)
        } else {
            return Contact.Invalid(name = completionText, email = completionText)
        }
    }

    companion object {
        fun isPlusToken(token: String): Boolean {
            return token.length > 1 && token[0] == '+'
                    && token.substring(1).all { char -> (char in '0'..'9') }
        }
    }
}