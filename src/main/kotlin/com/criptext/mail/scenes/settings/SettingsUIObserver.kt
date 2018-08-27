package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper

/**
 * Created by gabriel on 2/28/18.
 */

interface SettingsUIObserver {
    fun onBackButtonPressed()
    fun onSignatureOptionClicked()
    fun onProfileNameClicked()
    fun onPrivacyPoliciesClicked()
    fun onTermsOfServiceClicked()
    fun onOpenSourceLibrariesClicked()
    fun onLogoutClicked()
    fun onLogoutConfirmedClicked()
    fun onRemoveDeviceConfirmed(deviceId: Int, position: Int)
    fun onRemoveDevice(deviceId: Int, position: Int)
    fun onChangePasswordOptionClicked()
    fun onRecoveryEmailOptionClicked()
    fun onOldPasswordChangedListener(password: String)
    fun onPasswordChangedListener(password: String)
    fun onConfirmPasswordChangedListener(confirmPassword: String)
    fun onOkChangePasswordDialogButton()
    fun onProfileNameChanged(fullName: String)
    fun onCustomLabelNameAdded(labelName: String)
    fun onCreateLabelClicked()
    fun onToggleLabelSelection(label: LabelWrapper)
}