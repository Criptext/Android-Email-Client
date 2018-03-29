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
import com.email.scenes.MailItemHolder
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.DateUtils
import com.email.utils.Utility
import com.email.utils.anim.FlipAnimator
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
    private val iconAttachments: ImageView
    private val check: ImageView

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
    }

    fun bindMail(emailThread: EmailThread) {
        subjectView.setText(emailThread.subject, TextView.BufferType.EDITABLE)
        if(emailThread.unread) {
            DrawableCompat.setTint(
                    check.drawable,
                    ContextCompat.getColor(view.context, R.color.attachmentGray))

        } else {
            DrawableCompat.setTint(
                    check.drawable,
                    ContextCompat.getColor(view.context, R.color.azure))
        }

        previewView.text = emailThread.preview
        headerView.text = emailThread.headerPreview
        avatarView.setImageBitmap(
                Utility.getBitmapFromText(
                        emailThread.latestEmail.from?.name ?:"Empty",
                        emailThread.latestEmail.from?.name?.get(0)?.toString()?.toUpperCase() ?: "E",
                        250,
                        250))

        dateView.text = DateUtils.getFormattedDate(emailThread.timestamp.time)

        if(emailThread.unread) {
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

    fun toggleStatus(selected: Boolean, unread: Boolean) {
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

    fun hideMultiselect() {
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.mail_item_not_selected))
    }

    fun applyIconAnimation(holder: EmailHolder, mail: EmailThread, mContext: Context) {
        if (mail.isSelected) {
            holder.avatarView.visibility = View.GONE
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE)
            holder.iconBack.setAlpha(1.toFloat())
            FlipAnimator.flipView(mContext,
                    holder.iconBack,
                    holder.avatarView,
                    true);
        } else if(!mail.isSelected){
            holder.iconBack.setVisibility(View.GONE)
            resetIconYAxis(holder.avatarView)
            holder.avatarView.setVisibility(View.VISIBLE);
            FlipAnimator.flipView(mContext, holder.iconBack, holder.avatarView, false);

        }
    }

    private fun resetIconYAxis(view : View) {
        if (view.rotationY != 0.toFloat() ) {
            view.setRotationY(0.toFloat())
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
        iconAttachments = view.findViewById(R.id.email_has_attachments)
    }

}
