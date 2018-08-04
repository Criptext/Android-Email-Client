package com.criptext.mail.scenes.settings

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper

/**
 * Created by gabriel on 2/28/18.
 */

interface SettingsUIObserver {
    fun onBackButtonPressed()
    fun onSignatureOptionClicked()
    fun onProfileNameClicked()
    fun onLogoutClicked()
    fun onLogoutConfirmedClicked()
    fun onProfileNameChanged(fullName: String)
    fun onCustomLabelNameAdded(labelName: String)
    fun onCreateLabelClicked()
    fun onToggleLabelSelection(label: LabelWrapper)
}