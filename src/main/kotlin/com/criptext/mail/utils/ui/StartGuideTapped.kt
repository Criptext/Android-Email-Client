package com.criptext.mail.utils.ui

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class StartGuideTapped(val context: Context) {
    private val res = context.resources

    fun showViewTapped(view: View, activity: Activity, title: Int, dimension: Int) {
        when (title) {
            R.string.start_guide_email -> showTapViewTarget(view, activity, title, dimension, 300f, android.R.color.white, R.dimen.focal_radius)
            R.string.start_guide_notification -> showTapViewTarget(view, activity, title, dimension, 260f, android.R.color.transparent, R.dimen.focal_radius_small_views)
            R.string.start_guide_email_read -> showTapViewTarget(view, activity, title, dimension, 320f, R.color.white, R.dimen.focal_radius)
            R.string.start_guide_secure_attachments -> showTapViewTarget(view, activity, title)
            R.string.start_guide_secure -> showTapViewTarget(view, activity, title, dimension, 320f, android.R.color.transparent, R.dimen.focal_radius_small_views)
            else -> showTapViewTarget(view, activity, title, dimension, 260f, R.color.white, R.dimen.focal_radius)
        }
    }


    private fun showTapViewTarget(view: View, activity: Activity, title: Int, dimension: Int, padding: Float, color: Int, focal: Int){
        MaterialTapTargetPrompt.Builder(activity)
                .setTarget(view)
                .setFocalPadding(res.getDimension(dimension))
                .setFocalRadius(res.getDimension(focal))
                .setFocalColour(ContextCompat.getColor(context, color))
                .setBackgroundColour(ContextCompat.getColor(context, android.R.color.transparent))
                .setPromptBackground(DimmedPromptBackground())
                .setPrimaryText(res.getString(title))
                .setPrimaryTextSize(res.getDimension(R.dimen.start_guide_text))
                .setTextPadding(padding)
                .show()
    }

    private fun showTapViewTarget(view: View, activity: Activity, title: Int){
        MaterialTapTargetPrompt.Builder(activity)
                .setTarget(view)
                .setFocalColour(ContextCompat.getColor(context, R.color.white))
                .setBackgroundColour(ContextCompat.getColor(context, android.R.color.transparent))
                .setPromptBackground(DimmedPromptBackground())
                .setPrimaryText(res.getString(title))
                .setPrimaryTextSize(res.getDimension(R.dimen.start_guide_text))
                .show()
    }
}