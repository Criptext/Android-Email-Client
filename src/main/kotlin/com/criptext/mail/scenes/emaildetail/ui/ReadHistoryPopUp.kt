package com.criptext.mail.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.scenes.emaildetail.ReadHistoryListener
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ui.PopupUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by sebas on 3/15/18.
 */

class ReadHistoryPopUp(private val anchorView: View) {
    private val context = anchorView.context

    fun createPopup(
            fullEmail: FullEmail,
            readHistoryListener: ReadHistoryListener?
    ) {

        val inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE ) as LayoutInflater
        val layout = inflater.inflate( R.layout.layout_read_history_contacts, null)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.contacts_read_history_recycler)

        val mockedContacts = getMockedContacts()

        ContactsReadRecyclerView(recyclerView, mockedContacts)

        PopupUtils.createPopUpWindow(
                context = context,
                contentView = layout,
                anchorView = anchorView)

        bindFullEmail(fullEmail, layout)
    }

    private fun bindFullEmail(fullEmail: FullEmail, view: View) {

    }

    private fun getMockedContacts(): List<MockedContact> {

        val array = ArrayList<MockedContact>()
        array.add(MockedContact("Sebastian Caceres", DateAndTimeUtils.getDateFromString(
                "1992-05-23 20:12:58", null)))
        array.add(MockedContact("Gianni Carlo", DateAndTimeUtils.getDateFromString(
                "2016-12-23 20:12:58", null)))
        array.add(MockedContact("Gabriel Aumala",
                DateAndTimeUtils.getDateFromString("2017-05-23 20:12:58", null)))
        array.add(MockedContact("someemail@email.com",
                DateAndTimeUtils.getDateFromString("2012-05-23 20:12:58", null)))
        array.add(MockedContact("Erika Perugachi",
                DateAndTimeUtils.getDateFromString("2010-05-23 20:12:58", null)))
        array.add(MockedContact("Erika Perugachi",
                DateAndTimeUtils.getDateFromString("2000-05-23 20:12:58", null)))
        return array
    }


    class ContactsReadRecyclerView(val recyclerView: RecyclerView,
                                       contactsToList: List<MockedContact>
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
                                      private val contacts: List<MockedContact>
    ) : RecyclerView.Adapter<ContactHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder{
            val mView = LayoutInflater.from(mContext).
                    inflate(R.layout.contact_read_history_item, null)
            return ContactHolder(mView)
        }

        override fun getItemCount(): Int {
            return contacts.size
        }

        override fun onBindViewHolder(holder: ContactHolder, position: Int) {
            val contact = contacts[position]
            holder?.bindContact(contact)
        }
    }

    class ContactHolder(val view: View): RecyclerView.ViewHolder(view) {
        private val context = view.context

        private val name: TextView
        private val date: TextView

        fun bindContact(contact: MockedContact){
            name.text = contact.name
            date.text = DateAndTimeUtils.getFormattedDate(contact.date.time, context)
        }

        init {
            name = view.findViewById(R.id.name)
            date = view.findViewById(R.id.date)
        }
    }

    data class MockedContact(val name: String, val date: Date)
}
