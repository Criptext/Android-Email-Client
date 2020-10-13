package com.criptext.mail.api

/**
 * Created by gabriel on 5/9/18.
 */

object Hosts {
    const val restApiBaseUrl = "https://api.criptext.com"
    const val webSocketBaseUrl = "wss://socket.criptext.com"
    const val fileServiceUrl = "https://services.criptext.com"
    const val fileTransferServer = "https://transfer.criptext.com"
    const val newsRepository = "https://news.criptext.com"
    const val fileServiceAuthToken = "cXluaHR5empyc2hhenhxYXJrcHk6bG9mamtzZWRieHV1Y2RqanBuYnk="
    const val HELP_DESK_URL = "https://criptext.atlassian.net/servicedesk/customer/portals"
    const val ADMIN_URL = "https://admin.criptext.com/"
    const val ACCOUNT_URL = "https://account.criptext.com/"

    fun billing(jwt: String, language: String) = "${ACCOUNT_URL}?#/account/billing?token=$jwt&lang=$language"
    fun addressManager(jwt: String, language: String) = "${ACCOUNT_URL}?#/addresses??token=$jwt&lang=$language"
}