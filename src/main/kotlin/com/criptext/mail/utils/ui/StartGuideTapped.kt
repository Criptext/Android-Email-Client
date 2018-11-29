package com.criptext.mail.utils.ui

import android.app.Activity
import android.content.Context
import android.view.View
import com.criptext.mail.R
import com.criptext.mail.utils.DimmedPromptBackground
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class StartGuideTapped(val context: Context) {
    private val res = context.resources

    fun showViewTapped(view: View, activity: Activity, title: String){
        MaterialTapTargetPrompt.Builder(activity)
                .setTarget(view)
                .setFocalRadius(res.getDimension(R.dimen.focal_radius))
                .setFocalColour(res.getColor(R.color.white))
                .setBackgroundColour(res.getColor(android.R.color.transparent))
                .setPromptBackground(DimmedPromptBackground())
                .setPrimaryText(title)
                .setPrimaryTextSize(res.getDimension(R.dimen.start_guide_text))
                .setTextPadding(260f)
                .show()
    }

    fun showViewTappedTransparent(view: View, activity: Activity, title: String){
        MaterialTapTargetPrompt.Builder(activity)
                .setTarget(view)
                .setFocalRadius(res.getDimension(R.dimen.focal_radius_small_views))
                .setFocalColour(res.getColor(android.R.color.transparent))
                .setBackgroundColour(res.getColor(android.R.color.transparent))
                .setPromptBackground(DimmedPromptBackground())
                .setPrimaryText(title)
                .setPrimaryTextSize(res.getDimension(R.dimen.start_guide_text))
                .setTextPadding(260f)
                .show()
    }
}