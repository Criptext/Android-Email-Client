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
import com.tokenautocomplete.FilteredArrayAdapter
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * Created by gabriel on 2/28/18.
 */
class ContactsFilterAdapter(context : Context, objects : Array<Contact>)
    : FilteredArrayAdapter<Contact>(context, R.layout.autocomplete_item, 0, objects) {

    override fun keepObject(obj: Contact?, mask: String?): Boolean {
        if(mask?.length ?: 0 < 2 || obj == null ){
            return false
        }
        val newMask = mask!!.toLowerCase()
        return obj.name.toLowerCase().contains(newMask) || obj.email.contains(newMask)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(LayoutInflater.from(context), position, convertView, parent, R.layout.autocomplete_item);
    }

    fun createViewFromResource(inflater : LayoutInflater, position : Int, convertView: View?, parent: ViewGroup?, resource: Int) : View{
        val view: View = convertView ?: inflater.inflate(resource, parent, false)

        val nameTextView = view.findViewById<TextView>(R.id.auto_name)
        val mailTextView = view.findViewById<TextView>(R.id.auto_mail) as TextView
        val circleView = view.findViewById<CircleImageView>(R.id.auto_circle) as CircleImageView

        val item = getItem(position)
        nameTextView.text = item.name
        mailTextView.text = item.email
        if(item.email.contains(item.name)){
            nameTextView.visibility = View.GONE
        }
        else{
            nameTextView.visibility = View.VISIBLE
        }

        if(EmailAddressUtils.isFromCriptextDomain(item.email))
            UIUtils.setProfilePicture(
                    iv = circleView,
                    resources = context.resources,
                    recipientId = EmailAddressUtils.extractRecipientIdFromCriptextAddress(item.email),
                    name = item.name,
                    runnable = null)
        else
            circleView.setImageBitmap(
                    Utility.getBitmapFromText(
                            item.name,
                            250,
                            250))

        return view
    }

}
