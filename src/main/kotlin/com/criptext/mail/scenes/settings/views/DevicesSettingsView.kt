package com.criptext.mail.scenes.settings.views

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.settings.devices.DeviceAdapter
import com.criptext.mail.scenes.settings.devices.VirtualDeviceList
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.ui.TabView
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView
import kotlinx.android.synthetic.main.devices_item.view.*

class DevicesSettingsView(view: View, title: String): TabView(view, title){

    private lateinit var recyclerViewDevices: RecyclerView
    private lateinit var deviceListView: VirtualListView

    override fun onCreateView(){

        recyclerViewDevices = view as RecyclerView

        deviceListView = VirtualRecyclerView(recyclerViewDevices)

    }

    fun initView(virtualDeviceList: VirtualDeviceList, devicesListItemListener: DevicesListItemListener){
        deviceListView.setAdapter(DeviceAdapter(view.context, devicesListItemListener, virtualDeviceList))
    }

    fun getListView(): VirtualListView{
        return deviceListView
    }
    
}