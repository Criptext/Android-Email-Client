package com.criptext.mail.scenes.settings


import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.syncing.SyncBeginDialog
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver

interface SettingsScene{

    fun attachView(account: ActiveAccount, model: SettingsModel, settingsUIObserver: SettingsUIObserver)
    fun syncIsLoading(loading: Boolean)
    fun showMessage(message : UIMessage)
    fun showGeneralDialogWithInput(replyToEmail: String, dialogData: DialogData.DialogDataForReplyToEmail)
    fun showGeneralDialogConfirmation(dialogData: DialogData.DialogConfirmationData)
    fun toggleGeneralDialogWithInputLoad(isLoading: Boolean)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showTwoFADialog(hasRecoveryEmailConfirmed: Boolean)
    fun setSyncContactsProgressVisisble(isVisible: Boolean)
    fun getLabelLocalizedName(name: String): String
    fun dismissReplyToEmailDialog()
    fun clearThemeSwitchListener()
    fun setEmailPreview(isChecked: Boolean)
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()


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
        private val settingsCloudBackup: View by lazy {
            view.findViewById<View>(R.id.settings_cloud_backup)
        }
        private val settingsMailboxSync: View by lazy {
            view.findViewById<View>(R.id.settings_sync_mailbox)
        }
        private val settingsMailboxSyncLoad: View by lazy {
            view.findViewById<ProgressBar>(R.id.sync_mailbox_progress)
        }
        private val settingsBilling: View by lazy {
            view.findViewById<View>(R.id.settings_billing)
        }
        private val settingsAddressManager: View by lazy {
            view.findViewById<View>(R.id.settings_address_manager)
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
        private val settingsReportBug: View by lazy {
            view.findViewById<View>(R.id.settings_report_bug)
        }
        private val settingsReportAbuse: View by lazy {
            view.findViewById<View>(R.id.settings_report_abuse)
        }
        private val separatorAddresses: TextView by lazy {
            view.findViewById<TextView>(R.id.separator_addresses)
        }


        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private var generalDialogWithInput: GeneralDialogWithInput? = null
        private var generalDialogConfirmation: GeneralDialogConfirmation? = null

        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val twoFADialog = Settings2FADialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

        override var settingsUIObserver: SettingsUIObserver? = null

        override fun attachView(account: ActiveAccount, model: SettingsModel,
                                settingsUIObserver: SettingsUIObserver) {

            this.settingsUIObserver = settingsUIObserver

            settingsDarkTheme.setOnCheckedChangeListener { _, _ ->  }
            settingsDarkTheme.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

            versionText.text = BuildConfig.VERSION_NAME
            accountEmail.text = account.userEmail.toUpperCase()

            if(account.type == AccountTypes.ENTERPRISE){
                settingsBilling.visibility = View.GONE
                settingsAddressManager.visibility = View.GONE
                separatorAddresses.visibility = View.GONE
            }

            setListeners()
            setSwitchListener()

            backButton.setOnClickListener {
                settingsUIObserver.onBackButtonPressed()
            }
        }

        override fun syncIsLoading(loading: Boolean) {
            settingsMailboxSync.isEnabled = !loading
            settingsMailboxSyncLoad.visibility = if(loading) {
                View.VISIBLE
            } else {
                View.INVISIBLE
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

        override fun dismissLinkDeviceDialog() {
            linkAuthDialog.dismiss()
        }

        override fun dismissSyncDeviceDialog() {
            syncAuthDialog.dismiss()
        }

        override fun showTwoFADialog(hasRecoveryEmailConfirmed: Boolean) {
            twoFADialog.showLogoutDialog(hasRecoveryEmailConfirmed)
        }

        override fun setSyncContactsProgressVisisble(isVisible: Boolean) {
            if(isVisible) settingsSyncPhonebookProgress.visibility = View.VISIBLE
            else settingsSyncPhonebookProgress.visibility = View.INVISIBLE
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

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
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
                settingsUIObserver?.onSyncPhoneBookContacts()
            }
            settingsCloudBackup.setOnClickListener {
                settingsUIObserver?.onCloudBackupClicked()
            }
            settingsMailboxSync.setOnClickListener {
                settingsUIObserver?.onSyncMailbox()
            }
            settingsBilling.setOnClickListener {
                settingsUIObserver?.onBillingClicked()
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
            settingsReportBug.setOnClickListener {
                settingsUIObserver?.onReportBugClicked()
            }
            settingsReportAbuse.setOnClickListener {
                settingsUIObserver?.onReportAbuseClicked()
            }
            settingsAddressManager.setOnClickListener {
                settingsUIObserver?.onAddressManagerClicked()
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