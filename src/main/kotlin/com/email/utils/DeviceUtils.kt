package com.email.utils

import android.content.Context
import android.os.Build
import com.email.R

class DeviceUtils{

    enum class DeviceType { Pc, Phone, Tablet}

    companion object {
        fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL ?: return "TEST"
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }
        private fun capitalize(s: String?): String {
            if (s == null || s.isEmpty()) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                Character.toUpperCase(first) + s.substring(1)
            }
        }
        fun getDeviceType(context: Context): DeviceType {
            return if(context.resources.getBoolean(R.bool.isTablet)) DeviceType.Tablet
            else DeviceType.Phone
        }
    }
}