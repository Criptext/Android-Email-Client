package com.criptext.mail.scenes.settings.devices.data

import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.utils.DateAndTimeUtils


/**
 * Created by danieltigse on 28/6/18.
 */

class DeviceHolder(val view: View, val deviceItemType: DeviceItem.Companion.Type) : RecyclerView.ViewHolder(view) {

    private val rootView: RelativeLayout
    private val textViewDeviceName: TextView
    private val textViewCurrent: TextView
    private val textViewLastActive: TextView
    private val lastActiveLayout: LinearLayout
    private val deviceLayout: FrameLayout
    private val imageDeviceType: ImageView
    private val imageTrashDevice: ImageView
    private val deviceCheckbox: CheckBox

    private var listenerOnClick: (() -> Boolean)? = null

    init {
        rootView = view.findViewById(R.id.device_root_view) as RelativeLayout
        textViewDeviceName = view.findViewById(R.id.textViewDeviceName) as TextView
        textViewCurrent = view.findViewById(R.id.textViewCurrentDevice) as TextView
        textViewLastActive = view.findViewById(R.id.textViewDeviceLastActive) as TextView
        lastActiveLayout = view.findViewById(R.id.lastActivityLayout) as LinearLayout
        deviceLayout = view.findViewById(R.id.deviceItem) as FrameLayout
        imageDeviceType = view.findViewById(R.id.imageViewDeviceType) as ImageView
        imageTrashDevice = view.findViewById(R.id.imageViewTrashDevice) as ImageView
        deviceCheckbox = view.findViewById(R.id.deviceCheckbox) as CheckBox
    }

    fun bindDevice(deviceItem: DeviceItem){
        textViewDeviceName.text = deviceItem.friendlyName
        when {
            deviceItem.isCurrent && deviceItemType == DeviceItem.Companion.Type.WithCheckbox ->{
                textViewCurrent.setText(R.string.current_device)
                imageTrashDevice.visibility = View.GONE
                textViewCurrent.visibility = View.VISIBLE
                lastActiveLayout.visibility = View.INVISIBLE
                deviceCheckbox.visibility = View.VISIBLE
                deviceCheckbox.isChecked = deviceItem.checked
                deviceCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    deviceItem.checked = isChecked
                }
            }
            deviceItem.isCurrent && deviceItemType == DeviceItem.Companion.Type.Normal -> {
                textViewCurrent.setText(R.string.current_device)
                imageTrashDevice.visibility = View.GONE
                textViewCurrent.visibility = View.VISIBLE
                lastActiveLayout.visibility = View.INVISIBLE
                deviceCheckbox.visibility = View.GONE
            }
            !deviceItem.isCurrent && deviceItemType == DeviceItem.Companion.Type.Normal -> {
                textViewCurrent.text = ""
                if(deviceItem.lastActivity != null){
                    textViewLastActive.text = view.context.getString(R.string.device_last_activity_time,
                            DateAndTimeUtils.getFormattedDate(deviceItem.lastActivity.time, view.context))
                    lastActiveLayout.visibility = View.VISIBLE
                }else{
                    textViewLastActive.text = view.context.getText(R.string.device_last_activity_time_2_months)
                }
                imageTrashDevice.visibility = View.VISIBLE
                textViewCurrent.visibility = View.VISIBLE
                lastActiveLayout.visibility = View.VISIBLE
                deviceCheckbox.visibility = View.GONE
            }
            !deviceItem.isCurrent && deviceItemType == DeviceItem.Companion.Type.WithCheckbox -> {
                textViewCurrent.text = ""
                if(deviceItem.lastActivity != null){
                    textViewLastActive.text = view.context.getString(R.string.device_last_activity_time,
                            DateAndTimeUtils.getFormattedDate(deviceItem.lastActivity.time, view.context))
                    lastActiveLayout.visibility = View.VISIBLE
                }else{
                    textViewLastActive.text = view.context.getText(R.string.device_last_activity_time_2_months)
                }
                imageTrashDevice.visibility = View.GONE
                textViewCurrent.visibility = View.GONE
                lastActiveLayout.visibility = View.VISIBLE
                deviceCheckbox.visibility = View.VISIBLE
                deviceCheckbox.isChecked = deviceItem.checked
                deviceCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    deviceItem.checked = isChecked
                    listenerOnClick?.let { it() }
                }
                rootView.isClickable = true
                rootView.setOnClickListener {
                    performClickCheck(deviceItem)
                }
                val outValue = TypedValue()
                view.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                rootView.setBackgroundResource(outValue.resourceId)
            }
        }
        when (deviceItem.deviceType){
            1 -> imageDeviceType.setImageResource(R.drawable.device_pc)
            2, 3 -> imageDeviceType.setImageResource(R.drawable.device_m)
        }
    }

    fun setOnClickListener(onClick: () -> Boolean){
        when(deviceItemType){
            DeviceItem.Companion.Type.Normal -> {
                imageTrashDevice.setOnClickListener {
                    onClick()
                }
            }
            DeviceItem.Companion.Type.WithCheckbox -> {
                listenerOnClick = onClick
            }
        }
    }

    private fun performClickCheck(item: DeviceItem){
        deviceCheckbox.setOnCheckedChangeListener { _, _ ->  }
        deviceCheckbox.isChecked = !deviceCheckbox.isChecked
        item.checked = deviceCheckbox.isChecked
        deviceCheckbox.setOnCheckedChangeListener { _, isChecked ->
            item.checked = isChecked
            listenerOnClick?.let { it() }
        }
        listenerOnClick?.let { it() }
    }
}
