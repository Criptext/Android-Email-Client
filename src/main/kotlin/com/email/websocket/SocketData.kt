package com.email.websocket

import android.util.Log
import com.email.db.models.FullEmail
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.DateUtils
import org.json.JSONObject

/**
 * Defines structures that hold data received via Web Socket. Provides methods to construct such
 * strutures from the text received in the socket payload.
 * Created by gabriel on 5/11/17.
 */

object SocketData {

    private val COMMAND_OPEN = 50
    private val COMMAND_OPEN_ATTACHMENT = 2
    private val COMMAND_DOWNLOAD_ATTACHMENT = 3
    private val COMMAND_EMAIL_UNSENT = 4
    private val COMMAND_MUTE = 5
    private val COMMAND_USER_STATUS_CHANGE = 20
    private val COMMAND_EMAIL_FAILED = 53
    private val COMMAND_EMAIL_SENT = 1
    private val COMMAND_FILE_UPLOADED = 55

    open class SocketNotification(val location: String, val timestamp: Long){

        val accessData: String
            get() = "$location:$timestamp"

    }

    class UpdateNotification(val token: String, location: String, timestamp: Long)
        : SocketNotification(location, timestamp)

    class AttachmentNotification(val mailToken: String, val fileToken: String, location: String,
                                 timestamp: Long): SocketNotification(location, timestamp)

    sealed class Cmd(val timestamp: Long) {
        class Open(timestamp: Long, val updateNotification: UpdateNotification): Cmd(timestamp)

        class OpenAttachment(timestamp: Long, val attachmentNotification: AttachmentNotification)
            : Cmd(timestamp)

        class DownloadAttachment(timestamp: Long,
                                    val attachmentNotification: AttachmentNotification)
            : Cmd(timestamp)

        class EmailUnsent(timestamp: Long, val token: String): Cmd(timestamp)

        class Mute(timestamp: Long, val shouldMute: Boolean, val tokens: List<String>)
            : Cmd(timestamp)

        class UserStatusChange(timestamp: Long, val status: Int, val plan: String): Cmd(timestamp)

        class EmailSent(
                timestamp: Long, val metadata: String ): Cmd(timestamp)

        class FileUploaded(timestamp: Long, val token: String): Cmd(timestamp)
    }



    sealed class MailDetailResponse {
        class Ok(val response: EmailThread?): MailDetailResponse()
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
        TODO()
    }

    private fun parseOpenAttachmentCmd(json: JSONObject): Cmd.OpenAttachment {
        TODO()
    }

    private fun parseDownloadAttachmentCmd(json: JSONObject): Cmd.DownloadAttachment {
        TODO()
    }

    private fun parseEmailUnsentCmd(json: JSONObject): Cmd.EmailUnsent {
        TODO()
    }

    private fun parseMuteCmd(json: JSONObject): Cmd.Mute {
        TODO()
    }

    private fun parseUserStatusChangeCmd(json: JSONObject): Cmd.UserStatusChange {
        TODO()
    }

    private fun parseEmailSentCmd(json: JSONObject): Cmd.EmailSent {
        val params = json.getJSONObject("params")
        val timestamp = params.getString("date")
        val date = DateUtils.getDateFromString(timestamp, null)

        return Cmd.EmailSent(date!!.time, json.toString())
    }

    private fun parseFileUploaded(json: JSONObject): Cmd.FileUploaded {
        TODO()
    }


    private fun parseCmd(json: JSONObject): Cmd? {
        val cmd = json.getInt("cmd")
        return when (cmd) {
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
        val arrayCMDs = ArrayList<Cmd>()
        val parsedData = JSONObject(text)
        val cmd = parseCmd(parsedData)
        arrayCMDs.add(cmd!!)
        return arrayCMDs

/*        val parsedJSONArray = JSONArray(text)
        return (0..(parsedJSONArray.length() - 1))
                .map { parsedJSONArray.getJSONObject(it) }
                .mapNotNull { parseCmd(it) }*/
    }

}
