package com.email.scenes.signup

import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.email.R
import com.email.scenes.signup.holders.KeyGenerationHolder
import com.email.scenes.signup.holders.SignUpFormHolder
import com.email.validation.FormInputState
import com.email.utils.UIMessage
import com.email.validation.TextInput
import com.email.utils.getLocalizedUIMessage

/**
 * Created by sebas on 2/15/18.
 */

interface SignUpScene {
    fun isPasswordErrorShown() : Boolean
    fun isUsernameErrorShown() : Boolean

    /**
     * Show or hide the check mark that informs the user that the username is available.
     * @param visible if true, show the check mark otherwise hide it.
     */
    fun toggleUserAvailableCheckmark(visible: Boolean)
    fun enableCreateAccountButton()
    fun disableCreateAccountButton()
    fun togglePasswordSuccess(show: Boolean)
    /**
     * Set the error message right next to the passwordValue input field.
     * @param message The message to display. Any existing message is replaced. If this value
     * is null, any existing message is removed. If this value is not null, then the create button
     * is disabled.
     */
    fun setPasswordError(message: UIMessage?)
    /**
     * Displays the username input field as valid, error or unknown depending on the passed state
     * value.
     */
    fun setUsernameState(state: FormInputState)
    /**
     * Displays the full name input field as valid, error or unknown depending on the passed state
     * value.
     */
    fun setFullNameState(state: FormInputState)
    /**
     * Displays the full name input field as valid, error or unknown depending on the passed state
     * value.
     */
    fun setRecoveryEmailState(state: FormInputState)
    fun showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener)
    fun initListeners(uiObserver: SignUpSceneController.SignUpUIObserver)
    fun showError(message : UIMessage)
    fun showSuccess()
    fun showKeyGenerationHolder()
    fun showFormHolder()
    fun resetSceneWidgetsFromModel(
            username: TextInput,
            fullName: TextInput,
            password: String,
            confirmPassword: String,
            recoveryEmail: TextInput,
            isChecked: Boolean)

    var uiObserver: SignUpSceneController.SignUpUIObserver?

    class SignUpSceneView(private val view: View): SignUpScene {

        private val viewGroup = view.parent as ViewGroup

        private var signUpFormHolder: SignUpFormHolder? = null
        private var keyGenerationHolder : KeyGenerationHolder? = null

        override var uiObserver: SignUpSceneController.SignUpUIObserver? = null
            set(value) {
                signUpFormHolder?.uiObserver = value
                field = value
            }


        override fun showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener){
            signUpFormHolder?.showRecoveryEmailWarningDialog(onRecoveryEmailWarningListener)
        }

        override fun togglePasswordSuccess(show: Boolean) {
            if(show) {
                signUpFormHolder?.showPasswordSuccess()
            } else {
                signUpFormHolder?.hidePasswordSuccess()
            }
        }

        override fun setUsernameState(state: FormInputState) {
            signUpFormHolder?.setUsernameState(state)
        }

        override fun setPasswordError(message: UIMessage?) {
            signUpFormHolder?.setPasswordError(message)
        }

        override fun setFullNameState(state: FormInputState) {
            signUpFormHolder?.setFullNameState(state)
        }

        override fun setRecoveryEmailState(state: FormInputState) {
            signUpFormHolder?.setRecoveryEmailState(state)
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
            throw Exception("WTF are you doing?")
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

        override fun toggleUserAvailableCheckmark(visible: Boolean) {
            signUpFormHolder?.toggleUserAvailableCheckmark(visible)
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

        override fun initListeners(uiObserver: SignUpSceneController.SignUpUIObserver){
            this.uiObserver = uiObserver
            
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
            initListeners(uiObserver!!)
        }

        override fun showKeyGenerationHolder() {
            removeAllViews()
            val keyGenerationLayout = View.inflate(
                    view.context,
                    R.layout.view_key_generation, viewGroup)
            keyGenerationHolder = KeyGenerationHolder(
                    keyGenerationLayout, checkProgress, 100)
        }

        private val checkProgress = {
            progress: Int
            ->
            if(progress >= 100) {
                keyGenerationHolder?.stopTimer()
            }
            Unit
        }
        override fun showSuccess() {
            keyGenerationHolder?.updateProgress(100)
            keyGenerationHolder?.stopTimer()

            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    view.context,
                    "Success",
                    duration)
            toast.show()

            uiObserver?.onRegisterUserSuccess()
        }

        private fun removeAllViews() {
            signUpFormHolder?.uiObserver = null
            viewGroup.removeAllViews()
        }

        override fun resetSceneWidgetsFromModel(
                username: TextInput,
                fullName: TextInput,
                password: String,
                confirmPassword: String,
                recoveryEmail: TextInput,
                isChecked: Boolean) {
            signUpFormHolder?.fillSceneWidgets(
                    username = username,
                    fullName = fullName,
                    password = password,
                    confirmPassword = password,
                    recoveryEmail = recoveryEmail,
                    isChecked = isChecked)
        }
    }
}
