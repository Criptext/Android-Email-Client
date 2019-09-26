package com.criptext.mail.scenes.settings.data

import com.criptext.mail.utils.UIMessage

sealed class SettingsResult{
    sealed class ResetPassword : SettingsResult() {
        class Success: ResetPassword()
        data class Failure(val message: UIMessage): ResetPassword()
    }

    sealed class SyncBegin: SettingsResult() {
        class Success: SyncBegin()
        data class NoDevicesAvailable(val message: UIMessage): SyncBegin()
        data class Failure(val message: UIMessage): SyncBegin()
    }

    sealed class UpdateSignature: SettingsResult() {
        class Success: UpdateSignature()
        data class Failure(val message: UIMessage): UpdateSignature()
    }
}