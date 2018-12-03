package com.criptext.mail.utils.ui.data

sealed class DialogResult{
    data class DialogWithInput(val textInput: String, val type: DialogType): DialogResult()
}