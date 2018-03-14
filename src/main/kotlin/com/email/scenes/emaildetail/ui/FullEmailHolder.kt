package com.email.scenes.emaildetail.ui

import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.email.R
import com.email.db.models.FullEmail

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailHolder(val view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

    private val context = view.context
    private val layout : FrameLayout
    private val contactView : TextView
    private val toView: TextView
    private val dateView: TextView
    private val bodyView: TextView
    private val moreView: ImageView
    private val layoutAttachment : RelativeLayout

    private var popupMenu: PopupMenu? = null

    fun setListeners(fullEmail: FullEmail,
                    emailListener: FullEmailListAdapter.OnFullEmailEventListener?,
                    adapter: FullEmailListAdapter, position: Int) {
        moreView.setOnClickListener({
            displayPopMenu(emailListener, fullEmail, adapter, position)
        })

        toView.setOnClickListener({
            emailListener!!.onShowContactsToView(
                    fullEmail = fullEmail)
        })
        layoutAttachment.setOnClickListener{
            TODO("HANDLE CLICK TO ATTACHMENT")
        }

        layoutAttachment.setOnClickListener{
            TODO("HANDLE CLICK TO ATTACHMENT")
        }
    }

    private fun displayPopMenu(emailListener: FullEmailListAdapter.OnFullEmailEventListener?, fullEmail: FullEmail,
                               adapter: FullEmailListAdapter, position: Int){
        val popupMenu = createPopupMenu(fullEmail)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.forward ->
                    emailListener?.onReplyOptionSelected(
                            fullEmail = fullEmail,
                            position = position,
                            all = false)
                R.id.mark_read, R.id.mark_unread -> {
                    emailListener?.onToggleReadOption(
                            fullEmail = fullEmail,
                            position = position,
                            markAsRead = item.itemId == R.id.mark_read)
                }
                R.id.delete ->
                    emailListener?.onDeleteOptionSelected(
                            fullEmail = fullEmail,
                            position = position )
            }
            false
        }

        popupMenu.show()

    }

    fun removePopupMenu() {
        this.popupMenu = null
    }

    override fun onClick(p0: View?) {
    }

    private fun createPopupMenu(fullEmail: FullEmail): PopupMenu {
        val popupMenu = PopupMenu(context , moreView)

    val popuplayout =
            if (fullEmail.email.unread)
                R.menu.mail_options_unread_menu
            else
                R.menu.mail_options_read_menu

        popupMenu.inflate(popuplayout)
        return popupMenu
    }

    init {
        layout = view.findViewById(R.id.open_full_mail_item_container)
        contactView = view.findViewById(R.id.contact)
        toView = view.findViewById(R.id.to)
        dateView = view.findViewById(R.id.date)
        bodyView = view.findViewById(R.id.body)
        moreView = view.findViewById(R.id.more)
        layoutAttachment = view.findViewById(R.id.open_full_mail_attachment_container)
    }

}
