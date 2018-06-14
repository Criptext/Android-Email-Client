package com.email.scenes.settings.views

import android.support.v7.widget.RecyclerView
import android.view.View
import com.email.scenes.settings.devices.DeviceAdapter
import com.email.scenes.settings.devices.VirtualDeviceList
import com.email.utils.ui.TabView
import com.email.utils.virtuallist.VirtualListView
import com.email.utils.virtuallist.VirtualRecyclerView

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