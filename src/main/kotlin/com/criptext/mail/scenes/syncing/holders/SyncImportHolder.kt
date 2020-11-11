package com.criptext.mail.scenes.syncing.holders

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ProgressBarAnimation

class SyncImportHolder(
        val view: View
): BaseSyncingHolder() {

    private val skipText: TextView
    private val statusImage: ImageView
    private val statusMessage: TextView
    private val progressBar: ProgressBar

    init {
        skipText = view.findViewById(R.id.cancelSync)
        statusImage = view.findViewById(R.id.statusImage)
        statusMessage = view.findViewById(R.id.textViewStatus)
        progressBar = view.findViewById(R.id.progressBar)
        setListeners()
    }

    fun setStatus(message: UIMessage, drawable: Int?){
        statusMessage.text = view.context.getLocalizedUIMessage(message)
        if(drawable != null) statusImage.setImageResource(drawable)
    }

    fun disableSkip(){
        skipText.visibility = View.GONE
    }

    fun setProgress(progress: Int, onFinish: (() -> Unit)?) {
        val anim = ProgressBarAnimation(progressBar, progressBar.progress, progress, null, onFinish)
        anim.duration = 1000
        progressBar.startAnimation(anim)
    }

    private fun setListeners() {
        skipText.setOnClickListener {
            uiObserver?.onRetrySyncCancel()
        }
    }
}
