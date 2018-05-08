package com.email.scenes.mailbox.data

import com.email.api.ApiCall
import com.email.scenes.composer.data.PostEmailBody

/**
 * Created by sebas on 3/20/18.
 */


class MailboxAPIClient(private val token: String) {

    fun getPendingEvents(): String {
        val request = ApiCall.getPendingEvents(token = token)
        return ApiCall.executeRequest(request)
    }

    fun getBodyFromEmail(uuid: String): String {
        val request = ApiCall.getBodyFromEmail(
                token = token,
                uuid= uuid
                )
        return ApiCall.executeRequest(request)
    }

    fun findKeyBundles(recipients: List<String>, knownAddresses: Map<String, List<Int>>): String {
        val request = ApiCall.findKeyBundles(token, recipients, knownAddresses)
        return ApiCall.executeRequest(request)
    }

    fun postEmail(postEmailBody: PostEmailBody): String {
        val request = ApiCall.postEmail(token, postEmailBody)
        return ApiCall.executeRequest(request)
    }

    fun acknowledgeEvents(eventIds: List<Long>): String {
        val request = ApiCall.acknowledgeEvents(token, eventIds)
        return ApiCall.executeRequest(request)
    }

}
