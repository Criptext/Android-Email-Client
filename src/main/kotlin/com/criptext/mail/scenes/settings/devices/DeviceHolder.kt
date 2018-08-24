package com.criptext.mail.scenes.settings.devices

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import com.criptext.mail.R

/**
 * Created by danieltigse on 28/6/18.
 */

class DeviceHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val textViewDeviceName: TextView
    private val textViewCurrent: TextView
    private val deviceLayout: FrameLayout
    private val imageDeviceType: ImageView


    init {
        textViewDeviceName = view.findViewById(R.id.textViewDeviceName) as TextView
        textViewCurrent = view.findViewById(R.id.textViewCurrentDevice) as TextView
        deviceLayout = view.findViewById(R.id.deviceItem) as FrameLayout
        imageDeviceType = view.findViewById(R.id.imageViewDeviceType) as ImageView
    }

    fun bindDevice(deviceItem: DeviceItem){
        textViewDeviceName.text = deviceItem.friendlyName
        if(deviceItem.isCurrent) {
            textViewCurrent.setText(R.string.current_device)
            deviceLayout.isLongClickable = false
        }
        when (deviceItem.deviceType){
            1 -> imageDeviceType.setImageResource(R.drawable.device_pc)
            2, 3 -> imageDeviceType.setImageResource(R.drawable.device_m)
        }
    }

    fun setOnLongClickListener(onLongClick: () -> Boolean){
        if(deviceLayout.isLongClickable) {
            deviceLayout.setOnLongClickListener {
                onLongClick()
            }
        }
    }
}
