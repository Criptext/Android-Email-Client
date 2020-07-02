package com.criptext.mail.scenes.settings

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/28/18.
 */

abstract class SettingsUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onResendDeviceLinkAuth()
    abstract fun onBackButtonPressed()
    abstract fun onAccountOptionClicked()
    abstract fun onFAQClicked()
    abstract fun onPrivacyPoliciesClicked()
    abstract fun onTermsOfServiceClicked()
    abstract fun onOpenSourceLibrariesClicked()
    abstract fun onPinLockClicked()
    abstract fun onPrivacyClicked()
    abstract fun onDevicesOptionClicked()
    abstract fun onLabelsOptionClicked()
    abstract fun onShowPreviewSwitched(isChecked: Boolean)
    abstract fun onDarkThemeSwitched(isChecked: Boolean)
    abstract fun onSyncPhoneBookContacts()
    abstract fun onCloudBackupClicked()
    abstract fun onSyncMailbox()
    abstract fun onBillingClicked()
    abstract fun onSyncMailboxCanceled()
    abstract fun onReportBugClicked()
    abstract fun onReportAbuseClicked()
    abstract fun onAddressManagerClicked()
}