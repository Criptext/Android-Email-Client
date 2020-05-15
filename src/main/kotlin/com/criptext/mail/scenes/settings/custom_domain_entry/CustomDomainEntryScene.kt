package com.criptext.mail.scenes.settings.custom_domain_entry

import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.widget.AppCompatEditText
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.recovery_email.holders.FormInputViewHolder
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.validation.FormInputState


interface CustomDomainEntryScene{

    fun attachView(customDomainEntryUIObserver: CustomDomainEntryUIObserver,
                   keyboardManager: KeyboardManager, model: CustomDomainEntryModel)
    fun showMessage(message: UIMessage)
    fun setNewDomainState(state: FormInputState)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()
    fun enableNextButton(enable: Boolean)
    fun progressNextButton(isInProgress: Boolean)
    fun setDomainError(message: UIMessage)

    class Default(val view: View): CustomDomainEntryScene{
        private lateinit var uiObserver: CustomDomainEntryUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val hintButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.info)
        }

        private val nextButton: Button by lazy {
            view.findViewById<Button>(R.id.next)
        }

        private val nextButtonProgress: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.next_progress_button)
        }

        private val textViewNewDomain: AppCompatEditText by lazy {
            view.findViewById<AppCompatEditText>(R.id.input)
        }

        private val textViewNewDomainInput: FormInputViewHolder by lazy {
            FormInputViewHolder(
                    textInputLayout = view.findViewById(R.id.input_layout),
                    editText = textViewNewDomain,
                    validView = null,
                    errorView = null,
                    disableSubmitButton = { -> nextButton.isEnabled = false })
        }

        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

        override fun attachView(customDomainEntryUIObserver: CustomDomainEntryUIObserver, keyboardManager: KeyboardManager,
                                model: CustomDomainEntryModel) {
            this.uiObserver = customDomainEntryUIObserver

            backButton.setOnClickListener {
                this.uiObserver.onBackButtonPressed()
            }

            hintButton.setOnClickListener {
                GeneralMessageOkDialog(
                        context = view.context,
                        dialogData = DialogData.DialogMessageData(
                                title = UIMessage(R.string.title_custom_domain),
                                type = DialogType.Message(),
                                message = listOf(UIMessage(R.string.custom_domain_help)),
                                onOkPress = {}
                        )
                ).showDialog()
            }

            nextButton.setOnClickListener {
                this.uiObserver.onNextButtonPressed()
            }

            domainTextListener()
        }

        private fun domainTextListener() {
            textViewNewDomain.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    uiObserver.onDomainTextChanged(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        override fun setDomainError(message: UIMessage) {
            textViewNewDomainInput.setState(FormInputState.Error(message))
        }

        override fun setNewDomainState(state: FormInputState) {
            textViewNewDomainInput.setState(state)
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(uiObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(uiObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(uiObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(uiObserver, trustedDeviceInfo)
        }

        override fun enableNextButton(enable: Boolean) {
            nextButton.isEnabled = enable
        }

        override fun progressNextButton(isInProgress: Boolean) {
            if(isInProgress){
                nextButtonProgress.visibility = View.VISIBLE
                nextButton.visibility = View.GONE
            } else {
                nextButtonProgress.visibility = View.GONE
                nextButton.visibility = View.VISIBLE
            }
        }

        override fun dismissLinkDeviceDialog() {
            linkAuthDialog.dismiss()
        }

        override fun dismissSyncDeviceDialog() {
            syncAuthDialog.dismiss()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPassword.showDialog(observer)
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPassword.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPassword.setPasswordError(message)
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