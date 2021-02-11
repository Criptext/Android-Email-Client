package com.criptext.mail.scenes.linking

import android.animation.*
import android.media.Image
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.criptext.mail.R
import com.criptext.mail.androidui.progressdialog.IntervalTimer
import com.criptext.mail.scenes.settings.privacy.PrivacyUIObserver
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.AccountSuspendedDialog
import com.criptext.mail.utils.ui.KeepWaitingSyncAlertDialog
import com.criptext.mail.utils.ui.ProgressBarAnimation
import com.criptext.mail.utils.ui.RetrySyncAlertDialogOldDevice
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.ui.data.GeneralAnimationData
import com.criptext.mail.utils.uiobserver.UIObserver


interface LinkingScene{

    fun attachView(model: LinkingModel, linkingUIObserver: LinkingUIObserver)
    fun showMessage(message : UIMessage)
    fun setProgress(progress: Int, onFinish:(() -> Unit)? = null)
    fun setProgressStatus(message: UIMessage, animationData: GeneralAnimationData? = null, onFinish: (() -> Unit)? = null)
    fun showCompleteExport()
    fun showKeepWaitingDialog()
    fun showRetrySyncDialog(result: GeneralResult)
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()


    var linkingUIObserver: LinkingUIObserver?

    class Default(private val view: View): LinkingScene {

        private val context = view.context

        override var linkingUIObserver: LinkingUIObserver? = null

        private val accountSuspended = AccountSuspendedDialog(context)

        private val loadingView: View = view.findViewById(R.id.viewAnimation)
        private val textViewStatus: TextView = view.findViewById(R.id.textViewStatus)
        private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        private val cancelSyncText: TextView = view.findViewById(R.id.cancelSync)
        private val statusImage: LottieAnimationView = view.findViewById(R.id.statusImage)
        private val completeImage: ImageView = view.findViewById(R.id.completeImage)
        private val keepWaitingDialog: KeepWaitingSyncAlertDialog = KeepWaitingSyncAlertDialog(context)
        private val retrySyncDialog: RetrySyncAlertDialogOldDevice = RetrySyncAlertDialogOldDevice(context)

        override fun attachView(model: LinkingModel, linkingUIObserver: LinkingUIObserver) {
            this.linkingUIObserver = linkingUIObserver
            cancelSyncText.visibility = View.VISIBLE
            cancelSyncText.setOnClickListener {
                this.linkingUIObserver?.onCancelSync()
            }
        }

        override fun showCompleteExport() {
            completeImage.visibility = View.VISIBLE
            statusImage.visibility = View.GONE
        }

        override fun setProgress(progress: Int, onFinish: (() -> Unit)?) {
            val anim = ProgressBarAnimation(progressBar, progressBar.progress, progress, null, onFinish)
            anim.duration = 1000
            progressBar.startAnimation(anim)
        }

        override fun setProgressStatus(message: UIMessage, animationData: GeneralAnimationData?, onFinish: (() -> Unit)?) {
            textViewStatus.text = context.getLocalizedUIMessage(message)
            if(animationData != null) {
                if(animationData.isLoop)
                    statusImage.repeatCount = ValueAnimator.INFINITE
                else
                    statusImage.repeatCount = 0
                statusImage.setMinAndMaxFrame(animationData.start, animationData.end)

                if(onFinish != null && !animationData.isLoop){
                    statusImage.addAnimatorListener(
                            object: AnimatorListenerAdapter(){
                                override fun onAnimationEnd(animation: Animator?) {
                                    onFinish()
                                }
                            }
                    )
                }
                statusImage.playAnimation()
            }
        }

        override fun showRetrySyncDialog(result: GeneralResult) {
            retrySyncDialog.showLinkDeviceAuthDialog(linkingUIObserver, result)
        }

        override fun showKeepWaitingDialog() {
            keepWaitingDialog.showLinkDeviceAuthDialog(linkingUIObserver)
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }


        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

    }
}