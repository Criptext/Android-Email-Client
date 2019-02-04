package com.criptext.mail.utils

import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.UpdateBannerData

data class EventHelperResultData(val emailPreviews: List<EmailPreview>, val updateBannerData: UpdateBannerData?,
                                 val deviceInfo: List<DeviceInfo?>, val shouldNotify: Boolean)