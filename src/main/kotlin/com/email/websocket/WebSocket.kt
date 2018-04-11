package com.email.websocket

import android.content.Context
import com.criptext.secureemail.socket.NVWebSocketClient
import com.email.db.AppDatabase
import com.email.db.models.ActiveAccount

/**
 * Headless class where state will be stored so that it can be persisted on device rotations.
 * Created by Gabriel on 3/27/17.
 */

class WebSocket {

    lateinit var webSocketController: WebSocketController
        private set

    companion object {

        const val HOST_URL = "54.245.42.9:3001"
        private var INSTANCE : WebSocket? = null
        fun newInstance(
                activeAccount: ActiveAccount,
                context: Context): WebSocket {

            val stateFragment = INSTANCE

            if(stateFragment != null) {
                return stateFragment
            }

            val f = WebSocket()

            val account = activeAccount
            val appDatabase = AppDatabase.getAppDatabase(context)
            f.webSocketController = WebSocketController(
                    NVWebSocketClient(),
                    DetailedSocketDataOkHttpClient(account, appDatabase),
                    account)
            INSTANCE = f
            return f
        }

    }
}

