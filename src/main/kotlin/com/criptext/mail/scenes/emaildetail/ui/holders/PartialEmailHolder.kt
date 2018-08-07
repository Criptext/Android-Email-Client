package com.criptext.mail.scenes.emaildetail.ui.holders

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.criptext.mail.R
import com.criptext.mail.SecureEmail
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.criptext.mail.utils.DateUtils
import com.criptext.mail.utils.EmailThreadValidator
import com.criptext.mail.utils.Utility
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by sebas on 3/14/18.
 */

open class PartialEmailHolder(view: View) : ParentEmailHolder(view) {

    private val rootView: LinearLayout
    private val check: ImageView
    private val attachment: ImageView
    private val leftImageView: CircleImageView

    override fun setListeners(fullEmail: FullEmail, fileDetails: List<FileDetail>,
                              emailListener: FullEmailListAdapter.OnFullEmailEventListener?, adapter: FullEmailListAdapter, position: Int) {
        view.setOnClickListener {
            emailListener?.ontoggleViewOpen(
                    fullEmail = fullEmail,
                    position = position,
                    viewOpen = true)
        }
    }

    override fun bindFullMail(fullEmail: FullEmail) {

        if(fullEmail.email.delivered == DeliveryTypes.UNSEND) {
            bodyView.alpha = 0.5.toFloat()
            bodyView.text = bodyView.resources.getString(R.string.unsent)
            bodyView.setTextColor(ContextCompat.getColor(
                    view.context, R.color.unsent_content))
            rootView.background = ContextCompat.getDrawable(
                    view.context, R.drawable.background_cardview_unsend_partial)
        }
        else {
            bodyView.alpha = 1.toFloat()
            rootView.background = ContextCompat.getDrawable(
                    view.context, R.drawable.partial_email_drawable)
            bodyView.text = fullEmail.email.preview
        }

        dateView.text = DateUtils.getFormattedDate(fullEmail.email.date.time)

        headerView.text =
                if(EmailThreadValidator.isLabelInList(fullEmail.labels, SecureEmail.LABEL_DRAFT)) {
                    headerView.setTextColor(ContextCompat.getColor(headerView.context, R.color.colorUnsent))
                    headerView.context.getString(R.string.draft)
                }
                else {
                    headerView.setTextColor(ContextCompat.getColor(headerView.context, R.color.textColorPrimary))
                    fullEmail.from.name
                }

        leftImageView.setImageBitmap(Utility.getBitmapFromText(
                fullEmail.from.name,
                fullEmail.from.name[0].toString().toUpperCase(), 250, 250))

        setIcons(fullEmail.email.delivered)
    }

    private fun setIcons(deliveryType: DeliveryTypes){

        check.visibility = View.VISIBLE

        when(deliveryType){
            DeliveryTypes.UNSEND -> {
                check.visibility = View.GONE
            }
            DeliveryTypes.SENDING -> {
                setIconAndColor(R.drawable.clock, R.color.sent)
            }
            DeliveryTypes.READ -> {
                setIconAndColor(R.drawable.read, R.color.azure)
            }
            DeliveryTypes.DELIVERED -> {
                setIconAndColor(R.drawable.read, R.color.sent)
            }
            DeliveryTypes.SENT -> {
                setIconAndColor(R.drawable.mail_sent, R.color.sent)
            }
            DeliveryTypes.NONE -> {
                check.visibility = View.GONE
            }
        }

    }

    private fun setIconAndColor(drawable: Int, color: Int){
        Picasso.with(view.context).load(drawable).into(check, object : Callback {
            override fun onError() {}
            override fun onSuccess() {
                DrawableCompat.setTint(check.drawable,
                        ContextCompat.getColor(view.context, color))
            }
        })
    }

    init {
        check = view.findViewById(R.id.check)
        attachment = view.findViewById(R.id.email_has_attachments)
        leftImageView = view.findViewById(R.id.mail_item_left_name)
        rootView = view.findViewById(R.id.cardview)
    }

}
