package com.criptext.mail.scenes.settings.devices.data

import com.criptext.mail.scenes.settings.devices.DevicesModel
import com.criptext.mail.utils.virtuallist.VirtualListView

class DeviceWrapperListController(
        private val model: DevicesModel,
        private val listView: VirtualListView?){

    fun remove(position: Int) {
        model.devices.removeAt(position)
        listView?.notifyDataSetChanged()
    }

    fun update() {
        listView?.notifyDataSetChanged()
    }

}