package com.email.websocket

import android.util.Log
import com.email.db.models.ActiveAccount
import org.json.JSONArray

/**
 * Exposes actions to execute when a command is received via the web socket, and emits events to
 * the subscribers of the web socket.
 * Created by gabriel on 9/15/17.
 */

interface CmdHandler {

    fun handle(cmd: SocketData.Cmd)

    class Default(private val apiClient: DetailedSocketDataHttpClient,
                  private val activeAccount: ActiveAccount,
                  private val webSocketListeners: Map<String, WebSocketEventListener>): CmdHandler {

        private val handleMailDetailResponse: (SocketData.MailDetailResponse) -> Unit =
                { mailDetail ->

                    when (mailDetail) {
                        is SocketData.MailDetailResponse.Ok -> {
                            webSocketListeners.values.forEach {
                                if(mailDetail.response != null) {
                                    it.onNewMessage(mailDetail.response)
                                }
                            }
                        }
                        is SocketData.MailDetailResponse.Error -> {
                            Log.e("ERROR", "...")
                        }
                    }
                }

        private fun requestMailDetail(metadata: String) {
            apiClient.requestMailDetail(metadata, handleMailDetailResponse)
        }

        private fun prependToNewOpenArray(openArray: JSONArray,
                                          notification: SocketData.SocketNotification)
                : JSONArray {
            val newOpenArray = JSONArray()
            val openStringData = notification.accessData
            newOpenArray.put(openStringData)
            for (i in 0..(openArray.length() - 1)) {
                newOpenArray.put(openArray.get(i))
            }
            return newOpenArray
        }


        private fun handleEmailSentCmd(cmd: SocketData.Cmd.EmailSent) {
            requestMailDetail(cmd.metadata)
        }

        private fun handleFileUploadedCmd(cmd: SocketData.Cmd.FileUploaded) {
            requestMailDetail(cmd.token)
        }

        override fun handle(cmd: SocketData.Cmd) {
            when (cmd) {
                is SocketData.Cmd.Open -> TODO("open")
                is SocketData.Cmd.OpenAttachment -> TODO("open attachment")
                is SocketData.Cmd.DownloadAttachment -> TODO("download attachment")
                is SocketData.Cmd.EmailUnsent -> TODO("handle unsend")
                is SocketData.Cmd.Mute-> TODO("handle mute")
                is SocketData.Cmd.UserStatusChange -> TODO("handle status change")
                is SocketData.Cmd.EmailSent -> handleEmailSentCmd(cmd)
                is SocketData.Cmd.FileUploaded-> handleFileUploadedCmd(cmd)
            }
        }
    }
}
