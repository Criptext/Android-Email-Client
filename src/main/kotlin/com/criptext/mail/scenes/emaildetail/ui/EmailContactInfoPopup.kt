package com.criptext.mail.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.scenes.emaildetail.EmailContactInfoListener
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.ui.PopupUtils

/**
 * Created by sebas on 3/13/18.
 */

class EmailContactInfoPopup(private val anchorView: View) {
    private val context = anchorView.context

    fun createPopup(
            fullEmail: FullEmail,
            emailContactInfoListener: EmailContactInfoListener?
    ) {

        val inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE ) as LayoutInflater
        val layout = inflater.inflate( R.layout.email_contact_info_popup, anchorView.parent as ViewGroup, false)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.contacts_to_recycler)
        val contactsTo = VirtualList.Map(fullEmail.to, { t -> t})
        ContactsRecyclerView(recyclerView, contactsTo)

        val recyclerViewCC = layout.findViewById<RecyclerView>(R.id.contacts_cc_recycler)
        val contactsCC = VirtualList.Map(fullEmail.cc, { t -> t})
        ContactsRecyclerView(recyclerViewCC, contactsCC)

        val recyclerViewBCC = layout.findViewById<RecyclerView>(R.id.contacts_bcc_recycler)
        val contactsBCC = VirtualList.Map(fullEmail.bcc, { t -> t})
        ContactsRecyclerView(recyclerViewBCC, contactsBCC)

        PopupUtils.createPopUpWindow(
                context = context,
                anchorView = anchorView,
                contentView = layout)

        bindFullEmail(fullEmail = fullEmail,
                view = layout)


    }

    private fun bindFullEmail(
            fullEmail: FullEmail,
            view: View) {

        val viewFromName = view.findViewById<TextView>(R.id.from_name)
        val viewFromEmail = view.findViewById<TextView>(R.id.from_mail)
        val containerCC = view.findViewById<LinearLayout>(R.id.cc_container)
        val containerBCC = view.findViewById<LinearLayout>(R.id.bcc_container)
        val date = view.findViewById<TextView>(R.id.date)
        val subject = view.findViewById<TextView>(R.id.subject)

        viewFromName.text = fullEmail.from.name
        viewFromEmail.text = fullEmail.from.email

        if(fullEmail.cc.isEmpty()) {
            containerCC.visibility = View.GONE
        }

        if(fullEmail.bcc.isEmpty()) {
            containerBCC.visibility = View.GONE
        }

        date.text = DateAndTimeUtils.getFormattedDate(fullEmail.email.date.time)
        subject.text = fullEmail.email.subject
    }

    class ContactsRecyclerView(val recyclerView: RecyclerView,
                               contactsList: VirtualList<Contact>
    ) {

        val ctx: Context = recyclerView.context
        private val contactsListAdapter = ContactsListAdapter(
                mContext = ctx,
                contacts = contactsList)

        init {
            recyclerView.layoutManager = LinearLayoutManager(ctx)
            recyclerView.adapter = contactsListAdapter
        }
    }

    class ContactsListAdapter(private val mContext: Context,
                              private val contacts: VirtualList<Contact>
    ) : RecyclerView.Adapter<ContactHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder{
            val mView = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null)
            return ContactHolder(mView)
        }

        override fun getItemCount(): Int {
            return contacts.size
        }

        override fun onBindViewHolder(holder: ContactHolder, position: Int) {
            val contact = contacts[position]
            holder.bindContact(contact)
        }
    }

    class ContactHolder(val view: View): RecyclerView.ViewHolder(view) {
        private val context = view.context

        private val name: TextView
        private val email: TextView

        fun bindContact(contact: Contact){
            name.text = contact.name
            email.text = contact.email
        }

        init {
            name = view.findViewById(R.id.name)
            email = view.findViewById(R.id.email)
        }
    }
}
