package com.email

import android.content.Context
import android.os.Bundle
import com.criptext.secureemail.socket.NVWebSocketClient
import com.email.db.AppDatabase
import com.email.db.models.ActiveAccount
import com.email.websocket.ActiveAndroidSocketDB
import com.email.websocket.DetailedSocketDataOkHttpClient
import com.email.websocket.WebSocketController

/**
 * Headless fragment where state will be stored so that it can be persisted on device rotations.
 * Created by Gabriel on 3/27/17.
 */

class StateFragment  {

    lateinit var webSocketController: WebSocketController
        private set

    companion object {

        private var INSTANCE : StateFragment? = null
        fun newInstance(
                activeAccount: ActiveAccount,
                context: Context): StateFragment {

            val stateFragment = INSTANCE

            if(stateFragment != null) {
                return stateFragment
            }

            val f = StateFragment()

            val account = activeAccount
            val appDatabase = AppDatabase.getAppDatabase(context)
            f.webSocketController = WebSocketController(
                    NVWebSocketClient(),
                    DetailedSocketDataOkHttpClient(account, appDatabase),
                    ActiveAndroidSocketDB(),
                    account)
            INSTANCE = f
            return f
        }

    }
}

