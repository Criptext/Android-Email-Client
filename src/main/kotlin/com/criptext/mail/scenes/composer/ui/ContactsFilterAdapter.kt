package com.criptext.mail.scenes.composer.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.criptext.mail.db.models.Contact
import com.criptext.mail.R
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.Utility
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.tokenautocomplete.FilteredArrayAdapter
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * Created by gabriel on 2/28/18.
 */

class ContactsFilterAdapter(context : Context, private val objects : List<Contact>)
    : FilteredArrayAdapter<Contact>(context, R.layout.autocomplete_item, 0, objects) {

    override fun keepObject(obj: Contact?, mask: String?): Boolean {
        if(mask?.length ?: 0 < 2 || obj == null ){
            return false
        }
        val newMask = mask!!.toLowerCase()
        return if(obj.email.isNotEmpty() && AccountDataValidator.validateEmailAddress(obj.email) is FormData.Valid){
            val domain = EmailAddressUtils.extractEmailAddressDomain(obj.email)
            val recipientId = EmailAddressUtils.extractRecipientIdFromAddress(obj.email, domain)
            (obj.name.isNotEmpty() && obj.name.toLowerCase().contains(newMask)) || (obj.email.isNotEmpty() &&  recipientId.contains(newMask))
        } else {
            (obj.name.isNotEmpty() && obj.name.toLowerCase().contains(newMask)) || (obj.email.isNotEmpty() &&  obj.email.contains(newMask))
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(LayoutInflater.from(context), position, convertView, parent, R.layout.autocomplete_item);
    }

    fun updateIsCriptextDomain(list: List<Contact>){
        list.forEach {
            val contactIndex = objects.indexOf(it)
            if(contactIndex != -1){
                objects[contactIndex].isCriptextDomain = it.isCriptextDomain
            }
        }
    }

    private fun createViewFromResource(inflater : LayoutInflater, position : Int, convertView: View?, parent: ViewGroup?, resource: Int) : View{
        val view: View = convertView ?: inflater.inflate(resource, parent, false)

        val nameTextView = view.findViewById<TextView>(R.id.auto_name)
        val mailTextView = view.findViewById(R.id.auto_mail) as TextView
        val circleView = view.findViewById(R.id.auto_circle) as CircleImageView

        val item = getItem(position)

        if(item != null) {
            nameTextView.text = item.name
            mailTextView.text = item.email
            if (item.email.contains(item.name)) {
                nameTextView.visibility = View.GONE
            } else {
                nameTextView.visibility = View.VISIBLE
            }

            val domain = EmailAddressUtils.extractEmailAddressDomain(item.email)

            UIUtils.setProfilePicture(
                    iv = circleView,
                    resources = context.resources,
                    recipientId = EmailAddressUtils.extractRecipientIdFromAddress(item.email, domain),
                    name = item.name,
                    runnable = null,
                    domain = domain)
        }
        return view
    }
}
