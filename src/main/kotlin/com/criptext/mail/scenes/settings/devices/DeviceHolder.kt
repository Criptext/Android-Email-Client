package com.criptext.mail.scenes.settings.devices

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.criptext.mail.R

/**
 * Created by danieltigse on 28/6/18.
 */

class DeviceHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val textViewDeviceName: TextView
    private val textViewCurrent: TextView

    init {
        textViewDeviceName = view.findViewById(R.id.textViewDeviceName) as TextView
        textViewCurrent = view.findViewById(R.id.textViewCurrentDevice) as TextView
    }

    fun bindDevice(deviceItem: DeviceItem){
        textViewDeviceName.text = deviceItem.friendlyName
        if(deviceItem.isCurrent)
            textViewCurrent.setText(R.string.current_device)
    }
}
