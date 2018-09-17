package com.criptext.mail.scenes.linking

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.scenes.signin.SignInSceneController
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

interface LinkingScene{

    fun attachView(model: LinkingModel, linkingUIObserver: LinkingUIObserver)
    fun showMessage(message : UIMessage)
    fun startLoadingAnimation()
    fun startSucceedAnimation(launchMailboxScene: (
            signInUIObserver: SignInSceneController.SignInUIObserver) -> Unit)
    fun stopAnimationLoading()


    var linkingUIObserver: LinkingUIObserver?

    class Default(private val view: View): LinkingScene {

        private val context = view.context

        override var linkingUIObserver: LinkingUIObserver? = null



        private val loadingView: View = view.findViewById(R.id.viewAnimation)
        private val textViewStatus: TextView = view.findViewById(R.id.textViewStatus)
        private val textViewEmail: TextView = view.findViewById(R.id.textViewEmail)
        private var animLoading: AnimatorSet? = null
        var signInUIObserver: SignInSceneController.SignInUIObserver? = null

        override fun attachView(model: LinkingModel, linkingUIObserver: LinkingUIObserver) {
            textViewEmail.text = model.email
            startLoadingAnimation()
        }

        override fun startLoadingAnimation() {
            loadingView.post {
                animLoading = initSyncingAnimatorSet(view.findViewById(R.id.viewCircle1),
                        view.findViewById(R.id.viewCircle2),
                        view.findViewById(R.id.viewCircle3), view.findViewById(R.id.viewCircle4),
                        view.findViewById(R.id.viewCircle5), view.findViewById(R.id.viewCircle6),
                        view.findViewById(R.id.viewCircle7), view.findViewById(R.id.viewCircle8),
                        view.findViewById(R.id.viewCircle9), view.findViewById(R.id.viewCircle10),
                        view.findViewById(R.id.viewCircle11), view.findViewById(R.id.viewCircle12))
                animLoading!!.start()
            }
        }

        private fun initSyncObjectAnim(animObj: ObjectAnimator, delay: Long) {
            animObj.repeatMode = ValueAnimator.REVERSE
            animObj.repeatCount = -1
            if (delay > 0)
                animObj.startDelay = delay
        }

        private fun initSyncingAnimatorSet(circle1: View, circle2: View, circle3: View, circle4: View,
                                           circle5: View, circle6: View, circle7: View, circle8: View,
                                           circle9: View, circle10: View, circle11: View, circle12: View): AnimatorSet {

            val animArray = arrayOfNulls<ObjectAnimator>(12)
            var animObj = ObjectAnimator.ofFloat(circle7, "alpha", 0.1f, 1f)
            initSyncObjectAnim(animObj, 0)
            animArray[0] = animObj
            animObj = ObjectAnimator.ofFloat(circle8, "alpha", 0.1f, 1f)
            initSyncObjectAnim(animObj, 100)
            animArray[1] = animObj
            animObj = ObjectAnimator.ofFloat(circle9, "alpha", 0.1f, 1f)
            initSyncObjectAnim(animObj, 200)
            animArray[2] = animObj
            animObj = ObjectAnimator.ofFloat(circle10, "alpha", 0.1f, 1f)
            initSyncObjectAnim(animObj, 300)
            animArray[3] = animObj
            animObj = ObjectAnimator.ofFloat(circle11, "alpha", 0.1f, 1f)
            initSyncObjectAnim(animObj, 400)
            animArray[4] = animObj
            animObj = ObjectAnimator.ofFloat(circle12, "alpha", 0.1f, 1f)
            initSyncObjectAnim(animObj, 500)
            animArray[5] = animObj
            animObj = ObjectAnimator.ofFloat(circle1, "alpha", 1f, 0.1f)
            initSyncObjectAnim(animObj, 0)
            animArray[6] = animObj
            animObj = ObjectAnimator.ofFloat(circle2, "alpha", 1f, 0.1f)
            initSyncObjectAnim(animObj, 100)
            animArray[7] = animObj
            animObj = ObjectAnimator.ofFloat(circle3, "alpha", 1f, 0.1f)
            initSyncObjectAnim(animObj, 200)
            animArray[8] = animObj
            animObj = ObjectAnimator.ofFloat(circle4, "alpha", 1f, 0.1f)
            initSyncObjectAnim(animObj, 300)
            animArray[9] = animObj
            animObj = ObjectAnimator.ofFloat(circle5, "alpha", 1f, 0.1f)
            initSyncObjectAnim(animObj, 400)
            animArray[10] = animObj
            animObj = ObjectAnimator.ofFloat(circle6, "alpha", 1f, 0.1f)
            initSyncObjectAnim(animObj, 500)
            animArray[11] = animObj
            val animSet = AnimatorSet()
            animSet.playTogether(*animArray)
            animSet.duration = 500
            return animSet
        }


