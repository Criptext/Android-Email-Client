package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/28/18.
 */

interface SettingsUIObserver: UIObserver {
    fun onResendDeviceLinkAuth()
    fun onBackButtonPressed()
    fun onAccountOptionClicked()
    fun onFAQClicked()
    fun onPrivacyPoliciesClicked()
    fun onTermsOfServiceClicked()
    fun onOpenSourceLibrariesClicked()
    fun onPinLockClicked()
    fun onPrivacyClicked()
    fun onDevicesOptionClicked()
    fun onLabelsOptionClicked()
    fun onShowPreviewSwitched(isChecked: Boolean)
    fun onDarkThemeSwitched(isChecked: Boolean)
    fun onSyncPhonebookContacts()
    fun onCloudBackupClicked()
    fun onSyncMailbox()
    fun onSyncMailboxCanceled()
    fun onReportBugClicked()
    fun onReportAbuseClicked()
    fun onCustomDomainClicked()
    fun onAliasesClicked()
}