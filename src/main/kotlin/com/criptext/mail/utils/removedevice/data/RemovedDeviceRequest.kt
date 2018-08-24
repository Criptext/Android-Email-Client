package com.criptext.mail.utils.removedevice.data

/**
 * Created by gabriel on 5/1/18.
 */
sealed class RemovedDeviceRequest {
    class DeviceRemoved: RemovedDeviceRequest()
}