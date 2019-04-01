package com.criptext.mail.scenes.settings


import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.syncing.SyncBeginDialog
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.uiobserver.UIObserver

interface SettingsScene{

    fun attachView(email: String, model: SettingsModel, settingsUIObserver: SettingsUIObserver)
    fun showMessage(message : UIMessage)
    fun showGeneralDialogWithInput(replyToEmail: String, dialogData: DialogData.DialogDataForReplyToEmail)
    fun showGeneralDialogConfirmation(dialogData: DialogData.DialogConfirmationData)
    fun showSyncBeginDialog()
    fun toggleGeneralDialogWithInputLoad(isLoading: Boolean)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun updateUserSettings(model: SettingsModel)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun showTwoFADialog(hasRecoveryEmailConfirmed: Boolean)
    fun setSyncContactsProgressVisisble(isVisible: Boolean)
    fun getLabelLocalizedName(name: String): String
    fun enableSyncBeginResendButton()
    fun dismissSyncBeginDialog()
    fun dismissReplyToEmailDialog()
    fun syncBeginDialogDenied()
    fun clearThemeSwitchListener()
    fun setEmailPreview(isChecked: Boolean)


    var settingsUIObserver: SettingsUIObserver?

    class Default(private val view: View): SettingsScene {

        private val context = view.context

