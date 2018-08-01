package com.criptext.mail.scenes.settings.views

import android.support.v7.widget.RecyclerView
import android.view.View
import com.criptext.mail.scenes.settings.devices.DeviceAdapter
import com.criptext.mail.scenes.settings.devices.VirtualDeviceList
import com.criptext.mail.utils.ui.TabView
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView

class DevicesSettingsView(view: View, title: String): TabView(view, title){

    private lateinit var recyclerViewDevices: RecyclerView
    private lateinit var deviceListView: VirtualListView

    override fun onCreateView(){

        recyclerViewDevices = view as RecyclerView
        deviceListView = VirtualRecyclerView(recyclerViewDevices)

    }

    fun initView(virtualDeviceList: VirtualDeviceList){
        deviceListView.setAdapter(DeviceAdapter(view.context, virtualDeviceList))
    }
    
}