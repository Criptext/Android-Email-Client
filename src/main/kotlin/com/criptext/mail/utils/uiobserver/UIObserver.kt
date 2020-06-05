package com.criptext.mail.utils.uiobserver

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult

abstract class UIObserver(private val generalDataSource: GeneralDataSource,
                        private val host: IHostActivity){
    abstract fun onGeneralOkButtonPressed(result: DialogResult)
    abstract fun onGeneralCancelButtonPressed(result: DialogResult)
    abstract fun onOkButtonPressed(password: String)
    abstract fun onCancelButtonPressed()
    abstract fun onSnackbarClicked()

    fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo){
        if(untrustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
            generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
        else
            host.showToastMessage(UIMessage(R.string.sync_version_incorrect))
    }

    fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo){
        generalDataSource.submitRequest(GeneralRequest.LinkDenied(untrustedDeviceInfo))
    }

    fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo){
        if(trustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
            generalDataSource.submitRequest(GeneralRequest.SyncAccept(trustedDeviceInfo))
        else
            host.showToastMessage(UIMessage(R.string.sync_version_incorrect))
    }

    fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
        generalDataSource.submitRequest(GeneralRequest.SyncDenied(trustedDeviceInfo))
    }
}