package com.criptext.mail.scenes.linking

import android.animation.*
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
    fun startSucceedAnimation(launchMailboxScene: (
            linkingUIObserver: LinkingUIObserver) -> Unit)
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
        private val keepWaitingDialog: KeepWaitingSyncAlertDialog = KeepWaitingSyncAlertDialog(context)
        private val retrySyncDialog: RetrySyncAlertDialogOldDevice = RetrySyncAlertDialogOldDevice(context)

        override fun attachView(model: LinkingModel, linkingUIObserver: LinkingUIObserver) {
            this.linkingUIObserver = linkingUIObserver
            cancelSyncText.visibility = View.VISIBLE
            cancelSyncText.setOnClickListener {
                this.linkingUIObserver?.onCancelSync()
            }
        }

        override fun startSucceedAnimation(launchMailboxScene: (
                linkingUIObserver: LinkingUIObserver) -> Unit) {
            loadingView.post {
                val animSucceed = initSuccessAnimatorSet(view.findViewById(R.id.statusImage))

                animSucceed.addListener(object : Animation.AnimationListener, Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator?) {
                    }

                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        launchMailboxScene(linkingUIObserver!!)
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                    }

                    override fun onAnimationRepeat(p0: Animation?) {
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                    }

                    override fun onAnimationStart(p0: Animation?) {
                    }

                })

                animSucceed.start()
            }
            textViewStatus.text = view.resources.getText(R.string.device_ready)
        }


        private fun initSuccessAnimatorSet(viewSucceed: View): AnimatorSet {

            val animArray = arrayOfNulls<ObjectAnimator>(1)
            val animObj = ObjectAnimator.ofFloat(viewSucceed, "alpha", 0.0f, 1f)
            initSuccessObjectAnim(animObj, 0)
            animArray[0] = animObj

            val animSet = AnimatorSet()
            animSet.playTogether(*animArray)
            animSet.duration = 1000
            return animSet
        }

        private fun initSuccessObjectAnim(animObj: ObjectAnimator, delay: Long) {
            if (delay > 0)
                animObj.startDelay = delay
        }

        override fun setProgress(progress: Int, onFinish: (() -> Unit)?) {
            val anim = ProgressBarAnimation(progressBar, progressBar.progress, progress, null, onFinish)
            anim.duration = 1000
            progressBar.startAnimation(anim)
        }

        override fun setProgressStatus(message: UIMessage, animationData: GeneralAnimationData?, onFinish: (() -> Unit)?) {
            textViewStatus.text = context.getLocalizedUIMessage(message)
            if(animationData != null) {
                statusImage.setMinAndMaxFrame(animationData.start, animationData.end)
                if(animationData.isLoop)
                    statusImage.repeatCount = ValueAnimator.INFINITE
                else if(onFinish != null){
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