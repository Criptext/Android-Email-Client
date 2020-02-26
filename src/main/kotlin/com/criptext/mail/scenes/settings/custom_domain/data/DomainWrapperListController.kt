package com.criptext.mail.scenes.settings.custom_domain.data

import com.criptext.mail.scenes.settings.custom_domain.CustomDomainModel
import com.criptext.mail.scenes.settings.devices.DevicesModel
import com.criptext.mail.utils.virtuallist.VirtualListView

class DomainWrapperListController(
        private val model: CustomDomainModel,
        private val listView: VirtualListView?){

    fun remove(position: Int) {
        model.domains.removeAt(position)
        listView?.notifyDataSetChanged()
    }

    fun addAll(devices: List<DomainItem>) {
        model.domains.addAll(devices)
        listView?.notifyDataSetChanged()
    }

    fun update() {
        listView?.notifyDataSetChanged()
    }

}