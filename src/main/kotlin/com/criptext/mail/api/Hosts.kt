package com.criptext.mail.api

import com.criptext.mail.BuildConfig

/**
 * Created by gabriel on 5/9/18.
 */

object Hosts {
    val restApiBaseUrl = if(BuildConfig.DEBUG) "https://stage.mail.criptext.com"
                        else "https://api.criptext.com"
    val webSocketBaseUrl = if(BuildConfig.DEBUG) "wss://stagesocket.criptext.com:3002"
                        else "wss://socket.criptext.com:3002"
    val fileServiceUrl = "https://services.criptext.com"
    val fileTransferServer = "https://transfer.criptext.com"
    val fileServiceAuthToken = "cXluaHR5empyc2hhenhxYXJrcHk6bG9mamtzZWRieHV1Y2RqanBuYnk="
}