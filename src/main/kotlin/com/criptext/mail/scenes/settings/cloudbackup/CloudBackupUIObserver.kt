package com.criptext.mail.scenes.settings.cloudbackup


interface CloudBackupUIObserver {
    fun onBackButtonPressed()
    fun onCloudBackupActivated(isActive: Boolean)
    fun onFrequencyChanged(frequency: Int)
    fun onWifiOnlySwiched(isActive: Boolean)
    fun onChangeGoogleDriveAccount()
    fun onPasswordChangedListener(password: String)
    fun setOnCheckedChangeListener(isChecked: Boolean)
    fun encryptDialogButtonPressed()
    fun backUpNowPressed()
}
