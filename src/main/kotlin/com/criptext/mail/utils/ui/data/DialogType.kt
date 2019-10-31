package com.criptext.mail.utils.ui.data

sealed class DialogType{
    class DeleteAccount: DialogType()
    class ManualSyncConfirmation: DialogType()
    class ReplyToChange: DialogType()
    class RecoveryCode: DialogType()
    class SwitchAccount: DialogType()
    class SignIn: DialogType()
    class Message: DialogType()
    class DeleteLabel: DialogType()
    class EditLabel: DialogType()
}