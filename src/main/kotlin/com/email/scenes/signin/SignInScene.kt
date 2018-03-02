package com.email.scenes.signin

import android.animation.*
import android.annotation.SuppressLint
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.*
import com.email.R
import com.email.scenes.connection.ConnectionScene
import com.email.scenes.signin.holders.SignInFormHolder

/**
 * Created by sebas on 2/15/18.
 */

interface SignInScene {
    fun drawNormalSignInOptions()
    fun toggleSignUpPressed(isPressed: Boolean)
    fun toggleLoginProgressBar(isLoggingIn : Boolean)
    fun drawError()
    fun drawSuccess()
    fun initListeners(signInListener: SignInSceneController.SignInListener)
    fun getConnectionScene() : ConnectionScene
    fun showConnectionScene()
    fun showFormScene()
    fun startLoadingAnimation()
    fun startSucceedAnimation()
    fun stopAnimationLoading()
    fun startAnimation()
    fun initFormUI()

    class SignInSceneView(val view: View): SignInScene {
        override fun startAnimation() {
            startLoadingAnimation()

            Handler().postDelayed({
                startSucceedAnimation()
            }, 3000)
        }

        private val res = view.context.resources
        private val viewGroup = view.parent as ViewGroup
        private var signInFormHolder: SignInFormHolder? = null

        private lateinit var connectionLayout : View
        private lateinit var loadingView: View
        private lateinit var textViewStatus: TextView
        private lateinit var textViewEmail: TextView
        private lateinit var animLoading: AnimatorSet

        private lateinit var formLayout : View


        private lateinit var signInListener: SignInSceneController.SignInListener

        fun assignUsernameInputListener(){
            signInFormHolder?.assignUsernameInputListener()
        }

        @SuppressLint("ClickableViewAccessibility")
        fun assignSignUpTextViewListener() {
            signInFormHolder?.assignSignUpTextViewListener()
        }

        private fun assignLoginButtonListener() {
            signInFormHolder?.assignLoginButtonListener()
        }

        override fun toggleSignUpPressed(isPressed: Boolean){
            signInFormHolder?.toggleSignUpPressed(isPressed)
        }
        @SuppressLint("RestrictedApi")
        override fun drawError() {
            signInFormHolder?.drawError()
        }


        @SuppressLint("RestrictedApi")
        override fun drawNormalSignInOptions(){
            signInFormHolder?.drawNormalSignInOptions()
        }

        override fun drawSuccess() {
            TODO("Show progress dialog...")
        }

        override fun toggleLoginProgressBar(isLoggingIn : Boolean){
            signInFormHolder?.toggleLoginProgressBar(isLoggingIn)
        }

        init {
            showFormScene()
        }

        override fun initFormUI() {
        }
        override fun initListeners(
                signInListener: SignInSceneController.SignInListener
        ) {
            this.signInListener = signInListener
            assignLoginButtonListener()
            assignSignUpTextViewListener()
            assignUsernameInputListener()

            if(signInFormHolder != null) {
                signInFormHolder!!.signInListener = signInListener
            }
        }

        override fun getConnectionScene(): ConnectionScene {
            return ConnectionScene.ConnectionSceneView(connectionLayout)
        }

        override fun showConnectionScene() {
            removeAllViews()
            connectionLayout = View.inflate(
                    view.context,
                    R.layout.activity_connection, viewGroup)

            loadingView = connectionLayout.findViewById(R.id.viewAnimation)
            textViewStatus = connectionLayout.findViewById(R.id.textViewStatus)
            textViewEmail = connectionLayout.findViewById(R.id.textViewEmail)
        }

        private fun removeAllViews() {
            viewGroup.removeAllViews()
        }

        override fun showFormScene() {
            removeAllViews()
            formLayout = View.inflate(
                    view.context,
                    R.layout.activity_form_signin, viewGroup)
            formLayout = formLayout.findViewById(R.id.signin_form_container)
            signInFormHolder = SignInFormHolder(formLayout)
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

        private fun initSyncObjectAnim(animObj: ObjectAnimator, delay: Long) {
            animObj.repeatMode = ValueAnimator.REVERSE
            animObj.repeatCount = -1
            if (delay > 0)
                animObj.startDelay = delay
        }

        override fun startLoadingAnimation() {
            loadingView.post {
                animLoading = initSyncingAnimatorSet(connectionLayout.findViewById(R.id.viewCircle1),
                        connectionLayout.findViewById(R.id.viewCircle2),
                        connectionLayout.findViewById(R.id.viewCircle3), connectionLayout.findViewById(R.id.viewCircle4),
                        connectionLayout.findViewById(R.id.viewCircle5), connectionLayout.findViewById(R.id.viewCircle6),
                        connectionLayout.findViewById(R.id.viewCircle7), connectionLayout.findViewById(R.id.viewCircle8),
                        connectionLayout.findViewById(R.id.viewCircle9), connectionLayout.findViewById(R.id.viewCircle10),
                        connectionLayout.findViewById(R.id.viewCircle11), connectionLayout.findViewById(R.id.viewCircle12))
                animLoading.start()
            }
        }

        override fun startSucceedAnimation() {
            animLoading.cancel()
            loadingView.post {
                val animSucceed = initSuccessAnimatorSet(connectionLayout.findViewById(R.id.viewCircle1), connectionLayout.findViewById(R.id.viewCircle2),
                        connectionLayout.findViewById(R.id.viewCircle3), connectionLayout.findViewById(R.id.viewCircle4),
                        connectionLayout.findViewById(R.id.viewCircle5), connectionLayout.findViewById(R.id.viewCircle6),
                        connectionLayout.findViewById(R.id.viewCircle7), connectionLayout.findViewById(R.id.viewCircle8),
                        connectionLayout.findViewById(R.id.viewCircle9), connectionLayout.findViewById(R.id.viewCircle10),
                        connectionLayout.findViewById(R.id.viewCircle11), connectionLayout.findViewById(R.id.viewCircle12),
                        connectionLayout.findViewById(R.id.imageViewDevice1), connectionLayout.findViewById(R.id.imageViewDevice2),
                        connectionLayout.findViewById(R.id.imageViewSucceed))

                animSucceed.addListener(object : Animation.AnimationListener, Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        showFormScene()
                        initListeners(signInListener = signInListener)

                    }

                    override fun onAnimationStart(p0: Animator?) {
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
            textViewStatus.text = res.getText(R.string.device_ready)
        }

        private fun initSuccessObjectAnim(animObj: ObjectAnimator, delay: Long) {
            if (delay > 0)
                animObj.startDelay = delay
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

        override fun stopAnimationLoading() {
            animLoading.cancel()
        }
    }

}

