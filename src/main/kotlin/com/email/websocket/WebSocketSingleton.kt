package com.email.websocket

import android.content.Context
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

object WebSocketSingleton {
    const val HOST_URL = "stage.socket.criptext.com:3001"
    private var INSTANCE : WebSocketController? = null

    private fun newInstance(activeAccount: ActiveAccount, context: Context): WebSocketController {
        val appDB = AppDatabase.getAppDatabase(context.applicationContext)
        val dataSource = EventDataSource(runner = AsyncTaskWorkRunner(),
                emailInsertionDao = appDB.emailInsertionDao(),
                emailInsertionAPIClient = EmailInsertionAPIClient(activeAccount.jwt),
                signalClient = SignalClient.Default(SignalStoreCriptext(appDB)))

        INSTANCE = WebSocketController(
                NVWebSocketClient(), activeAccount, dataSource)
        return INSTANCE!!
    }

    fun getInstance(
            activeAccount: ActiveAccount,
            context: Context): WebSocketController {
        return INSTANCE ?: newInstance(activeAccount, context)
    }

}

