package com.criptext.mail.websocket

import android.content.Context
import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.websocket.data.EventDataSource

/**
 * Headless class where state will be stored so that it can be persisted on device rotations.
 * Created by Gabriel on 3/27/17.
 */

object WebSocketSingleton {
    private var INSTANCE : WebSocketController? = null

    private fun newInstance(activeAccount: ActiveAccount, context: Context): WebSocketController {
        val appDB = AppDatabase.getAppDatabase(context.applicationContext)
        val dataSource = EventDataSource(db = appDB, runner = AsyncTaskWorkRunner(),
                emailInsertionAPIClient = EmailInsertionAPIClient(HttpClient.Default(), activeAccount.jwt),
                signalClient = SignalClient.Default(SignalStoreCriptext(appDB)),
                activeAccount = activeAccount, httpClient = HttpClient.Default(),
                storage = KeyValueStorage.SharedPrefs(context))

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

