package com.criptext.mail.scenes.settings.labels

import com.criptext.mail.IHostActivity
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class LabelsUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onCustomLabelNameAdded(labelName: String)
    abstract fun onCreateLabelClicked()
    abstract fun onToggleLabelSelection(label: LabelWrapper)
    abstract fun onDeleteLabelClicked(label: LabelWrapper)
    abstract fun onEditLabelClicked(label: LabelWrapper)
}