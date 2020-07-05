package com.criptext.mail.scenes.settings.cloudbackup

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver


abstract class CloudBackupUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onCloudBackupActivated(isActive: Boolean)
    abstract fun onFrequencyChanged(frequency: Int)
    abstract fun onWifiOnlySwitched(isActive: Boolean)
    abstract fun onChangeGoogleDriveAccount()
    abstract fun onPasswordChangedListener(password: String)
    abstract fun setOnCheckedChangeListener(isChecked: Boolean)
    abstract fun encryptDialogButtonPressed()
    abstract fun backUpNowPressed()
    abstract fun exportBackupPressed()
    abstract fun restoreBackupPressed()
}
