package com.email.scenes.mailbox.holders

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.db.DeliveryTypes
import com.email.scenes.MailItemHolder
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.DateUtils
import com.email.utils.Utility
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

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
    }

    fun bindEmailThread(emailThread: EmailThread) {
        subjectView.text = if (emailThread.subject.isEmpty())
            subjectView.context.getString(R.string.nosubject)
        else emailThread.subject

        previewView.text = if(emailThread.preview.isEmpty())
            subjectView.context.getString(R.string.nocontent)
        else
            emailThread.preview

        headerView.text = emailThread.headerPreview
        avatarView.setImageBitmap(
                Utility.getBitmapFromText(
                        emailThread.latestEmail.from.name ?:"Empty",
                        emailThread.latestEmail.from.name[0].toString().toUpperCase() ?: "E",
                        250,
                        250))

        dateView.text = DateUtils.getFormattedDate(emailThread.timestamp.time)

        if(emailThread.unread) {
            DrawableCompat.setTint(
                    check.drawable,
                    ContextCompat.getColor(view.context, R.color.attachmentGray))
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

        //TODO validate number of emails in thread
        countView.visibility = View.GONE

        setIcons(emailThread.status)
        toggleStatus(emailThread.isSelected, emailThread.unread)
    }

    private fun setIcons(deliveryType: DeliveryTypes){

        check.visibility = View.VISIBLE

        when(deliveryType){
            DeliveryTypes.SENT -> {
                setIconAndColor(R.drawable.mail_sent, R.color.sent)
            }
            DeliveryTypes.DELIVERED -> {
                setIconAndColor(R.drawable.read, R.color.sent)
            }
            DeliveryTypes.OPENED -> {
                setIconAndColor(R.drawable.read, R.color.azure)
            }
            DeliveryTypes.UNSENT -> {
                setIconAndColor(R.drawable.un_sent, R.color.unsendBtn)
            }
            DeliveryTypes.NONE -> {
                check.visibility = View.GONE
            }
        }

        //TODO validate if has attachments
        attachment.visibility = View.GONE
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        context = view.context
    }

}
