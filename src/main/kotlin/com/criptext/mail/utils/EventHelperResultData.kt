package com.criptext.mail.utils

import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.UpdateBannerData
import java.util.*

data class EventHelperResultData(val updateBannerData: UpdateBannerData?,
                                 val deviceInfo: List<DeviceInfo?>, val shouldNotify: Boolean,
                                 val newEmails: List<EmailPreview>, val customLabels: List<Label>,
                                 val threadReads: Pair<List<String>, Boolean>?,
                                 val emailReads: Pair<List<Long>, Boolean>?,
                                 val movedThread: List<Triple<List<String>, List<Label>?, List<Label>?>>,
                                 val movedEmail: List<Triple<List<Long>, List<Label>?, List<Label>?>>,
                                 val nameChanged: String, val unsend: Pair<Long, Date>?)
