package com.criptext.mail.utils

import android.content.Context
import android.os.Build
import com.criptext.mail.R

class DeviceUtils{

    enum class DeviceType { NONE, PC, iOS, Android}

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

        fun getDeviceOS(): String {
            return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
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
        fun getDeviceType(): DeviceType {
            return DeviceType.Android
        }
    }
}