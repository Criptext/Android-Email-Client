package com.criptext.mail.scenes.settings.labels

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.uiobserver.UIObserver

interface LabelsUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onCustomLabelNameAdded(labelName: String)
    fun onCreateLabelClicked()
    fun onToggleLabelSelection(label: LabelWrapper)
    fun onDeleteLabelClicked(label: LabelWrapper)
    fun onEditLabelClicked(label: LabelWrapper)
}