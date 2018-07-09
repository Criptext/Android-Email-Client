package com.email.scenes.settings.labels

import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.scenes.settings.SettingsModel
import com.email.utils.virtuallist.VirtualListView

class LabelWrapperListController(
        private val model: SettingsModel,
        private val listView: VirtualListView?){

    fun addNew(newLabel: LabelWrapper) {
        model.labels.add(newLabel)
        listView?.notifyDataSetChanged()
    }

}