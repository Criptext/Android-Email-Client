package com.email.scenes.composer.data

/**
 * Created by gabriel on 2/26/18.
 */

sealed class ComposerRequest {
    class SuggestContacts(text: String): ComposerRequest()
}