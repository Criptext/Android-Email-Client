package com.email.scenes.signin

import android.annotation.SuppressLint
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.scenes.signin.holders.ConnectionHolder
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
    fun showConnectionHolder()
    fun showFormHolder()
    fun startLoadingAnimation()
    fun startSucceedAnimation(showForm: (
            signInListener: SignInSceneController.SignInListener) -> Unit)
    fun stopAnimationLoading()
    fun startAnimation()
    fun initFormUI()

    class SignInSceneView(val view: View): SignInScene {
        override fun startAnimation() {
            startLoadingAnimation()

            Handler().postDelayed({
                startSucceedAnimation(showForm)
            }, 3000)
        }

        private val res = view.context.resources
        private val viewGroup = view.parent as ViewGroup
        private var signInFormHolder: SignInFormHolder? = null
        private var connectionHolder: ConnectionHolder? = null


        private var signInListener: SignInSceneController.SignInListener? = null
            set(value) {
                signInFormHolder?.signInListener = value
                connectionHolder?.signInListener = value
                field = value
            }

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
            showFormHolder()
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
          }


        override fun showConnectionHolder() {
            removeAllViews()
            val connectionLayout = View.inflate(
                    view.context,
                    R.layout.activity_connection, viewGroup)

            connectionHolder = ConnectionHolder(connectionLayout)
            if(connectionHolder != null) {
                connectionHolder!!.signInListener = signInListener
            }
        }

        private fun removeAllViews() {
            viewGroup.removeAllViews()
            signInListener = null
        }

        override fun showFormHolder() {
            removeAllViews()
            val layout = View.inflate(
                    view.context,
                    R.layout.activity_form_signin, viewGroup)
            val formLayout = layout.findViewById<View>(R.id.signin_form_container)
            signInFormHolder = SignInFormHolder(formLayout)
        }


        override fun startLoadingAnimation() {
            connectionHolder?.startLoadingAnimation()
        }

        override fun startSucceedAnimation(showForm: (
                signInListener: SignInSceneController.SignInListener) -> Unit) {
            connectionHolder?.startSucceedAnimation(showForm)
        }
        private val showForm = {
            signInListener: SignInSceneController.SignInListener ->
            showFormHolder()
            initListeners(signInListener)
        }

        override fun stopAnimationLoading() {
            connectionHolder?.stopAnimationLoading()
        }
    }

}

