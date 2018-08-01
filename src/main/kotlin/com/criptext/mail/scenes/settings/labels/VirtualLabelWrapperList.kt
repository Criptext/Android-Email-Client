package com.criptext.mail.scenes.settings.labels

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.settings.SettingsModel
import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualLabelWrapperList(val model: SettingsModel): VirtualList<LabelWrapper>{

    override fun get(i: Int): LabelWrapper {
        return model.labels[i]
    }

    override val size: Int
        get() = model.labels.size

    override val hasReachedEnd = true

}