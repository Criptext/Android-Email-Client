package com.email.websocket

import android.content.Context
import com.criptext.secureemail.socket.NVWebSocketClient
import com.email.db.AppDatabase
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.signal.SignalClient
import com.email.signal.SignalStoreCriptext

/**
 * Headless class where state will be stored so that it can be persisted on device rotations.
 * Created by Gabriel on 3/27/17.
 */

class WebSocket {

    lateinit var webSocketController: WebSocketController
        private set

    companion object {

        const val HOST_URL = "stage.socket.criptext.com"
        private var INSTANCE : WebSocket? = null
        fun newInstance(
                activeAccount: ActiveAccount,
                context: Context): WebSocket {

            val stateFragment = INSTANCE

            if(stateFragment != null) {
                return stateFragment
            }

            val f = WebSocket()

            val appDatabase = AppDatabase.getAppDatabase(context)
            f.webSocketController = WebSocketController(
                    NVWebSocketClient(),
                    DetailedSocketDataOkHttpClient(activeAccount,
                            MailboxLocalDB.Default(appDatabase),
                            SignalClient.Default(SignalStoreCriptext(appDatabase))),
                    activeAccount)
            INSTANCE = f
            return f
        }

    }
}