        private val accountEmail: TextView by lazy {
            view.findViewById<TextView>(R.id.account_email)
        }
        private val settingsAccount: View by lazy {
            view.findViewById<View>(R.id.settings_account)
        }
        private val settingsPrivacy: View by lazy {
            view.findViewById<View>(R.id.settings_privacy)
        }
        private val settingsDevices: View by lazy {
            view.findViewById<View>(R.id.settings_devices)
        }
        private val settingsLabels: View by lazy {
            view.findViewById<View>(R.id.settings_labels)
        }
        private val settingsMailboxSync: View by lazy {
            view.findViewById<View>(R.id.settings_sync_mailbox)
        }
        private val settingsDarkTheme: Switch by lazy {
            view.findViewById<Switch>(R.id.switch_dark_theme)
        }
        private val settingsSyncPhonebookContacts: View by lazy {
            view.findViewById<View>(R.id.settings_sync_contacts)
        }
        private val settingsSyncPhonebookProgress: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.sync_phonebook_progress)
        }
        private val settingsShowPreview: Switch by lazy {
            view.findViewById<Switch>(R.id.switch_preview)
        }
        private val settingsPinLock: View by lazy {
            view.findViewById<View>(R.id.settings_pin_lock)
        }
        private val settingsFAQ: View by lazy {
            view.findViewById<View>(R.id.settings_faq)
        }
        private val settingsPrivacyPolicies: View by lazy {
            view.findViewById<View>(R.id.settings_privacy_policy)
        }
        private val settingsTermsOfService: View by lazy {
            view.findViewById<View>(R.id.settings_terms_of_service)
        }
        private val settingsOpenSourceLibraries: View by lazy {
            view.findViewById<View>(R.id.settings_open_source_libraries)
        }
        private val versionText: TextView by lazy {
            view.findViewById<TextView>(R.id.version_text)
        }


        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private var generalDialogWithInput: GeneralDialogWithInput? = null
        private var generalDialogConfirmation: GeneralDialogConfirmation? = null

        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val twoFADialog = Settings2FADialog(context)
        private val syncBeginDialog = SyncBeginDialog(context, UIMessage(R.string.title_sync))
        private val syncAuthDialog = SyncDeviceAlertDialog(context)

        override var settingsUIObserver: SettingsUIObserver? = null

        override fun attachView(email: String, model: SettingsModel,
                                settingsUIObserver: SettingsUIObserver) {

            this.settingsUIObserver = settingsUIObserver

            settingsDarkTheme.setOnCheckedChangeListener { _, _ ->  }
            settingsDarkTheme.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

            versionText.text = BuildConfig.VERSION_NAME
            accountEmail.text = email.toUpperCase()

            setListeners()
            setSwitchListener()

            backButton.setOnClickListener {
                settingsUIObserver.onBackButtonPressed()
            }
        }

        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }

        override fun showGeneralDialogWithInput(replyToEmail: String, dialogData: DialogData.DialogDataForReplyToEmail) {
            generalDialogWithInput = GeneralDialogWithInput(context, dialogData)
            generalDialogWithInput?.showDialog(settingsUIObserver)
        }

        override fun showGeneralDialogConfirmation(dialogData: DialogData.DialogConfirmationData) {
            generalDialogConfirmation = GeneralDialogConfirmation(context, dialogData)
            generalDialogConfirmation?.showDialog(settingsUIObserver)
        }

        override fun toggleGeneralDialogWithInputLoad(isLoading: Boolean) {
            generalDialogWithInput?.toggleLoad(isLoading)
        }

        override fun dismissReplyToEmailDialog() {
            generalDialogWithInput?.dismiss()
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

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(settingsUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(settingsUIObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(settingsUIObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(settingsUIObserver, trustedDeviceInfo)
        }

        override fun showTwoFADialog(hasRecoveryEmailConfirmed: Boolean) {
            twoFADialog.showLogoutDialog(hasRecoveryEmailConfirmed)
        }

        override fun showSyncBeginDialog() {
            syncBeginDialog.showDialog(settingsUIObserver)
        }

        override fun dismissSyncBeginDialog() {
            syncBeginDialog.dismiss()
        }

        override fun syncBeginDialogDenied() {
            syncBeginDialog.showFailedSync()
        }

        override fun enableSyncBeginResendButton() {
            syncBeginDialog.enableResendButton()
        }

        override fun setSyncContactsProgressVisisble(isVisible: Boolean) {
            if(isVisible) settingsSyncPhonebookProgress.visibility = View.VISIBLE
            else settingsSyncPhonebookProgress.visibility = View.INVISIBLE
        }

        override fun updateUserSettings(model: SettingsModel) {
            settingsDevices.isClickable = true
            settingsPrivacy.isClickable = true
        }

        override fun setEmailPreview(isChecked: Boolean) {
            settingsShowPreview.setOnCheckedChangeListener { _, _ ->  }
            settingsShowPreview.isEnabled = true
            settingsShowPreview.isChecked = isChecked
            setSwitchListener()
        }

        override fun clearThemeSwitchListener() {
            settingsDarkTheme.setOnCheckedChangeListener { _, _ ->  }
        }

        override fun getLabelLocalizedName(name: String): String {
            return context.getLocalizedUIMessage(
                    UIUtils.getLocalizedSystemLabelName(name)
            )
        }

        private fun setListeners(){
            settingsAccount.setOnClickListener {
                settingsUIObserver?.onAccountOptionClicked()
            }
            settingsFAQ.setOnClickListener {
                settingsUIObserver?.onFAQClicked()
            }
            settingsPrivacyPolicies.setOnClickListener {
                settingsUIObserver?.onPrivacyPoliciesClicked()
            }
            settingsTermsOfService.setOnClickListener {
                settingsUIObserver?.onTermsOfServiceClicked()
            }
            settingsOpenSourceLibraries.setOnClickListener {
                settingsUIObserver?.onOpenSourceLibrariesClicked()
            }
            settingsSyncPhonebookContacts.setOnClickListener {
                settingsUIObserver?.onSyncPhonebookContacts()
            }
            settingsMailboxSync.setOnClickListener {
                settingsUIObserver?.onSyncMailbox()
            }
            settingsPinLock.setOnClickListener {
                settingsUIObserver?.onPinLockClicked()
            }
            settingsPrivacy.setOnClickListener {
                settingsUIObserver?.onPrivacyClicked()
            }
            settingsDevices.setOnClickListener {
                settingsUIObserver?.onDevicesOptionClicked()
            }
            settingsLabels.setOnClickListener {
                settingsUIObserver?.onLabelsOptionClicked()
            }
            setSwitchListener()
        }

        private fun setSwitchListener(){
            settingsDarkTheme.setOnCheckedChangeListener {_, isChecked ->
                settingsUIObserver?.onDarkThemeSwitched(isChecked)
            }
            settingsShowPreview.setOnCheckedChangeListener { _, isChecked ->
                settingsUIObserver?.onShowPreviewSwitched(isChecked)
            }
        }
    }
}