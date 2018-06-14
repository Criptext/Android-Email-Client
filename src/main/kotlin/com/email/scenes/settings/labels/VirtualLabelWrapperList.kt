package com.email.scenes.settings.labels

import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.scenes.settings.SettingsModel
import com.email.utils.virtuallist.VirtualList

class VirtualLabelWrapperList(val model: SettingsModel): VirtualList<LabelWrapper>{

    override fun get(i: Int): LabelWrapper {
        return model.labels[i]
    }

    override val size: Int
        get() = model.labels.size

    override val hasReachedEnd = true

}