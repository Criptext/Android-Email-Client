package com.criptext.mail.scenes.signin.data

import com.criptext.mail.scenes.signin.SignInSceneModel
import com.criptext.mail.utils.virtuallist.VirtualListView

class DeviceWrapperListController(
        private val model: SignInSceneModel,
        private val listView: VirtualListView?){

    fun remove(position: Int) {
        model.devices.removeAt(position)
        listView?.notifyDataSetChanged()
    }

    fun remove(deviceIds: List<Int>) {
        deviceIds.forEach { id ->
            val device = model.devices.find { it.id == id }
            model.devices.remove(device)
        }
        listView?.notifyDataSetChanged()
    }

    fun clearChecks(){
        model.devices.forEach { it.checked = false }
        listView?.notifyDataSetChanged()
    }

    fun update() {
        listView?.notifyDataSetChanged()
    }

    fun count(): Int
    {
        return model.devices.size
    }
}