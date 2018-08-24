package com.criptext.mail.scenes.settings.devices

import com.criptext.mail.scenes.settings.SettingsModel
import com.criptext.mail.utils.virtuallist.VirtualListView

class DeviceWrapperListController(
        private val model: SettingsModel,
        private val listView: VirtualListView?){

    fun remove(position: Int) {
        model.devices.removeAt(position)
        listView?.notifyDataSetChanged()
    }

    fun update() {
        listView?.notifyDataSetChanged()
    }

}