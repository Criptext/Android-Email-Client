package com.criptext.mail.scenes.syncing.holders

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ProgressBarAnimation
import com.criptext.mail.utils.ui.data.GeneralAnimationData

class SyncImportHolder(
        val view: View
): BaseSyncingHolder() {

    private val skipText: TextView
    private val statusImage: LottieAnimationView
    private val completeImage: ImageView
    private val statusMessage: TextView
    private val progressBar: ProgressBar

    init {
        skipText = view.findViewById(R.id.cancelSync)
        statusImage = view.findViewById(R.id.statusImage)
        completeImage = view.findViewById(R.id.completeImage)
        statusMessage = view.findViewById(R.id.textViewStatus)
        progressBar = view.findViewById(R.id.progressBar)
        setListeners()
    }

    fun setStatus(message: UIMessage, animationData: GeneralAnimationData?, onFinish: (() -> Unit)?){
        //statusMessage.text = view.context.getLocalizedUIMessage(message)
        //if(drawable != null) statusImage.setImageResource(drawable)

        if(animationData != null) {
            fun playNewAnimation(){
                if(animationData.isLoop)
                    statusImage.repeatCount = ValueAnimator.INFINITE
                else
                    statusImage.repeatCount = 0
                statusImage.setMinAndMaxFrame(animationData.start, animationData.end)

                if(onFinish != null && !animationData.isLoop){
                    statusImage.addAnimatorListener(
                            object: AnimatorListenerAdapter(){
                                override fun onAnimationEnd(animation: Animator?) {
                                    statusMessage.text = view.context.getLocalizedUIMessage(message)
                                    onFinish()
                                }
                            }
                    )
                }
                statusImage.playAnimation()
            }
            if(statusImage.repeatCount == ValueAnimator.INFINITE){
                statusImage.repeatCount = 1
                statusImage.addAnimatorListener(
                        object: AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                playNewAnimation()
                            }
                        }
                )
            } else {
                playNewAnimation()
            }
        } else {
            statusMessage.text = view.context.getLocalizedUIMessage(message)
        }
    }

    fun showCompleteImport(){
        completeImage.visibility = View.VISIBLE
        statusImage.visibility = View.GONE
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
