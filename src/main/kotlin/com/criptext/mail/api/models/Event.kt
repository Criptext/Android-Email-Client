package com.criptext.mail.api.models

import org.json.JSONObject

/**
 * Created by gabriel on 4/26/18.
 */
data class Event(val rowid: Long, val cmd: Int, val params: String) {
    companion object {
        fun fromJSON(jsonEventString: String): Event {
            val json = JSONObject(jsonEventString)
            return Event(rowid = json.getLong("rowid"),
                    cmd = json.getInt("cmd"),
                    params = json.getString("params"))
        }
    }

    class Cmd {
        companion object {
            val newEmail = 101
            val trackingUpdate = 102
            val newError = 104
            val deviceRemoved = 205
            val peerEmailReadStatusUpdate = 301
            val peerEmailThreadReadStatusUpdate = 302
            val peerEmailChangedLabels = 303
            val peerThreadChangedLabels = 304
            val peerEmailDeleted = 305
            val peerThreadDeleted = 306
            val peerEmailUnsendStatusUpdate = 307
            val peerLabelCreated = 308
            val peerUserChangeName = 309
            val deviceLock = 310
            val recoveryEmailChanged = 311
            val recoveryEmailConfirmed = 312

        }
    }
}