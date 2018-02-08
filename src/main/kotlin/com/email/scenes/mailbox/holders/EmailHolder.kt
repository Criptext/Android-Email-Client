package com.email.scenes.mailbox.holders

import android.content.Context
import android.media.Image
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.email.R
import com.email.scenes.MailItemHolder
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.Utility
import com.email.utils.anim.FlipAnimator
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by sebas on 1/24/18.
 */

class EmailHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener , MailItemHolder {

    private val headerView : TextView
    private val subjectView : TextView
    private val previewView : TextView
    private val dateView : TextView
    private val countView : TextView
    private val context : Context
    private val attachment : ImageView
    private val avatarView: CircleImageView
    private val iconBack: ImageView

    init {
        view.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
    }

    fun bindMail(emailThread: EmailThread) {
        previewView.text = emailThread.headerPreview
        headerView.text = emailThread.headerPreview
        avatarView.setImageBitmap(Utility.getBitmapFromText(emailThread.preview, emailThread.preview.get(0).toString(), 250, 250))

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

    fun toggleSelectedStatus(selected: Boolean) {
        if(selected) {
            view.setBackgroundColor(view.resources.getColor(R.color.mail_item_selected))
            avatarView.visibility = View.GONE
            iconBack.visibility = View.VISIBLE
        }else {
            view.setBackgroundColor(view.resources.getColor(R.color.mail_item_not_selected))
            avatarView.visibility = View.VISIBLE
            iconBack.visibility = View.GONE
        }
    }
    fun hideMultiselect() {
        view.setBackgroundColor(view.resources.getColor(R.color.mail_item_not_selected))
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
            view.setRotationY(0.toFloat());
        }
    }

    init {
        headerView = view.findViewById(R.id.email_header) as TextView
        avatarView = view.findViewById(R.id.mail_item_left_name) as CircleImageView
        subjectView = view.findViewById(R.id.email_subject) as TextView
        previewView = view.findViewById(R.id.email_preview) as TextView
        dateView = view.findViewById(R.id.email_date) as TextView
        countView = view.findViewById(R.id.email_count) as TextView
        iconBack = view.findViewById(R.id.icon_back) as ImageView
        attachment = view.findViewById(R.id.email_has_attachments) as ImageView
        context = view.context
    }

}
