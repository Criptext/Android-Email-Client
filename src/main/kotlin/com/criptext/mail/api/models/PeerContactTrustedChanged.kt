package com.criptext.mail.api.models

import org.json.JSONObject

data class PeerContactTrustedChanged(val email: String, val trusted: Boolean){
    companion object {
        fun fromJSON(jsonString: String): PeerContactTrustedChanged {
            val json = JSONObject(jsonString)
            return PeerContactTrustedChanged(
                    email = json.getString("email"),
                    trusted = json.getBoolean("trusted")
            )
        }
    }

    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("cmd", Event.Cmd.peerContactTrustedChanged)
        json.put("email", email)
        json.put("trusted", trusted)
        return json
    }
}