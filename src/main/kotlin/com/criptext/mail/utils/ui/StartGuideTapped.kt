package com.criptext.mail.utils.ui

import android.app.Activity
import android.content.Context
import android.view.View
import com.criptext.mail.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class StartGuideTapped(val context: Context) {
    private val res = context.resources

    fun showViewTapped(view: View, activity: Activity, title: Int, dimension: Int) {
        when (title) {
            R.string.start_guide_email -> showTapViewTarget(view, activity, title, dimension, 300f, android.R.color.white, R.dimen.focal_radius)
            R.string.start_guide_notification -> showTapViewTarget(view, activity, title, dimension, 260f, android.R.color.transparent, R.dimen.focal_radius_small_views)
            R.string.start_guide_email_read -> showTapViewTarget(view, activity, title, dimension, 440f, R.color.white, R.dimen.focal_radius)
            else -> showTapViewTarget(view, activity, title, dimension, 260f, R.color.white, R.dimen.focal_radius)
        }
    }


    private fun showTapViewTarget(view: View, activity: Activity, title: Int, dimension: Int, padding: Float, color: Int, focal: Int){
        MaterialTapTargetPrompt.Builder(activity)
                .setTarget(view)
                .setFocalPadding(res.getDimension(dimension))
                .setFocalRadius(res.getDimension(focal))
                .setFocalColour(res.getColor(color))
                .setBackgroundColour(res.getColor(android.R.color.transparent))
                .setPromptBackground(DimmedPromptBackground())
                .setPrimaryText(res.getString(title))
                .setPrimaryTextSize(res.getDimension(R.dimen.start_guide_text))
                .setTextPadding(padding)
                .show()
    }
}