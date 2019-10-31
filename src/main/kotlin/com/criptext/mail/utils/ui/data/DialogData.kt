package com.criptext.mail.utils.ui.data

import com.criptext.mail.utils.UIMessage
sealed class DialogData(open val title: UIMessage, open val type: DialogType) {
    data class DialogMessageData(override val title: UIMessage, val message: List<UIMessage>, override val type: DialogType, val onOkPress: (() -> Unit) = {}): DialogData(title, type)
    data class DialogDataForReplyToEmail(override val title: UIMessage, val replyToEmail: String?, override val type: DialogType): DialogData(title, type)
    data class DialogDataForInput(override val title: UIMessage, val input: String?, override val type: DialogType): DialogData(title, type)
    data class DialogDataForRecoveryCode(override val title: UIMessage, val message: UIMessage, override val type: DialogType): DialogData(title, type)
    data class DialogConfirmationData(override val title: UIMessage, val message: List<UIMessage>, override val type: DialogType): DialogData(title, type)
}