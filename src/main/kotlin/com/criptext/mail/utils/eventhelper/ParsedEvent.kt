package com.criptext.mail.utils.eventhelper

import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.TrackingUpdate
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.ActionRequiredData
import com.criptext.mail.scenes.mailbox.data.UpdateBannerData
import java.util.*

sealed class ParsedEvent(open val cmd: Int){
    data class NewEmail(override val cmd: Int, val preview: EmailPreview) : ParsedEvent(cmd)
    data class BannerData(override val cmd: Int, val updateBannerData: UpdateBannerData) : ParsedEvent(cmd)
    data class ActionRequired(override val cmd: Int, val actionRequiredData: ActionRequiredData) : ParsedEvent(cmd)
    data class LinkDeviceInfo(override val cmd: Int, val deviceInfo: DeviceInfo) : ParsedEvent(cmd)
    data class ChangeToLabels(override val cmd: Int, val customLabels: List<Label>) : ParsedEvent(cmd)
    data class ReadThreads(override val cmd: Int, val threadRead: Pair<List<String>, Boolean>) : ParsedEvent(cmd)
    data class ReadEmails(override val cmd: Int, val emailRead: Pair<List<Long>, Boolean>) : ParsedEvent(cmd)
    data class MoveThread(override val cmd: Int, val threadIds: List<String>) : ParsedEvent(cmd)
    data class MoveEmail(override val cmd: Int) : ParsedEvent(cmd)
    data class NameChange(override val cmd: Int, val newName: String) : ParsedEvent(cmd)
    data class AvatarChange(override val cmd: Int) : ParsedEvent(cmd)
    data class UnsendEmail(override val cmd: Int, val unsend: Pair<Long, Date>) : ParsedEvent(cmd)
    data class TrackingEvent(override val cmd: Int, val trackingUpdate: TrackingUpdate) : ParsedEvent(cmd)
}