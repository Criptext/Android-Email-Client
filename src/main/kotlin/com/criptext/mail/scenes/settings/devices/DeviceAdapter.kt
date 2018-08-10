package com.criptext.mail.scenes.settings.devices

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.virtuallist.VirtualListAdapter

/**
 * Created by danieltigse on 29/6/18.
 */

class DeviceAdapter(private val mContext : Context,
                    private val devicesListItemListener: DevicesListItemListener,
                    private val deviceList: VirtualList<DeviceItem>)
    : VirtualListAdapter(deviceList) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is DeviceHolder -> {
                if(holder.itemView == null) return
                val device = deviceList[position]
                holder.bindDevice(device)
                holder.setOnLongClickListener {
                    devicesListItemListener.onDeviceLongClicked(device, position)
                }
            }
        }
    }

    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView : View = View.inflate(mContext, R.layout.devices_item, null )
        return DeviceHolder(itemView)
    }

    override fun getActualItemViewType(position: Int): Int {
        return 1
    }

    override fun onApproachingEnd() {

    }

    override fun getActualItemId(position: Int): Long {
        return deviceList[position].id.toLong()
    }

}
