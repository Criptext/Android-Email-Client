package com.criptext.mail.utils.removedevice.data

/**
 * Created by gabriel on 5/1/18.
 */
sealed class RemovedDeviceResult {
    sealed class DeviceRemoved: RemovedDeviceResult()  {
        class Success: DeviceRemoved()
        class Failure: DeviceRemoved()
    }

}