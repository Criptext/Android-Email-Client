package com.criptext.mail.scenes.mailbox.holders

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.MailItemHolder
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.Utility
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import uk.co.chrisjenx.calligraphy.TypefaceUtils

/**
 * Created by sebas on 1/24/18.
 */

class EmailHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener , MailItemHolder {

    private val headerView : TextView
    private val layout : LinearLayout
    private val subjectView : TextView
    private val previewView : TextView
    private val dateView : TextView
    private val countView : TextView
    private val context : Context
    private val attachment : ImageView
    private val avatarView: CircleImageView
    private val iconBack: ImageView
    private val check: ImageView
    private val starIcon: ImageView

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
    }

    fun bindEmailPreview(emailPreview: EmailPreview) {
        subjectView.text = if (emailPreview.subject.isEmpty())
            subjectView.context.getString(R.string.nosubject)
        else emailPreview.subject

        previewView.text = if(emailPreview.bodyPreview.isEmpty())
            subjectView.context.getString(R.string.nocontent)
        else
            emailPreview.bodyPreview

        if(emailPreview.deliveryStatus == DeliveryTypes.UNSEND && emailPreview.latestEmailUnsentDate != null) {
            val previewText = subjectView.context.getString(R.string.unsent_at).plus(
                    DateAndTimeUtils.getFormattedDate(emailPreview.latestEmailUnsentDate.time, context))
            previewView.text = previewText
            previewView.setTextColor(ContextCompat.getColor(view.context, R.color.unsent_content))
        }else {
            previewView.setTextColor(ContextCompat.getColor(view.context, R.color.mail_preview))
            previewView.text = emailPreview.bodyPreview
        }

        headerView.text = emailPreview.topText

        avatarView.setImageBitmap(
                Utility.getBitmapFromText(
                        emailPreview.senderName,
                        250,
                        250))

        dateView.text = DateAndTimeUtils.getFormattedDate(emailPreview.timestamp.time, context)

        if(emailPreview.unread) {
            dateView.typeface = TypefaceUtils.load(
                    view.resources.assets,
                    "fonts/NunitoSans-Bold.ttf")
            headerView.typeface = TypefaceUtils.load(
                    view.resources.assets,
                    "fonts/NunitoSans-Bold.ttf")
        } else {
            headerView.typeface = TypefaceUtils.load(
                    view.resources.assets,
                    "fonts/NunitoSans-Regular.ttf")
            dateView.typeface = TypefaceUtils.load(
                    view.resources.assets,
                    "fonts/NunitoSans-Regular.ttf")
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.mailbox_mail_unread))
        }


        if(emailPreview.count > 1){
            countView.visibility = View.VISIBLE
            countView.text = emailPreview.count.toString()
        }
        else{
            countView.visibility = View.GONE
        }

        setIcons(emailPreview.deliveryStatus, emailPreview.isStarred, emailPreview.hasFiles)
        toggleStatus(emailPreview.isSelected, emailPreview.unread)

    }

    private fun setIcons(deliveryType: DeliveryTypes, isStarred: Boolean, hasFiles: Boolean){

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

        starIcon.visibility = if(isStarred) View.VISIBLE else View.GONE
        attachment.visibility = if(hasFiles) View.VISIBLE else View.GONE
    }

    private fun setIconAndColor(drawable: Int, color: Int){
        Picasso.with(view.context).load(drawable).into(check, object : Callback{
            override fun onError() {}
            override fun onSuccess() {
                DrawableCompat.setTint(check.drawable,
                        ContextCompat.getColor(view.context, color))
            }
        })
    }

    fun setOnIconBackClickedListener(clickOnIcon: () -> Unit){
        iconBack.setOnClickListener{
            clickOnIcon()
        }
    }

    fun setOnAvatarClickedListener(clickInAvatar: () -> Unit){
        avatarView.setOnClickListener{
            clickInAvatar()
        }
    }
    override fun fillIcons() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    override fun drawInMultiSelectMode() {
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
    }

    override fun drawInNormalMode() {
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
    }

    private fun toggleStatus(selected: Boolean, unread: Boolean) {
        if(selected) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.mail_item_selected))
            avatarView.visibility = View.GONE
            iconBack.visibility = View.VISIBLE
        }else {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.mail_item_not_selected))
            avatarView.visibility = View.VISIBLE
            iconBack.visibility = View.GONE
            if(unread) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.mail_item_not_selected))
            } else {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.mailbox_mail_unread))
            }
        }
    }

    init {
        check = view.findViewById(R.id.check)
        headerView = view.findViewById(R.id.email_header)
        avatarView = view.findViewById(R.id.mail_item_left_name)
        subjectView = view.findViewById(R.id.email_subject)
        previewView = view.findViewById(R.id.email_preview)
        dateView = view.findViewById(R.id.email_date)
        countView = view.findViewById(R.id.email_count)
        iconBack = view.findViewById(R.id.icon_back)
        layout = view.findViewById(R.id.mail_item_layout)
        attachment = view.findViewById(R.id.email_has_attachments)
        starIcon = view.findViewById(R.id.starred)
        context = view.context
    }

}
