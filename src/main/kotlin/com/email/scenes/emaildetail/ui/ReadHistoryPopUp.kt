package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import com.email.R
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.ReadHistoryListener
import com.email.utils.DateUtils
import java.text.SimpleDateFormat
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
    ): PopupWindow {
        val height = context.resources.getDimension(R.dimen.popup_window_contactinfo_height).toInt()
        val width = context.resources.getDimension(R.dimen.popup_window_contactinfo_width).toInt()

        val inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE ) as LayoutInflater
        val layout = inflater.inflate( R.layout.layout_read_history_contacts, null)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.contacts_read_history_recycler)
        val popupWindow = PopupWindow()

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_drawable))

        popupWindow.height = height
        popupWindow.width = width
        popupWindow.contentView = layout


        val mockedContacts = getMockedContacts()

        ContactsReadRecyclerView(recyclerView, mockedContacts)

        popupWindow.showAsDropDown(anchorView)

        val container = if (android.os.Build.VERSION.SDK_INT > 22) {
            popupWindow.contentView.parent.parent as View
        } else {
            popupWindow.contentView.parent as View
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.3f
        wm.updateViewLayout(container, p)
        return popupWindow
    }

    private fun getMockedContacts(): List<MockedContact> {
        val sdf = SimpleDateFormat("yyyy-MM-dd")

        val array = ArrayList<MockedContact>()
        array.add(MockedContact("Sebastian Caceres", sdf.parse("2018-02-11")))
        array.add(MockedContact("Gianni Carlo", sdf.parse("2018-20-01")))
        array.add(MockedContact("Gabriel Aumala", sdf.parse("2017-20-12")))
        array.add(MockedContact("someemail@email.com", sdf.parse("2017-21-12")))
        array.add(MockedContact("Erika Perugachi", sdf.parse("2018-13-03")))
        array.add(MockedContact("Erika Perugachi", sdf.parse("2018-16-03")))
        return array
    }


    inner class ContactsReadRecyclerView(val recyclerView: RecyclerView,
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

    inner class ContactsToListAdapter(private val mContext: Context,
                                      private val contacts: List<MockedContact>
    ) : RecyclerView.Adapter<ContactHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ContactHolder{
            val mView = LayoutInflater.from(mContext).
                    inflate(R.layout.contact_read_history_item, null)
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
        private val date: TextView

        fun bindContact(contact: MockedContact){
            name.text = contact.name
            date.text = DateUtils.getFormattedDate(contact.date.time)
        }

        init {
            name = view.findViewById(R.id.name)
            date = view.findViewById(R.id.date)
        }
    }

    data class MockedContact(val name: String, val date: Date)
}
