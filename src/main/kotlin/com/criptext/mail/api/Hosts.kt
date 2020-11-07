package com.criptext.mail.api

/**
 * Created by gabriel on 5/9/18.
 */

object Hosts {
    const val restApiBaseUrl = "https://stage.mail.criptext.com"
    const val webSocketBaseUrl = "wss://stagesocket.criptext.com"
    const val fileServiceUrl = "https://services.criptext.com"
    const val fileTransferServer = "https://stagetransfer.criptext.com"
    const val newsRepository = "https://news.criptext.com"
    const val fileServiceAuthToken = "cXluaHR5empyc2hhenhxYXJrcHk6bG9mamtzZWRieHV1Y2RqanBuYnk="
    const val HELP_DESK_URL = "https://criptext.atlassian.net/servicedesk/customer/portals"
    const val ADMIN_URL = "https://stageadmin.criptext.com/"
    const val ACCOUNT_URL = "https://stageaccount.criptext.com/"

    fun billing(jwt: String, language: String) = "${ACCOUNT_URL}?#/billing?token=$jwt&lang=$language"
    fun addressManager(jwt: String, language: String) = "${ACCOUNT_URL}?#/addresses??token=$jwt&lang=$language"
}