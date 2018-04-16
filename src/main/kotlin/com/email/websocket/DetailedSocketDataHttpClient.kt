package com.email.websocket

/**
 * Some commands received on the socket only contain a small, summarized portion of the necessary
 * data. To get the whole thing, an Http request to the Criptext server is needed. This interface
 * abstracts away the implementation of such requests.
 * Created by gabriel on 9/27/17.
 */

interface DetailedSocketDataHttpClient {

    fun requestMailDetail(mailToken: String,
                          onMailDetailReady: (SocketData.MailDetailResponse) -> Unit)

}
