package com.criptext.mail.utils

import android.animation.ValueAnimator
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import com.beardedhen.androidbootstrap.BootstrapProgressBar
import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.db.models.Label
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

object UIUtils{

    fun setProfilePicture(iv: ImageView, recipientId: String, name: String, runnable: Runnable?) {
        val url = Hosts.restApiBaseUrl.plus("/user/avatar/$recipientId")
        Picasso.get()
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(iv, object : Callback {
                    override fun onSuccess() {
                        runnable?.run()
                    }

                    override fun onError(e: Exception) {
                        Picasso.get()
                                .load(url)
                                .into(iv, object : Callback {
                                    override fun onSuccess() {
                                        runnable?.run()
                                    }

                                    override fun onError(e: Exception) {
                                        runnable?.run()
                                        iv.setImageBitmap(Utility.getBitmapFromText(
                                                name,250, 250))
                                    }
                                })
                    }
                })
    }

    fun animationForProgressBar(progressBar: BootstrapProgressBar, progress: Int, progressBarNumber: TextView,
                                duration: Long): ValueAnimator{
        val anim = ValueAnimator.ofInt(progressBar.progress, progress)
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            progressBarNumber.text = (`val`).toString().plus("%")
            progressBar.progress = progress
        }
        anim.duration = duration
        return anim
    }

    fun expand(v: View) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val targetHeight = v.measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.layoutParams.height = 1
        v.visibility = View.VISIBLE
        val a = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                v.layoutParams.height = if (interpolatedTime == 1f)
                    ViewGroup.LayoutParams.WRAP_CONTENT
                else {
                    val newHeight = (targetHeight * interpolatedTime).toInt()
                    if(newHeight == 0)
                        1
                    else
                        newHeight
                }
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // 1dp/ms
        a.duration = (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
    }

    fun collapse(v: View) {
        val initialHeight = v.measuredHeight

        val a = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // 1dp/ms
        a.duration = (initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
        v.startAnimation(a)
    }

    fun getLocalizedSystemLabelName(title: String): UIMessage{
        return when(title){
            Label.LABEL_SENT -> UIMessage(R.string.titulo_mailbox_sent)
            Label.LABEL_STARRED -> UIMessage(R.string.titulo_mailbox_starred)
            Label.LABEL_SPAM -> UIMessage(R.string.titulo_mailbox_spam)
            Label.LABEL_DRAFT -> UIMessage(R.string.titulo_mailbox_draft)
            Label.LABEL_TRASH -> UIMessage(R.string.titulo_mailbox_trash)
            Label.LABEL_ALL_MAIL -> UIMessage(R.string.titulo_mailbox_all_mail)
            else -> UIMessage(R.string.titulo_mailbox)
        }
    }

}

fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}