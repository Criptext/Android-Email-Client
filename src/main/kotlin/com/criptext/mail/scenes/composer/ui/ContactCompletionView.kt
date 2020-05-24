package com.criptext.mail.scenes.composer.ui

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.db.models.Contact
import com.criptext.mail.R
import com.tokenautocomplete.TokenCompleteTextView
import com.criptext.mail.utils.EmailAddressUtils.CRIPTEXT_DOMAIN_SUFFIX

/**
 * Created by gabriel on 2/28/18.
 */
class ContactCompletionView : TokenCompleteTextView<Contact> {

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun getViewForObject(contact: Contact): View {

        val l = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: LinearLayout
        view = when {
            contact is Contact.Invalid -> l.inflate(R.layout.contact_token_invalid, parent as ViewGroup, false) as LinearLayout
            contact.isCriptextDomain -> l.inflate(R.layout.contact_token, parent as ViewGroup, false) as LinearLayout
            else -> l.inflate(R.layout.contact_token_external, parent as ViewGroup, false) as LinearLayout
        }

        view.findViewById<TextView>(R.id.name).text = contact.email

        return view
    }

    override fun defaultObject(completionText: String): Contact {
        val index = completionText.indexOf('@')
        val hasAtSymbol = index != -1
        val name = if (hasAtSymbol) completionText.substring(0, index) else completionText
        val emailAddress = if (hasAtSymbol) completionText else completionText + CRIPTEXT_DOMAIN_SUFFIX

        val isValidEmailAddress = android.util.Patterns.EMAIL_ADDRESS
                                  .matcher(emailAddress).matches()
        return if (isValidEmailAddress) {
            Contact(id = 0, name = name, email = emailAddress.toLowerCase(), isTrusted = false, score = 0, spamScore = 0)
        } else {
            Contact.Invalid(name = name, email = emailAddress.toLowerCase())
        }
    }

    companion object {
        fun isPlusToken(token: String): Boolean {
            return token.length > 1 && token[0] == '+'
                    && token.substring(1).all { char -> (char in '0'..'9') }
        }
    }
}