        override fun startSucceedAnimation(launchMailboxScene: (
                signInUIObserver: SignInSceneController.SignInUIObserver) -> Unit) {
            animLoading!!.cancel()
            loadingView.post {
                val animSucceed = initSuccessAnimatorSet(view.findViewById(R.id.viewCircle1),
                        view.findViewById(R.id.viewCircle2),
                        view.findViewById(R.id.viewCircle3), view.findViewById(R.id.viewCircle4),
                        view.findViewById(R.id.viewCircle5), view.findViewById(R.id.viewCircle6),
                        view.findViewById(R.id.viewCircle7), view.findViewById(R.id.viewCircle8),
                        view.findViewById(R.id.viewCircle9), view.findViewById(R.id.viewCircle10),
                        view.findViewById(R.id.viewCircle11), view.findViewById(R.id.viewCircle12),
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


        private fun initSuccessAnimatorSet(circle1: View, circle2: View, circle3: View, circle4: View,
                                           circle5: View, circle6: View, circle7: View, circle8: View,
                                           circle9: View, circle10: View, circle11: View, circle12: View,
                                           device1: View, device2: View, viewSucceed: View): AnimatorSet {

            val animArray = arrayOfNulls<ObjectAnimator>(15)
            var animObj = ObjectAnimator.ofFloat(circle7, "alpha", 1f, 0f)
            initSuccessObjectAnim(animObj, 0)
            animArray[0] = animObj
            animObj = ObjectAnimator.ofFloat(circle8, "alpha", 1f, 0f)
            initSuccessObjectAnim(animObj, 100)
            animArray[1] = animObj
            animObj = ObjectAnimator.ofFloat(circle9, "alpha", 1f, 0f)
            initSuccessObjectAnim(animObj, 200)
            animArray[2] = animObj
            animObj = ObjectAnimator.ofFloat(circle10, "alpha", 1f, 0f)
            initSuccessObjectAnim(animObj, 300)
            animArray[3] = animObj
            animObj = ObjectAnimator.ofFloat(circle11, "alpha", 1f, 0f)
            initSuccessObjectAnim(animObj, 400)
            animArray[4] = animObj
            animObj = ObjectAnimator.ofFloat(circle12, "alpha", 1f, 0f)
            initSuccessObjectAnim(animObj, 500)
            animArray[5] = animObj
            animObj = ObjectAnimator.ofFloat(circle1, "alpha", 1f, 0.0f)
            initSuccessObjectAnim(animObj, 0)
            animArray[6] = animObj
            animObj = ObjectAnimator.ofFloat(circle2, "alpha", 1f, 0.0f)
            initSuccessObjectAnim(animObj, 100)
            animArray[7] = animObj
            animObj = ObjectAnimator.ofFloat(circle3, "alpha", 1f, 0.0f)
            initSuccessObjectAnim(animObj, 200)
            animArray[8] = animObj
            animObj = ObjectAnimator.ofFloat(circle4, "alpha", 1f, 0.0f)
            initSuccessObjectAnim(animObj, 300)
            animArray[9] = animObj
            animObj = ObjectAnimator.ofFloat(circle5, "alpha", 1f, 0.0f)
            initSuccessObjectAnim(animObj, 400)
            animArray[10] = animObj
            animObj = ObjectAnimator.ofFloat(circle6, "alpha", 1f, 0.0f)
            initSuccessObjectAnim(animObj, 500)
            animArray[11] = animObj

            animObj = ObjectAnimator.ofFloat(device1, "x", device1.x, circle2.x
                    + circle2.width + circle3.width)
            initSuccessObjectAnim(animObj, 600)
            animArray[12] = animObj

            animObj = ObjectAnimator.ofFloat(device2, "x", device2.x, circle8.x)
            initSuccessObjectAnim(animObj, 600)
            animArray[13] = animObj

            animObj = ObjectAnimator.ofFloat(viewSucceed, "alpha", 0.0f, 1f)
            initSuccessObjectAnim(animObj, 600)
            animArray[14] = animObj

            val animSet = AnimatorSet()
            animSet.playTogether(*animArray)
            animSet.duration = 700
            return animSet
        }

        private fun initSuccessObjectAnim(animObj: ObjectAnimator, delay: Long) {
            if (delay > 0)
                animObj.startDelay = delay
        }

        override fun stopAnimationLoading() {
            animLoading!!.cancel()
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