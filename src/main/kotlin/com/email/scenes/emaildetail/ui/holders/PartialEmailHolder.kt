package com.email.scenes.emaildetail.ui.holders

import android.view.View
import android.widget.ImageView
import com.email.R
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by sebas on 3/14/18.
 */

open class PartialEmailHolder(view: View) : ParentEmailHolder(view) {

    private val isSeen: ImageView
    private val hasAttachments: ImageView
    private val leftImageView: CircleImageView

    override fun setListeners(fullEmail: FullEmail, emailListener: FullEmailListAdapter.OnFullEmailEventListener?, adapter: FullEmailListAdapter, position: Int) {
        view.setOnClickListener {
            emailListener?.ontoggleViewOpen(
                    fullEmail = fullEmail,
                    position = position,
                    viewOpen = true)
        }
    }

    init {
        isSeen = view.findViewById(R.id.check)
        hasAttachments = view.findViewById(R.id.email_has_attachments)
        leftImageView = view.findViewById(R.id.mail_item_left_name)
    }

}
