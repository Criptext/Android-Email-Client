package com.criptext.mail.scenes.settings.labels.data

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.settings.SettingsModel
import com.criptext.mail.scenes.settings.labels.LabelsModel
import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualLabelWrapperList(val model: LabelsModel): VirtualList<LabelWrapper>{

    override fun get(i: Int): LabelWrapper {
        return model.labels[i]
    }

    override val size: Int
        get() = model.labels.size

    override val hasReachedEnd = true

}