package com.criptext.mail.websocket

import com.criptext.mail.db.models.ActiveAccount

/**
 * Headless class where state will be stored so that it can be persisted on device rotations.
 * Created by Gabriel on 3/27/17.
 */

object WebSocketSingleton {
    private var INSTANCE : WebSocketController? = null

    private fun newInstance(jwts: String): WebSocketController {
        INSTANCE = WebSocketController(
                NVWebSocketClient(), jwts)
        return INSTANCE!!
    }

    fun getInstance(jwts: String): WebSocketController {
        return INSTANCE ?: newInstance(jwts)
    }

}

