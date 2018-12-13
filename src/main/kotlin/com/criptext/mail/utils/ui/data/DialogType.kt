package com.criptext.mail.utils.ui.data

sealed class DialogType{
    class DeleteAccount: DialogType()
    class ManualSyncConfirmation: DialogType()
}