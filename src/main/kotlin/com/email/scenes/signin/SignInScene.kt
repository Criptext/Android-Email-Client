package com.email.scenes.signin

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Handler
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.email.R
import com.email.scenes.connection.ConnectionScene

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

    class SignInSceneView(val view: View): SignInScene {
        override fun startAnimation() {
            startLoadingAnimation()

            Handler().postDelayed({
                startSucceedAnimation()
            }, 3000)
        }

        private val res = view.context.resources
        private val viewGroup = view.parent as ViewGroup
        private val usernameInput : AppCompatEditText
        private val usernameInputLayout : TextInputLayout
        private val signInButton : Button
        private val signUpTextView: TextView
        private val progressBar: ProgressBar
        private val imageError: ImageView

        private lateinit var connectionLayout : View
        private lateinit var loadingView: View
        private lateinit var textViewStatus: TextView
        private lateinit var textViewEmail: TextView
        private lateinit var animLoading: AnimatorSet

        private val formLayout : View

        private val shouldButtonBeEnabled : Boolean
            get() = usernameInputLayout.hint == "Username" && usernameInput.text.length > 0

        private lateinit var signInListener: SignInSceneController.SignInListener
        fun assignLoginButtonListener() {
            signInButton.setOnClickListener {
                // start progress dialog... change UI
                signInListener.onLoginClick()
            }
        }

        fun assignUsernameInputListener(){
            usernameInputLayout.setOnFocusChangeListener { _, isFocused ->
                signInListener.toggleUsernameFocusState(isFocused)
            }

            usernameInput.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    signInListener.onUsernameTextChanged(text.toString())
                }

            })
        }

        @SuppressLint("ClickableViewAccessibility")
        fun assignSignUpTextViewListener() {
            signUpTextView.setOnClickListener{
                signInListener.goToSignUp()
            }
        }

        override fun toggleSignUpPressed(isPressed: Boolean){
            if(isPressed) {
                signUpTextView.setTextColor(
                        ContextCompat.getColor(view.context, R.color.black))
            } else {
                signUpTextView.setTextColor(
                        ContextCompat.getColor(view.context, R.color.white))
            }
        }
        @SuppressLint("RestrictedApi")
        override fun drawError() {
            usernameInputLayout.hint = "Username does not exist"
            usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login_error)
            usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.black))
            imageError.visibility = View.VISIBLE
            signInButton.isEnabled  = false
        }

        @SuppressLint("RestrictedApi")
        private fun setUsernameBackgroundTintList() {
            usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.signup_hint_color))
        }

        @SuppressLint("RestrictedApi")
        override fun drawNormalSignInOptions(){
            usernameInputLayout.hint = "Username"
            usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
            usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.signup_hint_color))
            signInButton.isEnabled = shouldButtonBeEnabled
            imageError.visibility = View.INVISIBLE
        }

        override fun drawSuccess() {
            TODO("Show progress dialog...")
        }

        override fun toggleLoginProgressBar(isLoggingIn : Boolean){
            if(isLoggingIn) {
                signInButton.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            } else {
                signInButton.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }

        private fun showNormalTints(){
            usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
            setUsernameBackgroundTintList()
        }
        init {
            usernameInput = view.findViewById(R.id.input_username)
            signInButton = view.findViewById(R.id.signin_button)
            usernameInputLayout = view.findViewById(R.id.input_username_layout)
            signUpTextView = view.findViewById(R.id.signup_textview)
            progressBar = view.findViewById(R.id.signin_progress_login)
            imageError = view.findViewById(R.id.signin_error_image)
            formLayout = view.findViewById(R.id.signin_form_container)
            signInButton.isEnabled  = false
            showNormalTints()
        }

        override fun initListeners(
                signInListener: SignInSceneController.SignInListener
        ) {
            this.signInListener = signInListener
            assignLoginButtonListener()
            assignSignUpTextViewListener()
            assignUsernameInputListener()
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
            viewGroup.addView(formLayout)
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

