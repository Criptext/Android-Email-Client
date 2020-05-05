package com.criptext.mail.scenes.emaildetail.ui.holders

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.FileDetail
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.criptext.mail.utils.*
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception


/**
 * Created by sebas on 3/14/18.
 */

open class PartialEmailHolder(view: View) : ParentEmailHolder(view) {

    private val rootView: LinearLayout
    private val check: ImageView
    private val attachment: ImageView
    private val isSecure : ImageView
    private val leftImageView: CircleImageView

    override fun setBackground(drawable: Drawable) {
        rootView.background = drawable
    }

    override fun setBottomMargin(marginBottom: Int) {
        val params = view.layoutParams as MarginLayoutParams
        params.bottomMargin = marginBottom
        rootView.layoutParams = params
    }

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

        dateView.text = DateAndTimeUtils.getFormattedDate(fullEmail.email.date.time, view.context)

        headerView.text =
                if(EmailThreadValidator.isLabelInList(fullEmail.labels, Label.LABEL_DRAFT)) {
                    headerView.setTextColor(ContextCompat.getColor(headerView.context, R.color.colorUnsent))
                    headerView.context.getString(R.string.draft)
                }
                else {
                    headerView.setTextColor(view.context.getColorFromAttr(R.attr.criptextPrimaryTextColor))
                    fullEmail.from.name
                }
        val contactFrom = fullEmail.from
        val domain = EmailAddressUtils.extractEmailAddressDomain(contactFrom.email)

        UIUtils.setProfilePicture(
                iv = leftImageView,
                resources = view.context.resources,
                recipientId = EmailAddressUtils.extractRecipientIdFromAddress(contactFrom.email, domain),
                name = contactFrom.name,
                runnable = null,
                domain = domain)


        setIcons(fullEmail.email.delivered, fullEmail.files.isNotEmpty(), fullEmail.email.secure)
    }

    private fun setIcons(deliveryType: DeliveryTypes, hasFiles: Boolean, secure: Boolean){

        check.visibility = View.VISIBLE

        when(deliveryType){
            DeliveryTypes.UNSEND -> {
                check.visibility = View.GONE
            }
            DeliveryTypes.FAIL -> {
                setIconAndColor(R.drawable.x_rounded, R.color.unsent_content)
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
            else -> {
                check.visibility = View.GONE
            }
        }
        attachment.visibility = if(hasFiles) View.VISIBLE else View.GONE
        isSecure.visibility = if(secure) View.VISIBLE else View.GONE
    }

    private fun setIconAndColor(drawable: Int, color: Int){
        Picasso.get().load(drawable).into(check, object : Callback {
            override fun onError(e: Exception?) {

            }
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
        isSecure = view.findViewById(R.id.email_is_secure)
    }

}
