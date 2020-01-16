package com.criptext.mail.utils

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.AttrRes
import com.beardedhen.androidbootstrap.BootstrapProgressBar
import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.file.FileUtils
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.File


object UIUtils{

    fun checkForCacheCleaning(storage: KeyValueStorage, cacheDir: File, activeAccount: ActiveAccount) {
        val currentMillis = System.currentTimeMillis()
        val millisInADays = (24 * 60 * 60 * 1000).toLong()
        val savedTime = storage.getLong(KeyValueStorage.StringKey.CacheResetTimestamp, 0L)
        if(savedTime < currentMillis - millisInADays){
            Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.domain}/${activeAccount.recipientId}"))
            Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.recipientId}"))
            storage.putLong(KeyValueStorage.StringKey.CacheResetTimestamp, currentMillis)
            clearImageDiskCache(cacheDir)
        }
    }

    private fun clearImageDiskCache(cacheDir: File): Boolean {
        val cache = File(cacheDir, "picasso-cache")
        return if (cache.exists() && cache.isDirectory) {
            FileUtils.deleteDir(cache)
        } else false
    }

    fun forceCacheClear(storage: KeyValueStorage, cacheDir: File, activeAccount: ActiveAccount) {
        val currentMillis = System.currentTimeMillis()
        Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.domain}/${activeAccount.recipientId}"))
        Picasso.get().invalidate(Hosts.restApiBaseUrl.plus("/user/avatar/${activeAccount.recipientId}"))
        storage.putLong(KeyValueStorage.StringKey.CacheResetTimestamp, currentMillis)
        clearImageDiskCache(cacheDir)
    }

    fun setProfilePicture(iv: ImageView, resources: Resources, domain: String, recipientId: String, name: String, runnable: Runnable?) {
        val url = Hosts.restApiBaseUrl.plus("/user/avatar/$domain/$recipientId")
        val bitmapFromText = Utility.getBitmapFromText(
                name,250, 250)
        Picasso.get()
                .load(url)
                .placeholder(BitmapDrawable(resources, bitmapFromText))
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(iv, object : Callback {
                    override fun onSuccess() {
                        runnable?.run()
                    }

                    override fun onError(e: Exception) {
                        Picasso.get()
                                .load(url)
                                .placeholder(BitmapDrawable(resources, bitmapFromText))
                                .into(iv, object : Callback {
                                    override fun onSuccess() {
                                        runnable?.run()
                                    }

                                    override fun onError(e: Exception) {
                                        runnable?.run()
                                        iv.setImageBitmap(bitmapFromText)
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

    fun animationForProgressBar(progressBar: ProgressBar, progress: Int, progressBarNumber: TextView,
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
            Label.LABEL_INBOX -> UIMessage(R.string.titulo_mailbox)
            Label.LABEL_SENT -> UIMessage(R.string.titulo_mailbox_sent)
            Label.LABEL_STARRED -> UIMessage(R.string.titulo_mailbox_starred)
            Label.LABEL_SPAM -> UIMessage(R.string.titulo_mailbox_spam)
            Label.LABEL_DRAFT -> UIMessage(R.string.titulo_mailbox_draft)
            Label.LABEL_TRASH -> UIMessage(R.string.titulo_mailbox_trash)
            Label.LABEL_ALL_MAIL -> UIMessage(R.string.titulo_mailbox_all_mail)
            else -> UIMessage(R.string.titulo_mailbox_custom, arrayOf(title))
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