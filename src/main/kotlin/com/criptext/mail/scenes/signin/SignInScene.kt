package com.criptext.mail.scenes.signin

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.scenes.signin.data.UserData
import com.criptext.mail.scenes.signin.holders.*
import com.criptext.mail.scenes.signup.holders.KeyGenerationHolder
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ForgotPasswordDialog
import com.criptext.mail.utils.ui.GeneralDialogWithInput
import com.criptext.mail.utils.ui.GeneralMessageOkDialog
import com.criptext.mail.utils.ui.RetrySyncAlertDialogNewDevice
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.validation.ProgressButtonState

/**
 * Created by sebas on 2/15/18.
 */

interface SignInScene {
    fun resetInput()
    fun showError(message: UIMessage)
    fun drawInputError(error: UIMessage?)
    fun drawSuccess()
    fun initLayout(model: SignInSceneModel, signInUIObserver: SignInSceneController.SignInUIObserver,
                   devicesListItemListener: DevicesListItemListener? = null)
    fun showResetPasswordDialog(emailAddress: String)
    fun showPasswordLoginDialog(
            onPasswordLoginDialogListener: OnPasswordLoginDialogListener)
    fun setSubmitButtonState(state: ProgressButtonState)
    fun showKeyGenerationHolder()
    fun toggleForgotPasswordClickable(isEnabled: Boolean)
    fun toggleResendClickable(isEnabled: Boolean)
    fun startLinkSucceedAnimation()
    fun setLinkProgress(message: UIMessage, progress: Int)
    fun showSyncRetryDialog(result: SignInResult)
    fun showSignInWarningDialog(oldAccountName: String, newUserData: UserData)
    fun showPasswordDialogError(message: UIMessage?)
    fun toggleChangePasswordButton(enable: Boolean)
    fun showDeviceCountRemaining(remaining: Int)
    fun showDeviceRemovalError()
    fun showGenericOkAlert(message: UIMessage)

    var signInUIObserver: SignInSceneController.SignInUIObserver?

    class SignInSceneView(val view: View): SignInScene {

        private val viewGroup = view.parent as ViewGroup
        private var holder: BaseSignInHolder
        private val retrySyncDialog = RetrySyncAlertDialogNewDevice(view.context)
        private val signInWarningDialog = SignInWarningDialog(view.context)
        private val deviceCountRemainingDialog = GeneralMessageOkDialog(view.context,
                DialogData.DialogMessageData(
                        title = UIMessage(R.string.sign_in_remove_dialog_title),
                        message = listOf(UIMessage(R.string.sign_in_remove_success_remaining)),
                        type = DialogType.Message()
                )
        )

        private val deviceExpirationWarning = GeneralMessageOkDialog(view.context,
                DialogData.DialogMessageData(
                        title = UIMessage(R.string.sign_in_remove_error_dialog_title),
                        message = listOf(UIMessage(R.string.sign_in_remove_error_dialog_message)),
                        type = DialogType.Message(),
                        onOkPress = { signInUIObserver?.onBackPressed() }
                )
        )

        override var signInUIObserver: SignInSceneController.SignInUIObserver? = null
            set(value) {
                holder.uiObserver = value
                field = value
            }

