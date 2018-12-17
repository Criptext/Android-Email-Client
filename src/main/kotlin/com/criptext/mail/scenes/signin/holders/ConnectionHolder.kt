package com.criptext.mail.scenes.signin.holders

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.core.content.ContextCompat
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.beardedhen.androidbootstrap.BootstrapProgressBar
import com.criptext.mail.R
import com.criptext.mail.androidui.progressdialog.IntervalTimer
import com.criptext.mail.scenes.signin.SignInSceneController
import com.criptext.mail.utils.*

/**
 * Created by sebas on 3/2/18.
 */

class ConnectionHolder(val view: View, val username: String, val authorizerType: DeviceUtils.DeviceType,
                       private val signInUIObserver: SignInSceneController.SignInUIObserver): BaseSignInHolder() {

    private val loadingView: View
    private val textViewStatus: TextView
    private val textViewEmail: TextView
    private val progressBar: BootstrapProgressBar
    private val progressBarNumber: TextView
    private val cancelSyncText: TextView
    private val oldDevice: ImageView
    private val timer = IntervalTimer()


    fun startSucceedAnimation(launchMailboxScene: (
            signInUIObserver: SignInSceneController.SignInUIObserver) -> Unit) {
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
                    launchMailboxScene(signInUIObserver!!)
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

    fun setProgress(message: UIMessage, progress: Int) {
        textViewStatus.text = view.context.getLocalizedUIMessage(message)
        if (progress >= 96) {
            progressBar.progress = 100
            progressBarNumber.text = 100.toString().plus("%")
        }
        else {
            val anim = UIUtils.animationForProgressBar(progressBar, progress,
                    progressBarNumber, 1000)
            anim.start()
        }
    }

    fun disableCancelSync(){
        cancelSyncText.isClickable = false
        cancelSyncText.visibility = View.INVISIBLE
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

    init {
        loadingView = view.findViewById(R.id.viewAnimation)
        textViewStatus = view.findViewById(R.id.textViewStatus)
        textViewEmail = view.findViewById(R.id.textViewEmail)
        textViewEmail.text = username.plus(EmailAddressUtils.CRIPTEXT_DOMAIN_SUFFIX)
        progressBar = view.findViewById(R.id.progressBar)
        progressBarNumber = view.findViewById(R.id.percentage_advanced)
        cancelSyncText = view.findViewById(R.id.cancelSync)
        oldDevice = view.findViewById(R.id.imageViewDevice1)

        when (authorizerType){
            DeviceUtils.DeviceType.PC, DeviceUtils.DeviceType.MacStore, DeviceUtils.DeviceType.MacInstaller,
            DeviceUtils.DeviceType.WindowsInstaller, DeviceUtils.DeviceType.WindowsStore,
            DeviceUtils.DeviceType.LinuxInstaller -> oldDevice.setImageResource(R.drawable.device_pc)
            else -> oldDevice.setImageResource(R.drawable.device_m)
        }

        cancelSyncText.setOnClickListener {
            signInUIObserver.onCancelSync()
        }
    }
}