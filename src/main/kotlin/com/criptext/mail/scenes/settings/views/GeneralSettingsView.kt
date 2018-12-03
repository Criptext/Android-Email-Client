package com.criptext.mail.scenes.settings.views

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.SettingsUIObserver
import com.criptext.mail.utils.ui.TabView

class GeneralSettingsView(view: View, title: String): TabView(view, title) {

    private lateinit var settingsProfileName: View
    private lateinit var settingsSignature: View
    private lateinit var settingsPrivacyPolicies: View
    private lateinit var settingsTermsOfService: View
    private lateinit var settingsOpenSourceLibraries: View
    private lateinit var settingsLogout: View
    private lateinit var settingsDeleteAccount: View
    private lateinit var settingsChangePassword: View
    private lateinit var settingsRecoveryEmail: View
    private lateinit var settingsRecoveryEmailLoading: View
    private lateinit var settingsSyncPhonebookContacts: View
    private lateinit var settingsPin: View
    private lateinit var settingsRecoveryEmailConfirmText: TextView
    private lateinit var settingsSyncPhonebookProgress: ProgressBar
    private lateinit var versionText: TextView
    private lateinit var twoFASwitch: Switch
    private lateinit var readReceiptsSwitch: Switch
    private lateinit var emailPreview: Switch

    private var settingsUIObserver: SettingsUIObserver? = null

    private val versionString ="Criptext System Version ${BuildConfig.VERSION_NAME}"

    override fun onCreateView(){

        settingsProfileName = view.findViewById(R.id.settings_profile_name)
        settingsSignature = view.findViewById(R.id.settings_signature)
        settingsPrivacyPolicies = view.findViewById(R.id.settings_privacy_policy)
        settingsTermsOfService = view.findViewById(R.id.settings_terms_of_service)
        settingsOpenSourceLibraries = view.findViewById(R.id.settings_open_source_libraries)
        settingsLogout = view.findViewById(R.id.settings_logout)
        settingsDeleteAccount = view.findViewById(R.id.settings_delete_account)
        settingsChangePassword = view.findViewById(R.id.settings_change_password)
        settingsPin = view.findViewById(R.id.settings_pin_lock)
        settingsRecoveryEmail = view.findViewById(R.id.settings_recovery)
        settingsSyncPhonebookContacts = view.findViewById(R.id.settings_sync_contacts)
        settingsRecoveryEmailLoading = view.findViewById(R.id.settings_recovery_loading)
        settingsRecoveryEmailConfirmText = view.findViewById(R.id.not_confirmed_text) as TextView
        settingsSyncPhonebookProgress = view.findViewById(R.id.sync_phonebook_progress) as ProgressBar
        versionText = view.findViewById(R.id.version_text) as TextView
        versionText.text = BuildConfig.VERSION_NAME
        twoFASwitch = view.findViewById(R.id.switch_two_fa)
        readReceiptsSwitch = view.findViewById(R.id.switch_read_receipts)
        emailPreview = view.findViewById(R.id.switch_preview)

        settingsRecoveryEmail.visibility = View.GONE
        settingsRecoveryEmailLoading.visibility = View.VISIBLE

        setListeners()
    }

    fun setExternalListeners(settingsUIObserver: SettingsUIObserver?){
        this.settingsUIObserver = settingsUIObserver
    }

    fun setRecoveryEmailConfirmationText(isConfirmed: Boolean){
        settingsRecoveryEmail.visibility = View.VISIBLE
        settingsRecoveryEmailLoading.visibility = View.GONE

        if(isConfirmed) {
            settingsRecoveryEmailConfirmText.setTextColor(ContextCompat.getColor(
                    view.context, R.color.green))
            settingsRecoveryEmailConfirmText.setText(R.string.status_confirmed)
        }else{
            settingsRecoveryEmailConfirmText.setTextColor(ContextCompat.getColor(
                    view.context, R.color.red))
            settingsRecoveryEmailConfirmText.setText(R.string.status_not_confirmed)
        }
    }

    fun set2FA(has2FA: Boolean){
        twoFASwitch.setOnCheckedChangeListener { _, _ ->  }
        twoFASwitch.isChecked = has2FA
        setSwitchListener()
    }

    fun enable2FASwitch(isEnabled: Boolean){
        twoFASwitch.isEnabled = isEnabled
    }

    fun setReadReceipts(hasReadReceipts: Boolean){
        readReceiptsSwitch.setOnCheckedChangeListener { _, _ ->  }
        readReceiptsSwitch.isChecked = hasReadReceipts
        setSwitchListener()
    }

    fun enableReadReceiptsSwitch(isEnabled: Boolean){
        readReceiptsSwitch.isEnabled = isEnabled
    }

    fun setEmailPreview(showPreview: Boolean){
        emailPreview.setOnCheckedChangeListener { _, _ ->  }
        emailPreview.isEnabled = true
        emailPreview.isChecked = showPreview
        setSwitchListener()
    }

    fun setSyncContactsProgressVisisble(isVisible: Boolean){
        if(isVisible) settingsSyncPhonebookProgress.visibility = View.VISIBLE
        else settingsSyncPhonebookProgress.visibility = View.INVISIBLE
    }

    private fun setListeners(){
        settingsProfileName.setOnClickListener {
            settingsUIObserver?.onProfileNameClicked()
        }
        settingsChangePassword.setOnClickListener {
            settingsUIObserver?.onChangePasswordOptionClicked()
        }
        settingsRecoveryEmail.setOnClickListener {
            settingsUIObserver?.onRecoveryEmailOptionClicked()
        }
        settingsSignature.setOnClickListener {
            settingsUIObserver?.onSignatureOptionClicked()
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
        settingsLogout.setOnClickListener {
            settingsUIObserver?.onLogoutClicked()
        }
        settingsDeleteAccount.setOnClickListener {
            settingsUIObserver?.onDeleteAccountClicked()
        }
        settingsSyncPhonebookContacts.setOnClickListener {
            settingsUIObserver?.onSyncPhonebookContacts()
        }
        settingsPin.setOnClickListener {
            settingsUIObserver?.onPinLockClicked()
        }
        setSwitchListener()
    }

    private fun setSwitchListener(){
        twoFASwitch.setOnCheckedChangeListener {_, isChecked ->
            settingsUIObserver?.onTwoFASwitched(isChecked)
        }

        readReceiptsSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsUIObserver?.onReadReceiptsSwitched(isChecked)
        }

        emailPreview.setOnCheckedChangeListener {_, isChecked ->
            settingsUIObserver?.onEmailPreviewSwitched(isChecked)
        }
    }

}