package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.email.R
import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.EmailContactInfoListener
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/13/18.
 */

class EmailContactInfoPopup(private val context: Context) {
    private var popupWindow : PopupWindow? = null
    private val res = context.resources
    private lateinit var layout: View
    private val recyclerView: RecyclerView
        get() = layout.findViewById(R.id.contacts_to_recycler)

    private fun createPopup(
            fullEmail: FullEmail,
            emailContactInfoListener: EmailContactInfoListener,
            positionY: Int
    ): PopupWindow {
        val height = context.resources.getDimension(R.dimen.alert_dialog_label_chooser_height).toInt()
        val width = context.resources.getDimension(R.dimen.alert_dialog_label_chooser_width).toInt()

        val inflater = (context as AppCompatActivity).layoutInflater
        layout = inflater.inflate( R.layout.email_contact_info_popup, null)

        val popupWindow = PopupWindow(
                layout,
                width,
                height,
                true)
        bindFullEmail(fullEmail = fullEmail,
                view = layout)

        val contactsTo = VirtualList.Map(fullEmail.to, {t -> t})
        ContactsToRecyclerView(recyclerView, contactsTo)
        popupWindow.showAtLocation(layout, Gravity.CENTER, 0, positionY)
        return popupWindow
    }

    private fun bindFullEmail(
            fullEmail: FullEmail,
            view: View) {

    }

    inner class ContactsToRecyclerView(val recyclerView: RecyclerView,
                                        contactsToList: VirtualList<Contact>
                                       ) {

        val ctx: Context = recyclerView.context
        private val fullEmailListAdapter = ContactsToListAdapter(
                mContext = ctx,
                contacts = contactsToList)

        init {
            recyclerView.layoutManager = LinearLayoutManager(ctx)
            recyclerView.adapter = fullEmailListAdapter
        }
    }

    inner class ContactsToListAdapter(private val mContext: Context,
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

    inner class ContactHolder(val view: View): RecyclerView.ViewHolder(view) {
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
