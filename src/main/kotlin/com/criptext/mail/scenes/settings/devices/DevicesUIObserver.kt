package com.criptext.mail.scenes.settings.devices

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class DevicesUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onRemoveDeviceConfirmed(deviceId: Int, position: Int, password: String)
    abstract fun onRemoveDeviceCancel()
    abstract fun onRemoveDevice(deviceId: Int, position: Int)
}