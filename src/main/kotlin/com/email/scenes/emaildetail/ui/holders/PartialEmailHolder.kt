package com.email.scenes.emaildetail.ui.holders

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.ImageView
import com.email.R
import com.email.db.models.FullEmail
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.utils.DateUtils
import com.email.utils.Utility
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by sebas on 3/14/18.
 */

open class PartialEmailHolder(view: View) : ParentEmailHolder(view) {

    private val isSeen: ImageView
    private val hasAttachmentsView: ImageView
    private val leftImageView: CircleImageView

    override fun setListeners(fullEmail: FullEmail, emailListener: FullEmailListAdapter.OnFullEmailEventListener?, adapter: FullEmailListAdapter, position: Int) {
        view.setOnClickListener {
            emailListener?.ontoggleViewOpen(
                    fullEmail = fullEmail,
                    position = position,
                    viewOpen = true)
        }
    }

    override fun bindFullMail(fullEmail: FullEmail) {
        bodyView.text = fullEmail.email.preview
        dateView.text = DateUtils.getFormattedDate(fullEmail.email.date.time)

        if(fullEmail.files.isEmpty()) {
            DrawableCompat.setTint(
                    hasAttachmentsView.drawable,
                    ContextCompat.getColor(view.context, R.color.attachmentGray))

        } else {
            DrawableCompat.setTint(
                    hasAttachmentsView.drawable,
                    ContextCompat.getColor(view.context, R.color.azure))
        }
        if(fullEmail.from == null) {
            headerView.text = "Me"
        } else {
            headerView.text = fullEmail.from.name
        }
        if(fullEmail.from != null) {
            leftImageView.setImageBitmap(Utility.getBitmapFromText(
                    fullEmail.from.name,
                    fullEmail.from.name[0].toString().toUpperCase(), 250, 250))
        } else {
            leftImageView.setImageBitmap(Utility.getBitmapFromText(
                    "Sebastian Caceres",
                    "S", 250, 250))
        }
    }

    init {
        isSeen = view.findViewById(R.id.check)
        hasAttachmentsView = view.findViewById(R.id.email_has_attachments)
        leftImageView = view.findViewById(R.id.mail_item_left_name)
    }

}
