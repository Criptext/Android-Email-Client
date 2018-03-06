package com.email.scenes.signin

import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.email.R
import com.email.scenes.keygeneration.KeyGenerationHolder
import com.email.scenes.signup.OnRecoveryEmailWarningListener
import com.email.scenes.signup.holders.SignUpFormHolder
import com.email.utils.UIMessage
import com.email.utils.getLocalizedUIMessage

/**
 * Created by sebas on 2/15/18.
 */

interface SignUpScene {
    fun isPasswordErrorShown() : Boolean
    fun isUsernameErrorShown() : Boolean
    fun isUserAvailable(userAvailable : Boolean)
    fun enableCreateAccountButton()
    fun disableCreateAccountButton()
    fun togglePasswordSuccess(show: Boolean)
    fun togglePasswordErrors(show: Boolean)
    fun toggleUsernameErrors(show: Boolean)
    fun showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener)
    fun initListeners(signUpListener: SignUpSceneController.SignUpListener)
    fun showError(message : UIMessage)
    fun showSuccess()
    fun showKeyGenerationHolder()
    fun showFormHolder()
    fun resetSceneWidgetsFromModel(
            username: String,
            fullName: String,
            password: String,
            recoveryEmail: String)

    var signUpListener: SignUpSceneController.SignUpListener?

    class SignUpSceneView(private val view: View): SignUpScene {

        private val viewGroup = view.parent as ViewGroup

        private var signUpFormHolder: SignUpFormHolder? = null
        private var keyGenerationHolder : KeyGenerationHolder? = null

        override var signUpListener : SignUpSceneController.SignUpListener? = null
            set(value) {
                signUpFormHolder?.signUpListener = value
                field = value
            }


        override fun showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener){
            signUpFormHolder?.showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener)
        }

        private fun showUsernameSucess() {
            signUpFormHolder?.showUsernameSucess()
        }

        private fun hideUsernameSucess() {
            signUpFormHolder?.hideUsernameSucess()
        }

        override fun togglePasswordSuccess(show: Boolean) {
            if(show) {
                signUpFormHolder?.showPasswordSucess()
            } else {
                signUpFormHolder?.hidePasswordSucess()
            }
        }

        override fun toggleUsernameErrors(show: Boolean) {
            if(show) {
                signUpFormHolder?.showUsernameErrors()
            } else {
                signUpFormHolder?.hideUsernameErrors()
            }
        }

        override fun togglePasswordErrors(show: Boolean) {
            if(show) {
                signUpFormHolder?.showPasswordErrors()
            } else {
                signUpFormHolder?.hidePasswordErrors()
            }
        }

        override fun disableCreateAccountButton() {
            signUpFormHolder?.disableCreateAccountButton()
        }

        override fun enableCreateAccountButton() {
            signUpFormHolder?.enableCreateAccountButton()
        }

        override fun isPasswordErrorShown(): Boolean {
            return signUpFormHolder!!.isPasswordErrorShown()
        }


        override fun isUsernameErrorShown(): Boolean {
            return signUpFormHolder!!.isUsernameErrorShown()
        }

        private fun assignPasswordTextListener() {
            signUpFormHolder?.assignPasswordTextListener()
        }

        private fun assignConfirmPasswordTextChangeListener() {
            signUpFormHolder?.assignConfirmPasswordTextChangeListener()
        }

        private fun assignCheckTermsAndConditionsListener() {
            signUpFormHolder?.assignCheckTermsAndConditionsListener()
        }

        private fun assignUsernameTextChangeListener() {
            signUpFormHolder?.assignUsernameTextChangeListener()
        }

        private fun assignfullNameTextChangeListener() {
            signUpFormHolder?.assignfullNameTextChangeListener()
        }

        private fun assignRecoveryEmailTextChangeListener() {
            signUpFormHolder?.assignRecoveryEmailTextChangeListener()
        }

        private fun assignTermsAndConditionsClickListener() {
            signUpFormHolder?.assignTermsAndConditionsClickListener()
        }

        private fun assignBackButtonListener() {
            signUpFormHolder?.assignBackButtonListener()
        }

        override fun isUserAvailable(userAvailable: Boolean){
            if(userAvailable) {
                showUsernameSucess()
                toggleUsernameErrors(show = false)
            } else {
                hideUsernameSucess()
                toggleUsernameErrors(show = true)
            }
        }
        init {
        }

        override fun showFormHolder() {
            removeAllViews()
            val formLayout = View.inflate(
                    view.context,
                    R.layout.activity_form_signup, viewGroup)
            signUpFormHolder = SignUpFormHolder(formLayout)
        }

        private fun assignCreateAccountClickListener() {
            signUpFormHolder?.assignCreateAccountClickListener()
        }

        override fun initListeners(signUpListener: SignUpSceneController.SignUpListener){
            this.signUpListener = signUpListener

            signUpFormHolder?.signUpListener = signUpListener

            assignPasswordTextListener()
            assignConfirmPasswordTextChangeListener()
            assignUsernameTextChangeListener()
            assignCheckTermsAndConditionsListener()
            assignTermsAndConditionsClickListener()
            assignfullNameTextChangeListener()
            assignCreateAccountClickListener()
            assignRecoveryEmailTextChangeListener()
            assignBackButtonListener()

        }

        override fun showError(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    view.context,
                    view.context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
            keyGenerationHolder?.stopTimer()
            showFormHolder()
            initListeners(signUpListener!!)
        }

        override fun showKeyGenerationHolder() {
            removeAllViews()
            val keyGenerationLayout = View.inflate(
                    view.context,
                    R.layout.view_key_generation, viewGroup)
            keyGenerationHolder = KeyGenerationHolder(
                    keyGenerationLayout, checkProgress, 10)
        }

        private val checkProgress = {
            progress: Int
            ->
            if(progress == 100) {
                keyGenerationHolder?.stopTimer()
            }
            Unit
        }
        override fun showSuccess() {
            keyGenerationHolder?.updateProgress(99)
            keyGenerationHolder?.stopTimer()

            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    view.context,
                    "Success",
                    duration)
            toast.show()
        }

        private fun removeAllViews() {
            viewGroup.removeAllViews()
        }

        override fun resetSceneWidgetsFromModel(
                username: String,
                fullName: String,
                password: String,
                recoveryEmail: String) {
            signUpFormHolder?.fillSceneWidgets(
                    username = username,
                    fullName = fullName,
                    password = password,
                    recoveryEmail = recoveryEmail)
        }
    }

}
