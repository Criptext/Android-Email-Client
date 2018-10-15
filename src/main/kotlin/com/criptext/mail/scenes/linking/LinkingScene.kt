package com.criptext.mail.scenes.linking

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.androidui.progressdialog.IntervalTimer
import com.criptext.mail.scenes.signin.SignInSceneController
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.KeepWaitingSyncAlertDialog
import com.criptext.mail.utils.ui.RetrySyncAlertDialogOldDevice
import com.criptext.mail.utils.ui.ScrollingGradient

interface LinkingScene{

    fun attachView(model: LinkingModel, linkingUIObserver: LinkingUIObserver)
    fun showMessage(message : UIMessage)
    fun setProgress(message: UIMessage, progress: Int)
    fun startSucceedAnimation(launchMailboxScene: (
            linkingUIObserver: LinkingUIObserver) -> Unit)
    fun showKeepWaitingDialog()
    fun showRetrySyncDialog(result: GeneralResult)


    var linkingUIObserver: LinkingUIObserver?

    class Default(private val view: View): LinkingScene {

        private val context = view.context

        override var linkingUIObserver: LinkingUIObserver? = null



        private val loadingView: View = view.findViewById(R.id.viewAnimation)
        private val textViewStatus: TextView = view.findViewById(R.id.textViewStatus)
        private val textViewEmail: TextView = view.findViewById(R.id.textViewEmail)
        private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        private val progressBarNumber: TextView = view.findViewById(R.id.percentage_advanced)
        private val cancelSyncText: TextView = view.findViewById(R.id.cancelSync)
        private val keepWaitingDialog: KeepWaitingSyncAlertDialog = KeepWaitingSyncAlertDialog(context)
        private val retrySyncDialog: RetrySyncAlertDialogOldDevice = RetrySyncAlertDialogOldDevice(context)
        private val timer = IntervalTimer()

        override fun attachView(model: LinkingModel, linkingUIObserver: LinkingUIObserver) {
            this.linkingUIObserver = linkingUIObserver
            textViewEmail.text = model.email
            cancelSyncText.setOnClickListener {
                this.linkingUIObserver?.onCancelSync()
            }
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f,
                    context.resources.displayMetrics)
            progressBar.indeterminateDrawable = ScrollingGradient(px)
        }

        override fun startSucceedAnimation(launchMailboxScene: (
                linkingUIObserver: LinkingUIObserver) -> Unit) {
            loadingView.post {
                val animSucceed = initSuccessAnimatorSet(view.findViewById(R.id.viewCircle1),
                        view.findViewById(R.id.viewCircle2),
                        view.findViewById(R.id.viewCircle3), view.findViewById(R.id.viewCircle4),
                        view.findViewById(R.id.imageViewDevice1), view.findViewById(R.id.imageViewDevice2),
                        view.findViewById(R.id.imageViewSucceed))

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

            textViewEmail.setTextColor(ContextCompat.getColor(view.context, R.color.colorAccent))
            textViewStatus.text = view.resources.getText(R.string.device_ready)
        }


        private fun initSuccessAnimatorSet(circle1: View, circle2: View, circle3: View, circle4: View,
                                           device1: View, device2: View, viewSucceed: View): AnimatorSet {

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

        override fun setProgress(message: UIMessage, progress: Int) {
            textViewStatus.text = context.getLocalizedUIMessage(message)
            timer.start(50, Runnable {
                val stepProgress = progressBar.progress + 1
                if(stepProgress <= progress) {
                    updateProgress(stepProgress)
                }
            })
        }

        override fun showRetrySyncDialog(result: GeneralResult) {
            retrySyncDialog.showLinkDeviceAuthDialog(linkingUIObserver, result)
        }

        private fun updateProgress(progress: Int){
            if(progress >= 99) timer.stop()
            progressBar.progress = progress
            progressBarNumber.text = progress.toString().plus("%")
        }

        override fun showKeepWaitingDialog() {
            keepWaitingDialog.showLinkDeviceAuthDialog(linkingUIObserver)
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