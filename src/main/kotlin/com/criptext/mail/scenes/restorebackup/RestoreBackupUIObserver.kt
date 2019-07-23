package com.criptext.mail.scenes.restorebackup


interface RestoreBackupUIObserver {
    fun onCancelRestore()
    fun onRetryRestore()
    fun onChangeDriveAccount()
    fun onPasswordChangedListener(password: String)
    fun onRestore()
    fun onLocalProgressFinished()
}
