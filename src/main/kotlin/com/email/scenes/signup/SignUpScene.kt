package com.email.scenes.signin

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.email.R
import com.email.scenes.keygeneration.KeyGenerationScene
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
    fun toggleUsernameError(userAvailable : Boolean)
    fun enableCreateAccountButton()
    fun disableCreateAccountButton()
    fun hidePasswordErrors()
    fun showPasswordSucess()
    fun hidePasswordSucess()
    fun showPasswordErrors()
    fun hideUsernameErrors()
    fun showUsernameErrors()
    fun showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener)
    fun initListeners(signUpListener: SignUpSceneController.SignUpListener)
    fun showError(message : UIMessage)
    fun showSuccess()
    fun launchKeyGenerationScene()
    fun showFormScene()

    var signUpListener: SignUpSceneController.SignUpListener?

    class SignUpSceneView(private val view: View): SignUpScene {

        private val res = view.context.resources
        private val viewGroup = view.parent as ViewGroup

        private var signUpFormHolder: SignUpFormHolder? = null
        private lateinit var formLayout : View
        private lateinit var keyGenerationLayout : View
        private lateinit var keyGenerationScene : KeyGenerationScene.KeyGenerationSceneView

        override var signUpListener : SignUpSceneController.SignUpListener? = null

        override fun showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener){
            signUpFormHolder?.showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener)
        }

        private fun showUsernameSucess() {
            signUpFormHolder?.showUsernameSucess()
        }

        private fun hideUsernameSucess() {
            signUpFormHolder?.hideUsernameSucess()
        }

        override fun showPasswordSucess() {
            signUpFormHolder?.showPasswordSucess()
        }

        override fun hidePasswordSucess() {
            signUpFormHolder?.hidePasswordSucess()
        }

        @SuppressLint("RestrictedApi")
        override fun hideUsernameErrors() {
            signUpFormHolder?.hideUsernameErrors()
        }

        @SuppressLint("RestrictedApi")
        override fun hidePasswordErrors() {
           signUpFormHolder?.hidePasswordErrors()
        }

        @SuppressLint("RestrictedApi")
        override fun showPasswordErrors() {
            signUpFormHolder?.showPasswordErrors()
        }

        @SuppressLint("RestrictedApi")
        override fun showUsernameErrors() {
            signUpFormHolder?.showUsernameErrors()
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

        override fun toggleUsernameError(userAvailable: Boolean){
            if(userAvailable) {
                showUsernameSucess()
                hideUsernameErrors()
            } else {
                hideUsernameSucess()
                showUsernameErrors()
            }
        }
        init {
        }

        override fun showFormScene() {
            removeAllViews()
            formLayout = View.inflate(
                    view.context,
                    R.layout.activity_form_signup, viewGroup)
            signUpFormHolder = SignUpFormHolder(formLayout)
        }

        fun assignCreateAccountClickListener() {
            signUpFormHolder?.assignCreateAccountClickListener()
        }

        override fun initListeners(signUpListener: SignUpSceneController.SignUpListener){
            this.signUpListener = signUpListener

            assignPasswordTextListener()
            assignConfirmPasswordTextChangeListener()
            assignUsernameTextChangeListener()
            assignCheckTermsAndConditionsListener()
            assignTermsAndConditionsClickListener()
            assignfullNameTextChangeListener()
            assignCreateAccountClickListener()
            assignRecoveryEmailTextChangeListener()
            assignBackButtonListener()

            if(signUpFormHolder != null) {
                signUpFormHolder!!.signUpListener = signUpListener
            }
        }

        override fun showError(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    view.context,
                    view.context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

        override fun launchKeyGenerationScene() {
            removeAllViews()
            keyGenerationLayout = View.inflate(
                    view.context,
                    R.layout.view_key_generation, viewGroup)
            keyGenerationScene = KeyGenerationScene.KeyGenerationSceneView(
                    keyGenerationLayout, checkProgress, 10)
        }

        private val checkProgress = {
            progress: Int
            ->
            if(progress == 100) {
                keyGenerationScene.stopTimer()
            }
            Unit
        }
        override fun showSuccess() {
        }

        private fun removeAllViews() {
            viewGroup.removeAllViews()
        }

    }
}
