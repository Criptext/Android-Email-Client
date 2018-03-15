package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import com.email.R
import com.email.db.models.Contact
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.EmailContactInfoListener
import com.email.utils.DateUtils
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/13/18.
 */

class EmailContactInfoPopup(private val parentView: View) {
    private val context = parentView.context

    fun createPopup(
            fullEmail: FullEmail,
            emailContactInfoListener: EmailContactInfoListener?
    ): PopupWindow {
        val height = context.resources.getDimension(R.dimen.popup_window_contactinfo_height).toInt()
        val width = context.resources.getDimension(R.dimen.popup_window_contactinfo_width).toInt()

        val inflater = context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE ) as LayoutInflater
        val layout = inflater.inflate( R.layout.email_contact_info_popup, null)
        val recyclerView = layout.findViewById<RecyclerView>(R.id.contacts_to_recycler)
        val popupWindow = PopupWindow(
                layout,
                width,
                height,
                true)
        bindFullEmail(fullEmail = fullEmail,
                view = layout)

        val contactsTo = VirtualList.Map(fullEmail.to, {t -> t})
        ContactsToRecyclerView(recyclerView, contactsTo)
        popupWindow.showAsDropDown(parentView)

        val container = popupWindow.contentView.parent as View
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.3f
        wm.updateViewLayout(container, p)
        return popupWindow
    }

    private fun bindFullEmail(
            fullEmail: FullEmail,
            view: View) {
        val viewFromName = view.findViewById<TextView>(R.id.from_name)
        val viewFromEmail = view.findViewById<TextView>(R.id.from_mail)
        val viewReplyName = view.findViewById<TextView>(R.id.reply_name)
        val viewReplyEmail = view.findViewById<TextView>(R.id.reply_mail)
        val date = view.findViewById<TextView>(R.id.date)

        date.text = DateUtils.getFormattedDate(fullEmail.email.date.time)
    }

    inner class ContactsToRecyclerView(val recyclerView: RecyclerView,
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
