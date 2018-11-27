package com.criptext.mail.scenes.settings.devices

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.utils.DateAndTimeUtils

/**
 * Created by danieltigse on 28/6/18.
 */

class DeviceHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val textViewDeviceName: TextView
    private val textViewCurrent: TextView
    private val textViewLastActive: TextView
    private val lastActiveLayout: LinearLayout
    private val deviceLayout: FrameLayout
    private val imageDeviceType: ImageView
    private val imageTrashDevice: ImageView


    init {
        textViewDeviceName = view.findViewById(R.id.textViewDeviceName) as TextView
        textViewCurrent = view.findViewById(R.id.textViewCurrentDevice) as TextView
        textViewLastActive = view.findViewById(R.id.textViewDeviceLastActive) as TextView
        lastActiveLayout = view.findViewById(R.id.lastActivityLayout) as LinearLayout
        deviceLayout = view.findViewById(R.id.deviceItem) as FrameLayout
        imageDeviceType = view.findViewById(R.id.imageViewDeviceType) as ImageView
        imageTrashDevice = view.findViewById(R.id.imageViewTrashDevice) as ImageView
    }

    fun bindDevice(deviceItem: DeviceItem){
        textViewDeviceName.text = deviceItem.friendlyName
        if(deviceItem.isCurrent) {
            textViewCurrent.setText(R.string.current_device)
            imageTrashDevice.visibility = View.GONE
            textViewCurrent.visibility = View.VISIBLE
            lastActiveLayout.visibility = View.INVISIBLE
        }else{
            textViewCurrent.text = ""
            if(deviceItem.lastActivity != null){
                textViewLastActive.text = view.context.getString(R.string.device_last_activity_time,
                        DateAndTimeUtils.getFormattedDate(deviceItem.lastActivity.time))
                lastActiveLayout.visibility = View.VISIBLE
            }else{
                textViewLastActive.text = view.context.getText(R.string.device_last_activity_time_2_months)
            }
            textViewCurrent.visibility = View.GONE
            imageTrashDevice.visibility = View.VISIBLE
        }
        when (deviceItem.deviceType){
            1 -> imageDeviceType.setImageResource(R.drawable.device_pc)
            2, 3 -> imageDeviceType.setImageResource(R.drawable.device_m)
        }
    }

    fun setOnClickListener(onClick: () -> Boolean){
        imageTrashDevice.setOnClickListener {
            onClick()
        }
    }
}
