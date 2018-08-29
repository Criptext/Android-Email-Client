package com.criptext.mail.scenes.settings.labels

import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.settings.SettingsModel
import com.criptext.mail.utils.virtuallist.VirtualListView

class LabelWrapperListController(
        private val model: SettingsModel,
        private val listView: VirtualListView?){

    fun addNew(newLabel: LabelWrapper) {
        model.labels.add(newLabel)
        listView?.notifyDataSetChanged()
    }

    fun notifyDataSetChange(){
        listView?.notifyDataSetChanged()
    }

}