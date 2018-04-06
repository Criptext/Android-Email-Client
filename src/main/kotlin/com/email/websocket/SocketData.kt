package com.email.websocket

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Defines structures that hold data received via Web Socket. Provides methods to construct such
 * strutures from the text received in the socket payload.
 * Created by gabriel on 5/11/17.
 */

object SocketData {

    private val COMMAND_OPEN = 1
    private val COMMAND_OPEN_ATTACHMENT = 2
    private val COMMAND_DOWNLOAD_ATTACHMENT = 3
    private val COMMAND_EMAIL_UNSENT = 4
    private val COMMAND_MUTE = 5
    private val COMMAND_USER_STATUS_CHANGE = 20
    private val COMMAND_EMAIL_FAILED = 53
    private val COMMAND_EMAIL_SENT = 54
    private val COMMAND_FILE_UPLOADED = 55

    open class SocketNotification(val location: String, val timestamp: Long){

        val accessData: String
            get() = "$location:$timestamp"

    }

    class UpdateNotification(val token: String, location: String, timestamp: Long)
        : SocketNotification(location, timestamp)

    class AttachmentNotification(val mailToken: String, val fileToken: String, location: String,
                                 timestamp: Long): SocketNotification(location, timestamp)

    sealed class Cmd(val timestamp: Int) {
        class Open(timestamp: Int, val updateNotification: UpdateNotification): Cmd(timestamp)

        class OpenAttachment(timestamp: Int, val attachmentNotification: AttachmentNotification)
            : Cmd(timestamp)

        class DownloadAttachment(timestamp: Int,
                                    val attachmentNotification: AttachmentNotification)
            : Cmd(timestamp)

        class EmailUnsent(timestamp: Int, val token: String): Cmd(timestamp)

        class Mute(timestamp: Int, val shouldMute: Boolean, val tokens: List<String>)
            : Cmd(timestamp)

        class UserStatusChange(timestamp: Int, val status: Int, val plan: String): Cmd(timestamp)

        class EmailSent(timestamp: Int, val token: String): Cmd(timestamp)

        class FileUploaded(timestamp: Int, val token: String): Cmd(timestamp)
    }



    sealed class MailDetailResponse {
        class Ok(val response: String): MailDetailResponse()
        class Error(val exception: Exception?): MailDetailResponse()
    }

    private fun parseUpdateNotification(json: JSONObject): UpdateNotification {
        val args = json.getJSONObject("args")
        val token = args.getString("msg").split(":")[1]
        val location = args.getString("location")
        val timestamp = args.getLong("timestamp")
        return UpdateNotification(token, location, timestamp)
    }

    private fun parseAttachmentNotification(json: JSONObject): AttachmentNotification {
        val args = json.getJSONObject("args")
        val mailToken = args.getString("email_token")
        val fileToken = args.getString("file_token")
        val location = args.getString("location")
        val timestamp = args.getLong("timestamp")
        return AttachmentNotification(mailToken, fileToken, location, timestamp)
    }

    private fun parseOpenCmd(json: JSONObject): Cmd.Open {
        val timestamp = json.getInt("timestamp")
        val notification = parseUpdateNotification(json)
        return Cmd.Open(timestamp, notification)
    }

    private fun parseOpenAttachmentCmd(json: JSONObject): Cmd.OpenAttachment {
        val timestamp = json.getInt("timestamp")
        val notification = parseAttachmentNotification(json)
        return Cmd.OpenAttachment(timestamp, notification)
    }

    private fun parseDownloadAttachmentCmd(json: JSONObject): Cmd.DownloadAttachment {
        val timestamp = json.getInt("timestamp")
        val notification = parseAttachmentNotification(json)

        return Cmd.DownloadAttachment(timestamp, notification)
    }

    private fun parseEmailUnsentCmd(json: JSONObject): Cmd.EmailUnsent {
        val timestamp = json.getInt("timestamp")
        val token = json.getJSONObject("args").getString("msg").split(":")[0]

        return Cmd.EmailUnsent(timestamp, token)
    }

    private fun parseMuteCmd(json: JSONObject): Cmd.Mute {
        val timestamp = json.getInt("timestamp")
        val msg = json.getJSONObject("args").getJSONObject("msg")
        val shouldMute = msg.getString("mute") == "1"
        val tokens = msg.getString("tokens").split(",")

        return Cmd.Mute(timestamp, shouldMute, tokens)
    }

    private fun parseUserStatusChangeCmd(json: JSONObject): Cmd.UserStatusChange {
        val timestamp = json.getInt("timestamp")
        val args = json.getJSONObject("args")
        val status = args.getString("msg").toInt()
        var plan = args.getString("plan")
        if (plan.isEmpty()) {
            plan = "Free Trial"
        }

        return Cmd.UserStatusChange(timestamp, status, plan)
    }

    private fun parseEmailSentCmd(json: JSONObject): Cmd.EmailSent {
        val timestamp = json.getInt("timestamp")
        val token = json.getJSONObject("args").getString("msg").split(":")[0]

        return Cmd.EmailSent(timestamp, token)
    }

    private fun parseFileUploaded(json: JSONObject): Cmd.FileUploaded {
        val timestamp = json.getInt("timestamp")
        val token = json.getJSONObject("args").getString("msg")

        return Cmd.FileUploaded(timestamp, token)
    }


    private fun parseCmd(json: JSONObject): Cmd? {
        val cmd = json.getInt("cmd")
        return when (cmd) {
            COMMAND_OPEN -> parseOpenCmd(json)
            COMMAND_OPEN_ATTACHMENT -> parseOpenAttachmentCmd(json)
            COMMAND_DOWNLOAD_ATTACHMENT -> parseDownloadAttachmentCmd(json)
            COMMAND_EMAIL_UNSENT -> parseEmailUnsentCmd(json)
            COMMAND_MUTE -> parseMuteCmd(json)
            COMMAND_USER_STATUS_CHANGE -> parseUserStatusChangeCmd(json)
            COMMAND_EMAIL_SENT -> parseEmailSentCmd(json)
            COMMAND_FILE_UPLOADED -> parseFileUploaded(json)
            else -> {
                Log.e("SocketData", "failed to parse command from JSON:  $json")
                null
            }

        }
    }

    fun parseSocketTextMessage(text: String): List<Cmd> {
        Log.d("newMessage Coming,", text)
/*         val parsedJSONArray = JSONArray(text)
         return (0..(parsedJSONArray.length() - 1))
                 .map { parsedJSONArray.getJSONObject(it) }
                 .mapNotNull { parseCmd(it) }*/
        TODO("RENDER TEXT MESSAGE IN MAILBOX")
    }

}
