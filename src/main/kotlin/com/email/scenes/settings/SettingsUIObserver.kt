package com.email.scenes.settings

import com.email.scenes.label_chooser.data.LabelWrapper

/**
 * Created by gabriel on 2/28/18.
 */

interface SettingsUIObserver {
    fun onBackButtonPressed()
    fun onSignatureOptionClicked()
    fun onProfileNameClicked()
    fun onProfileNameChanged(fullName: String)
    fun onCreateLabelClicked()
    fun onToggleLabelSelection(label: LabelWrapper)
}