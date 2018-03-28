package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.email.R
import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.EmailContactInfoListener
import com.email.utils.DateUtils
import com.email.utils.VirtualList
import com.email.utils.ui.PopupUtils

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
        val layout = inflater.inflate( R.layout.email_contact_info_popup, null)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.contacts_to_recycler)

        val contactsTo = VirtualList.Map(fullEmail.to, {t -> t})
        ContactsToRecyclerView(recyclerView, contactsTo)

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
        val viewReplyName = view.findViewById<TextView>(R.id.reply_name)
        val viewReplyEmail = view.findViewById<TextView>(R.id.reply_mail)
        val recycler = view.findViewById<RecyclerView>(R.id.contacts_to_recycler)
        val date = view.findViewById<TextView>(R.id.date)

        viewFromName.text = fullEmail.from?.name
        viewFromEmail.text = fullEmail.from?.email

        if(fullEmail.to.isEmpty()) {
            recycler.visibility = View.GONE
        }

        date.text = DateUtils.getFormattedDate(fullEmail.email.date.time)
    }

    class ContactsToRecyclerView(val recyclerView: RecyclerView,
                                       contactsToList: VirtualList<Contact>
    ) {

        val ctx: Context = recyclerView.context
        private val contactsListAdapter = ContactsToListAdapter(
                mContext = ctx,
                contacts = contactsToList)

        init {
            recyclerView.layoutManager = LinearLayoutManager(ctx)
            recyclerView.adapter = contactsListAdapter
        }
    }

    class ContactsToListAdapter(private val mContext: Context,
                                      private val contacts: VirtualList<Contact>
    ) : RecyclerView.Adapter<ContactHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ContactHolder{
            val mView = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null)
            return ContactHolder(mView)
        }

        override fun getItemCount(): Int {
            return contacts.size
        }

        override fun onBindViewHolder(holder: ContactHolder?, position: Int) {
            val contact = contacts[position]
            holder?.bindContact(contact)
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
