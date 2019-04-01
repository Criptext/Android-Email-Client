package com.criptext.mail.scenes.mailbox.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.db.models.Account
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.Utility
import de.hdodenhof.circleimageview.CircleImageView


class AccountHolder(val view: View) : RecyclerView.ViewHolder(view){

    private val nameView : TextView = view.findViewById(R.id.textViewNombre) as TextView
    private val mailView : TextView = view.findViewById(R.id.textViewCorreo) as TextView
    private val badgeNumber : TextView = view.findViewById(R.id.badgeNumber) as TextView
    private val avatarView : CircleImageView = view.findViewById(R.id.accountItemAvatar)
    private val rootView: View = view.findViewById(R.id.rootView)

    fun bindAccount(account: Account, badgeCount: Int) {
        nameView.text = account.name
        val email = account.recipientId.plus("@").plus(account.domain)
        mailView.text = email
        if(EmailAddressUtils.isFromCriptextDomain(email))
            UIUtils.setProfilePicture(
                    iv = avatarView,
                    resources = avatarView.context.resources,
                    recipientId = EmailAddressUtils.extractRecipientIdFromCriptextAddress(email),
                    name = account.name,
                    runnable = null)
        else
            avatarView.setImageBitmap(
                    Utility.getBitmapFromText(
                            account.name,
                            250,
                            250))
        if(badgeCount > 0) {
            badgeNumber.visibility = View.VISIBLE
            badgeNumber.text = badgeCount.toString()
        }else{
            badgeNumber.visibility = View.GONE
            badgeNumber.text = ""
        }
    }

    fun setOnClickedListener(onClick: () -> Unit) {
        rootView.setOnClickListener {
            onClick()
        }
    }
}
