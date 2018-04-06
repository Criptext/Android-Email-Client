package com.email.websocket

import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController

/**
 * Manages the web socket, exposes methods to connect, disconnect, reconnect and subscribe/unsubscribe
 * listeners to the web socket. Parses the text messages from commands received via the socket and
 * publishes events to subscriber scene controllers.
 * Created by gabriel on 9/15/17.
 */

class WebSocketController(private val wsClient: WebSocketClient,
                          apiClient: DetailedSocketDataHttpClient,
                          localDB: SocketLocalDB,
                          private val activeAccount: ActiveAccount): WebSocketEventPublisher {
    private val webSocketListeners = HashMap<String, WebSocketEventListener>()
    private val defaultCmdHandler = CmdHandler.Default(apiClient, localDB, activeAccount,
            webSocketListeners)

    private val onMessageReceived = { text: String ->
        val receivedCmds = SocketData.parseSocketTextMessage(text)
        TODO("UPDATE THE MAILBOX...")
/*        if (receivedCmds.isNotEmpty()) {
            val lastCmd = receivedCmds.last()
            updateLastSync(lastCmd.timestamp)

            val newMailOpens = receivedCmds.count { it is SocketData.Cmd.Open }
            updateUnreadNotificationsBadge(newMailOpens)

            receivedCmds.forEach { defaultCmdHandler.handle(it) }
        }*/
    }

    init {
/*        val lastSync = keyPairStorage.getInt(KeyPairStorage.Key.lastSync, 0)
        val url = createCriptextSocketServerURL(accountManager.criptextUserId, lastSync)
        */
        val url = createCriptextSocketServerURL(
                recipienId = activeAccount.recipientId,
                deviceId = 1 )

        wsClient.connect(url, onMessageReceived)
    }

    fun disconnect() {
        wsClient.disconnect()
    }

    fun reconnect() {
        wsClient.reconnect()
    }

    override fun subscribe(subscriberClass: Class<SceneController>, listener: WebSocketEventListener) {
        webSocketListeners.put(subscriberClass.name, listener)
    }

    override fun unsubscribe(subscriberClass: Class<SceneController>) {
        webSocketListeners.remove(subscriberClass.name)
    }

    companion object {
        private fun createCriptextSocketServerURL(recipienId: String, deviceId: Int): String {
            return """ws://10.0.3.2:3001?recipientId=$recipienId&deviceId=$deviceId"""
        }
    }

}
