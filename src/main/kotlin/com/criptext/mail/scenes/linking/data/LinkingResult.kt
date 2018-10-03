package com.criptext.mail.scenes.linking.data

import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.UIMessage

sealed class LinkingResult{
    sealed class CheckForKeyBundle: LinkingResult() {
        data class Success(val keyBundle: PreKeyBundleShareData.DownloadBundle): CheckForKeyBundle()
        data class Failure(val message: UIMessage): CheckForKeyBundle()
    }
}