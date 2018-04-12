package com.email.scenes.composer.data

import com.email.api.ApiCall

/**
 * Created by gabriel on 3/15/18.
 */

class ComposerAPIClient(private val token: String) {

    fun findKeyBundles(recipients: List<String>, knownAddresses: Map<String, List<Int>>): String {
        val request = ApiCall.findKeyBundles(token, recipients, knownAddresses)
        return ApiCall.executeRequest(request)
    }

    fun postEmail(postEmailBody: PostEmailBody): String {
        val request = ApiCall.postEmail(token, postEmailBody)
        return ApiCall.executeRequest(request)
    }

}
