package com.criptext.mail.api.models

import org.json.JSONObject

/**
 * Created by gabriel on 4/26/18.
 */
data class Event(val rowid: Long, val recipientId: String, val cmd: Int, val params: String) {
    companion object {
        fun fromJSON(jsonEventString: String): Event {
            val json = JSONObject(jsonEventString)
            val rowId = if(json.has("rowid"))
                                json.getLong("rowid")
                              else
                                -1
            return Event(rowid = rowId,
                    recipientId = if(json.has("recipientId")) json.getString("recipientId") else "",
                    cmd = json.getInt("cmd"),
                    params = json.getString("params"))
        }
    }

    class Cmd {
        companion object {
            //Mailing related events
            const val newEmail = 101
            const val trackingUpdate = 102
            const val newError = 104
            const val lowOnPreKeys = 107

            //Events triggered by link device feature
            const val deviceAuthRequest = 201
            const val deviceAuthConfirmed = 202
            const val deviceKeyBundleUploaded = 203
            const val deviceDataUploadComplete = 204
            const val deviceRemoved = 205
            const val deviceAuthDenied = 206

            //Events triggered by devices
            const val peerEmailReadStatusUpdate = 301
            const val peerEmailThreadReadStatusUpdate = 302
            const val peerEmailChangedLabels = 303
            const val peerThreadChangedLabels = 304
            const val peerEmailDeleted = 305
            const val peerThreadDeleted = 306
            const val peerEmailUnsendStatusUpdate = 307
            const val peerLabelCreated = 308
            const val peerUserChangeName = 309
            const val deviceLock = 310
            const val recoveryEmailChanged = 311
            const val recoveryEmailConfirmed = 312
            const val profilePictureChanged = 313

            //Sync Devices
            const val syncBeginRequest = 211
            const val syncAccept = 212
            const val syncDeny = 216

            //Get Events
            const val newEvent = 400

            //Update Banner
            const val updateBannerEvent = 401

            const val openEmail = 500

        }
    }
}