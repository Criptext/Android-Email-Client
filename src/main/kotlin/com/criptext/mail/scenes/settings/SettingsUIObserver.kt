package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/28/18.
 */

interface SettingsUIObserver: UIObserver {
    fun onResendDeviceLinkAuth()
    fun onBackButtonPressed()
    fun onSignatureOptionClicked()
    fun onProfileNameClicked()
    fun onReplyToChangeClicked()
    fun onPrivacyPoliciesClicked()
    fun onTermsOfServiceClicked()
    fun onOpenSourceLibrariesClicked()
    fun onLogoutClicked()
    fun onDeleteAccountClicked()
    fun onPinLockClicked()
    fun onTwoFASwitched(isChecked: Boolean)
    fun onDarkThemeSwitched(isChecked: Boolean)
    fun onLogoutConfirmedClicked()
    fun onRemoveDeviceConfirmed(deviceId: Int, position: Int, password: String)
    fun onRemoveDeviceCancel()
    fun onRemoveDevice(deviceId: Int, position: Int)
    fun onChangePasswordOptionClicked()
    fun onRecoveryEmailOptionClicked()
    fun onProfileNameChanged(fullName: String)
    fun onCustomLabelNameAdded(labelName: String)
    fun onCreateLabelClicked()
    fun onToggleLabelSelection(label: LabelWrapper)
    fun onSyncPhonebookContacts()
    fun onSyncMailbox()
    fun onSyncMailboxCanceled()
}