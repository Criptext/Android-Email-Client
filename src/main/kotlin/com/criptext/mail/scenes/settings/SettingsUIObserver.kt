package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/28/18.
 */

interface SettingsUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onSignatureOptionClicked()
    fun onProfileNameClicked()
    fun onPrivacyPoliciesClicked()
    fun onTermsOfServiceClicked()
    fun onOpenSourceLibrariesClicked()
    fun onLogoutClicked()
    fun onLogoutConfirmedClicked()
    fun onRemoveDeviceConfirmed(deviceId: Int, position: Int, password: String)
    fun onRemoveDeviceCancel()
    fun onRemoveDevice(deviceId: Int, position: Int)
    fun onChangePasswordOptionClicked()
    fun onResetPasswordOptionClicked()
    fun onRecoveryEmailOptionClicked()
    fun onOldPasswordChangedListener(password: String)
    fun onPasswordChangedListener(password: String)
    fun onConfirmPasswordChangedListener(confirmPassword: String)
    fun onOkChangePasswordDialogButton()
    fun onCancelChangePasswordButton()
    fun onProfileNameChanged(fullName: String)
    fun onCustomLabelNameAdded(labelName: String)
    fun onCreateLabelClicked()
    fun onToggleLabelSelection(label: LabelWrapper)
}