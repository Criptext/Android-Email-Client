package com.criptext.mail.websocket

import com.criptext.mail.db.models.ActiveAccount

/**
 * Headless class where state will be stored so that it can be persisted on device rotations.
 * Created by Gabriel on 3/27/17.
 */

object WebSocketSingleton {
    private var lastRecipientId: String = ""
    private var INSTANCE : WebSocketController? = null

    private fun newInstance(activeAccount: ActiveAccount): WebSocketController {
        INSTANCE = WebSocketController(
                NVWebSocketClient(), activeAccount.jwt)
        return INSTANCE!!
    }

    fun getInstance(activeAccount: ActiveAccount): WebSocketController {
        if(lastRecipientId != activeAccount.recipientId){
            INSTANCE?.disconnect()
            INSTANCE = null
            lastRecipientId = activeAccount.recipientId
        }
        return INSTANCE ?: newInstance(activeAccount)
    }

}

