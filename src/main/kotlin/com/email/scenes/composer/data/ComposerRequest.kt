package com.email.scenes.composer.data

import com.email.scenes.composer.ui.UIData

/**
 * Created by gabriel on 2/26/18.
 */

sealed class ComposerRequest {
    class SendMail(val data: UIData): ComposerRequest()
    class SuggestContacts(text: String): ComposerRequest()
}