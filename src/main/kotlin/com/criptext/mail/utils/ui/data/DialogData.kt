package com.criptext.mail.utils.ui.data

import com.criptext.mail.utils.UIMessage
sealed class DialogData {
    data class DialogMessageData(val title: UIMessage, val message: List<UIMessage>, val type: DialogType, val onOkPress: (() -> Unit) = {}): DialogData()
    data class DialogDataForReplyToEmail(val title: UIMessage, val replyToEmail: String?, val type: DialogType): DialogData()
    data class DialogConfirmationData(val title: UIMessage, val message: List<UIMessage>, val type: DialogType): DialogData()
}