package com.criptext.mail.utils

import android.content.Context
import android.os.Build
import com.criptext.mail.db.models.ActiveAccount
import android.bluetooth.BluetoothAdapter
import com.github.kittinunf.result.Result


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

        fun getDeviceFriendlyName(): String{
//            val myDevice = BluetoothAdapter.getDefaultAdapter()
//            if(myDevice != null){
//                val operation = Result.of { myDevice.name }
//                if(operation is Result.Success)
//                    return operation.value
//            }
//            return getDeviceName()
            return getDeviceName()
        }

        fun getDeviceId(context: Context): Int?{
            val activeAccount = ActiveAccount.loadFromStorage(context)
            return activeAccount?.deviceId
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

        fun getDeviceType(ordinal: Int): DeviceType{
            return when(ordinal){
                1 -> DeviceType.PC
                2 -> DeviceType.iOS
                3 -> DeviceType.Android
                else -> DeviceType.NONE
            }
        }
    }
}