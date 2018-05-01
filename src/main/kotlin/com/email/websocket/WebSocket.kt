package com.email.websocket

import android.content.Context
import com.criptext.secureemail.socket.NVWebSocketClient
import com.email.api.EmailInsertionAPIClient
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.models.ActiveAccount
import com.email.signal.SignalClient
import com.email.signal.SignalStoreCriptext
import com.email.websocket.data.EventDataSource

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

        private fun newInstance(activeAccount: ActiveAccount, context: Context): WebSocket {
            val appDB = AppDatabase.getAppDatabase(context.applicationContext)
            val f = WebSocket()
            val dataSource = EventDataSource(runner = AsyncTaskWorkRunner(),
                    emailInsertionDao = appDB.emailInsertionDao(),
                    emailInsertionAPIClient = EmailInsertionAPIClient(activeAccount.jwt),
                    signalClient = SignalClient.Default(SignalStoreCriptext(appDB)))

            f.webSocketController = WebSocketController(
                    NVWebSocketClient(), activeAccount, dataSource)
            INSTANCE = f
            return f
        }

        fun getInstance(
                activeAccount: ActiveAccount,
                context: Context): WebSocket {
            return INSTANCE ?: newInstance(activeAccount, context)
        }

    }
}