        override fun showError(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    view.context,
                    view.context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

        override fun drawSuccess() {
        }

        init {
            val signInLayout = View.inflate(
                    view.context,
                    R.layout.activity_start, viewGroup)
            holder = SignInStartHolder(signInLayout,  true, false)
        }


        override fun initLayout(model: SignInSceneModel,
                                signInUIObserver: SignInSceneController.SignInUIObserver,
                                devicesListItemListener: DevicesListItemListener?) {
            removeAllViews()
            val state = model.state
            holder = when (state) {
                is SignInLayoutState.Start -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.activity_start, viewGroup)
                    SignInStartHolder(newLayout, state.firstTime, model.isMultiple)
                }
                is SignInLayoutState.Login -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.activity_signin_form, viewGroup)
                    LoginHolder(newLayout, state, state.firstTime, model.isMultiple)
                }
                is SignInLayoutState.LoginValidation -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sign_in_two_fa, viewGroup)
                    LoginValidationHolder(newLayout, state)
                }
                is SignInLayoutState.ForgotPassword -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.holder_sign_in_reset_password, viewGroup)
                    ForgotPasswordHolder(newLayout)
                }
                is SignInLayoutState.Connection -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.activity_connection, viewGroup)
                    ConnectionHolder(newLayout, state.username, state.domain, state.authorizerType,signInUIObserver)
                }
                is SignInLayoutState.ChangePassword -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.activity_change_password_login, viewGroup)
                    ChangePasswordLoginHolder(newLayout, state)
                }
                is SignInLayoutState.DeniedValidation -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.activity_denied_validation, viewGroup)
                    DeniedValidationHolder(newLayout)
                }
                is SignInLayoutState.RemoveDevices -> {
                    val newLayout = View.inflate(
                            view.context,
                            R.layout.activity_sign_in_remove_devices, viewGroup)
                    RemoveDevicesHolder(newLayout, model, devicesListItemListener)
                }
            }
            this.signInUIObserver = signInUIObserver
            finishHolderSetup()
        }

        private fun finishHolderSetup(){
            when(holder){
                is RemoveDevicesHolder -> {
                    signInUIObserver?.onSetupDevices((holder as RemoveDevicesHolder).deviceListView)
                }
            }
        }


        private fun removeAllViews() {
            viewGroup.removeAllViews()
            holder.uiObserver = null
        }

        private val showMailboxScene = {
            signInUIObserver: SignInSceneController.SignInUIObserver ->
            signInUIObserver.userLoginReady()
        }

        override fun resetInput() {
            val currentHolder = holder
            when (currentHolder) {
                is LoginHolder -> currentHolder.resetInput()
            }
        }

        override fun showSyncRetryDialog(result: SignInResult) {
            retrySyncDialog.showLinkDeviceAuthDialog(signInUIObserver, result)
        }

        override fun showSignInWarningDialog(oldAccountName: String, newUserData: UserData) {
            signInWarningDialog.showDialog(holder.uiObserver, oldAccountName, newUserData)
        }

        override fun setSubmitButtonState(state: ProgressButtonState) {
            when (val currentHolder = holder) {
                is LoginHolder -> currentHolder.setSubmitButtonState(state)
                is ChangePasswordLoginHolder -> currentHolder.setSubmitButtonState(state)
                is LoginValidationHolder -> currentHolder.setSubmitButtonState(state)
                is RemoveDevicesHolder -> currentHolder.setSubmitButtonState(state)
                is ForgotPasswordHolder -> currentHolder.setSubmitButtonState(state)
            }
        }

        override fun showPasswordLoginDialog(onPasswordLoginDialogListener: OnPasswordLoginDialogListener) {
            (holder as LoginValidationHolder).showPasswordLoginDialog(onPasswordLoginDialogListener)
        }

        override fun showResetPasswordDialog(emailAddress: String) {
            ForgotPasswordDialog(view.context, emailAddress).showForgotPasswordDialog()
        }

        override fun drawInputError(error: UIMessage?) {
            val currentHolder = holder
            when (currentHolder) {
                is LoginHolder -> currentHolder.drawError(error)
                is ForgotPasswordHolder -> currentHolder.drawError(error)
                is LoginValidationHolder -> currentHolder.drawError(error)
            }
        }

        override fun toggleForgotPasswordClickable(isEnabled: Boolean) {
            val currentHolder = holder as ForgotPasswordHolder
            currentHolder.toggleForgotPasswordClickable(isEnabled)
        }

        override fun toggleResendClickable(isEnabled: Boolean) {
            val currentHolder = holder as LoginValidationHolder
            currentHolder.setEnableButtons(isEnabled)
        }

        override fun startLinkSucceedAnimation() {
            val currentHolder = holder as? ConnectionHolder
            currentHolder?.startSucceedAnimation(showMailboxScene)
        }

        override fun setLinkProgress(message: UIMessage, progress: Int) {
            val currentHolder = holder as? ConnectionHolder
            currentHolder?.setProgress(message, progress)
        }

        override fun showPasswordDialogError(message: UIMessage?) {
            val currentHolder = holder as ChangePasswordLoginHolder
            currentHolder.showPasswordDialogError(message)
        }

        override fun toggleChangePasswordButton(enable: Boolean) {
            val currentHolder = holder as ChangePasswordLoginHolder
            currentHolder.toggleChangePasswordButton(enable)
        }

        override fun showDeviceCountRemaining(remaining: Int) {
            deviceCountRemainingDialog.showDialog(listOf(UIMessage(R.string.sign_in_remove_success_remaining, arrayOf(remaining))))
        }

        override fun showDeviceRemovalError() {
            deviceExpirationWarning.showDialog()
        }

        override fun showGenericOkAlert(message: UIMessage) {
            GeneralMessageOkDialog(view.context,
                    DialogData.DialogMessageData(
                            title = UIMessage(R.string.signed_out_title),
                            message = listOf(message),
                            type = DialogType.Message()
                    )
            ).showDialog()
        }

        override fun showKeyGenerationHolder() {
            viewGroup.removeAllViews()
            val keyGenerationLayout = View.inflate(
                    view.context,
                    R.layout.view_key_generation, viewGroup)
            KeyGenerationHolder(keyGenerationLayout, {
                if(it >= 100){
                    holder.uiObserver?.onProgressHolderFinish()
                }
            }, 50)
        }

    }
}

