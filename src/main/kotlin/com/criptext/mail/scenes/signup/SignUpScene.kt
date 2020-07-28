package com.criptext.mail.scenes.signup

import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.scenes.signup.customize.holder.CustomizeAccountCreatedHolder
import com.criptext.mail.scenes.signup.holders.*
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.ProgressButtonState

/**
 * Created by sebas on 2/15/18.
 */

interface SignUpScene {

    fun showError(message : UIMessage)
    fun initLayout(model: SignUpSceneModel, signInUIObserver: SignUpSceneController.SignUpUIObserver)
    fun setInputState(layoutState: SignUpLayoutState, state: FormInputState)
    fun setPasswordCheck(isNotUsername: Boolean, isAtLeastEightChars: Boolean)
    fun setConfirmPasswordCheck(passwordMatches: FormInputState)
    fun setSubmitButtonState(state : ProgressButtonState)
    fun showGeneratingKeys(show: Boolean)

    var uiObserver: SignUpSceneController.SignUpUIObserver?
    val signUpSucceed: Boolean?

    class SignUpSceneView(private val view: View): SignUpScene {

        private var holder: BaseSignUpHolder

        private val viewGroup = view.parent as ViewGroup

        override var uiObserver: SignUpSceneController.SignUpUIObserver? = null
            set(value) {
                holder.uiObserver = value
                field = value
            }

        init {
            val signUpLayout = View.inflate(
                    view.context,
                    R.layout.holder_sign_up_name, viewGroup)
            holder = SignUpNameHolder(signUpLayout, "")
        }

        override fun initLayout(model: SignUpSceneModel, signInUIObserver: SignUpSceneController.SignUpUIObserver) {
            removeAllViews()
            val state = model.state
            holder = when (state) {
                is SignUpLayoutState.Name -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sign_up_name, viewGroup)
                    SignUpNameHolder(newLayout, state.name)
                }
                is SignUpLayoutState.EmailHandle -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sign_up_user, viewGroup)
                    SignUpEmailHandleHolder(newLayout, state.emailHandle)
                }
                is SignUpLayoutState.Password -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sign_up_password, viewGroup)
                    SignUpPasswordHolder(newLayout, state.password)
                }
                is SignUpLayoutState.ConfirmPassword -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sign_up_confirm_password, viewGroup)
                    SignUpConfirmPasswordHolder(newLayout, state.confirmPassword)
                }
                is SignUpLayoutState.RecoveryEmail -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sign_up_recovery_email, viewGroup)
                    SignUpRecoveryEmailHolder(newLayout, state.recoveryEmail)
                }
                is SignUpLayoutState.TermsAndConditions -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sign_up_terms_and_conditions, viewGroup)
                    SignUpTermsAndConditionsHolder(newLayout)
                }
            }
            this.uiObserver = signInUIObserver
        }

        override fun setInputState(layoutState: SignUpLayoutState, state: FormInputState) {
            when(layoutState) {
                is SignUpLayoutState.Name -> {
                    val currentHolder = holder as SignUpNameHolder
                    currentHolder.setState(state)
                }
                is SignUpLayoutState.EmailHandle -> {
                    val currentHolder = holder as SignUpEmailHandleHolder
                    currentHolder.setState(state)
                }
                is SignUpLayoutState.RecoveryEmail -> {
                    val currentHolder = holder as SignUpRecoveryEmailHolder
                    currentHolder.setState(state)
                }
            }
        }

        override fun setPasswordCheck(isNotUsername: Boolean, isAtLeastEightChars: Boolean) {
            val currentHolder = holder as? SignUpPasswordHolder
            currentHolder?.checkConditions(isNotUsername, isAtLeastEightChars)
        }

        override fun setConfirmPasswordCheck(passwordMatches: FormInputState) {
            val currentHolder = holder as? SignUpConfirmPasswordHolder
            currentHolder?.checkConditions(passwordMatches)
        }

        override fun setSubmitButtonState(state: ProgressButtonState) {
            holder.setSubmitButtonState(state)
        }

        override fun showGeneratingKeys(show: Boolean) {
            val currentHolder = holder as? SignUpTermsAndConditionsHolder
            currentHolder?.showCreatingAccount(show)
        }

        override val signUpSucceed: Boolean? = false

        override fun showError(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    view.context,
                    view.context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

        private fun removeAllViews() {
            holder.uiObserver = null
            viewGroup.removeAllViews()
        }
    }
}
