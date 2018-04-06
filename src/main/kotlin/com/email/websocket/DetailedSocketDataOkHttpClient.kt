package com.email.websocket

import com.email.db.models.ActiveAccount
import okhttp3.OkHttpClient

/**
 * Created by gabriel on 9/27/17.
 */

class DetailedSocketDataOkHttpClient(private val account: ActiveAccount)
    : DetailedSocketDataHttpClient {

    override fun requestMailDetail(mailToken: String, onMailDetailReady: (SocketData.MailDetailResponse) -> Unit) {
        TODO("not implemented")
    }

    private val httpClient = OkHttpClient()

}
